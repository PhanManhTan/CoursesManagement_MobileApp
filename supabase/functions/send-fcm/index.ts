import { serve } from "https://deno.land/std@0.177.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.8"
import { JWT } from "https://esm.sh/google-auth-library@9.6.0"

// Read Firebase credentials from Supabase Environment Secrets (configured via CLI or Dashboard)
const FIREBASE_PROJECT_ID = Deno.env.get('FIREBASE_PROJECT_ID')
const FIREBASE_CLIENT_EMAIL = Deno.env.get('FIREBASE_CLIENT_EMAIL')
const FIREBASE_PRIVATE_KEY = Deno.env.get('FIREBASE_PRIVATE_KEY')

serve(async (req) => {
  // Handle CORS Preflight
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: { 'Access-Control-Allow-Origin': '*' } })
  }

  try {
    const payload = await req.json()
    const { record, table } = payload

    if (!FIREBASE_PROJECT_ID || !FIREBASE_CLIENT_EMAIL || !FIREBASE_PRIVATE_KEY) {
      throw new Error("Missing Firebase configuration env secrets on Supabase.")
    }

    // 1. Initialize Supabase Admin Client using environment keys
    const supabaseUrl = Deno.env.get('SUPABASE_URL')
    const supabaseServiceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

    if (!supabaseUrl || !supabaseServiceKey) {
      throw new Error("Missing Supabase configuration env keys.")
    }

    const supabaseClient = createClient(supabaseUrl, supabaseServiceKey)

    // 2. Setup notification messages and target users based on triggering table
    let targetUserIds: string[] = []
    let title = "Yêu cầu xử lý mới"
    let body = "Vui lòng đăng nhập trang admin để kiểm tra."
    let actionType = "default"

    if (table === 'notifications') {
      if (!record.user_id) {
        return new Response(JSON.stringify({ message: "No user_id found in notification. Skipping." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }
      targetUserIds = [record.user_id]
      title = record.title || "Thông báo mới"
      body = record.message || "Bạn có thông báo mới."
      actionType = "general_notification"
    } else if (table === 'courses') {
      if (record.status === 'pending') {
        title = "Yêu cầu duyệt khóa học mới"
        body = `Khóa học "${record.title || 'Không tên'}" đang chờ bạn phê duyệt.`
        actionType = "course_pending_approval"

        // Fetch all administrators
        const { data: admins, error: adminError } = await supabaseClient
          .from('users')
          .select('id')
          .eq('role', 'admin')

        if (adminError || !admins || admins.length === 0) {
          return new Response(JSON.stringify({ message: "No admin users found." }), {
            headers: { "Content-Type": "application/json" },
            status: 200
          })
        }
        targetUserIds = admins.map((a) => a.id)
      } else if (record.status === 'approved' || record.status === 'rejected') {
        if (!record.instructor_id) {
          return new Response(JSON.stringify({ message: "No instructor_id found for course. Skipping." }), {
            headers: { "Content-Type": "application/json" },
            status: 200
          })
        }
        targetUserIds = [record.instructor_id]
        const isApproved = record.status === 'approved'
        title = isApproved ? "Khóa học đã được duyệt" : "Khóa học bị từ chối"
        body = isApproved
          ? `Khóa học "${record.title || 'Không tên'}" của bạn đã được phê duyệt và sẵn sàng cho học viên.`
          : `Khóa học "${record.title || 'Không tên'}" của bạn đã bị từ chối. Vui lòng kiểm tra lại.`
        actionType = isApproved ? "course_approved" : "course_rejected"
      } else {
        return new Response(JSON.stringify({ message: `Course status is "${record.status}". Skipping notification.` }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }
    } else if (table === 'reports') {
      title = "Báo cáo vi phạm mới"
      body = `Khóa học bị báo cáo vi phạm với lý do: "${record.reason || 'N/A'}"`
      actionType = "course_violation_report"

      // Fetch all administrators
      const { data: admins, error: adminError } = await supabaseClient
        .from('users')
        .select('id')
        .eq('role', 'admin')

      if (adminError || !admins || admins.length === 0) {
        return new Response(JSON.stringify({ message: "No admin users found." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }
      targetUserIds = admins.map((a) => a.id)
    } else if (table === 'enrollments') {
      // 1. Fetch course details
      const { data: course, error: courseError } = await supabaseClient
        .from('courses')
        .select('title, instructor_id, price, discount_price')
        .eq('id', record.course_id)
        .single()

      if (courseError || !course) {
        return new Response(JSON.stringify({ message: "Failed to fetch course details for enrollment notification." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }

      if (!course.instructor_id) {
        return new Response(JSON.stringify({ message: "No instructor_id found for course. Skipping." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }

      targetUserIds = [course.instructor_id]

      // 2. Fetch student details
      const { data: student, error: studentError } = await supabaseClient
        .from('users')
        .select('full_name, email')
        .eq('id', record.user_id)
        .single()

      const studentName = student ? (student.full_name || student.email.split('@')[0]) : "Học viên"

      // 3. Format price
      let priceStr = "Free"
      const price = (course.discount_price !== null && course.discount_price > 0) ? course.discount_price : course.price
      if (price > 0) {
        priceStr = new Intl.NumberFormat('vi-VN').format(price) + " VND"
      }

      title = "New Course Purchase"
      body = `${studentName} purchased "${course.title || 'Không tên'}" for ${priceStr}.`
      actionType = "general_notification"

      // 4. Automatically insert a database notification so the instructor sees it in the app's notification list!
      await supabaseClient.from('notifications').insert({
        user_id: course.instructor_id,
        title: title,
        message: body,
        is_read: false
      })
    } else if (table === 'lessons') {
      // 1. Fetch chapter details to get the course_id
      const { data: chapter, error: chapterError } = await supabaseClient
        .from('chapters')
        .select('course_id, title')
        .eq('id', record.chapter_id)
        .single()

      if (chapterError || !chapter) {
        return new Response(JSON.stringify({ message: "Failed to fetch chapter details for lesson notification." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }

      // 2. Fetch course details to get the course title
      const { data: course, error: courseError } = await supabaseClient
        .from('courses')
        .select('title')
        .eq('id', chapter.course_id)
        .single()

      if (courseError || !course) {
        return new Response(JSON.stringify({ message: "Failed to fetch course details for lesson notification." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }

      // 3. Fetch all enrolled students
      const { data: enrolledStudents, error: enrollError } = await supabaseClient
        .from('enrollments')
        .select('user_id')
        .eq('course_id', chapter.course_id)

      if (enrollError || !enrolledStudents || enrolledStudents.length === 0) {
        return new Response(JSON.stringify({ message: "No students enrolled in this course. Skipping notifications." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }

      targetUserIds = enrolledStudents.map((e) => e.user_id)

      title = "New Lesson Added"
      body = `A new lesson "${record.title || 'Không tên'}" was added to "${course.title || 'Không tên'}".`
      actionType = "general_notification"

      // 4. Automatically insert database notifications for each student
      const notificationInserts = targetUserIds.map((studentId) => {
        return supabaseClient.from('notifications').insert({
          user_id: studentId,
          title: title,
          message: body,
          is_read: false
        })
      })
      await Promise.all(notificationInserts)
    }

    if (targetUserIds.length === 0) {
      return new Response(JSON.stringify({ message: "No target users to notify." }), {
        headers: { "Content-Type": "application/json" },
        status: 200
      })
    }

    // 3. Fetch FCM tokens for target users
    const { data: tokens, error: tokenError } = await supabaseClient
      .from('user_fcm_tokens')
      .select('fcm_token')
      .in('user_id', targetUserIds)

    if (tokenError || !tokens || tokens.length === 0) {
      return new Response(JSON.stringify({ message: "No FCM tokens registered for target users." }), {
        headers: { "Content-Type": "application/json" },
        status: 200
      })
    }

    // 4. Generate OAuth2 token for Google FCM API using Service Account
    const jwtClient = new JWT({
      email: FIREBASE_CLIENT_EMAIL,
      key: FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'), // handle newlines correctly in environment vars
      scopes: ['https://www.googleapis.com/auth/firebase.messaging'],
    })

    const tokenResponse = await jwtClient.getAccessToken()
    const accessToken = tokenResponse.token

    if (!accessToken) {
      throw new Error("Failed to retrieve FCM OAuth2 token.")
    }

    // 6. Broadcast push notifications to all admin tokens
    const sendRequests = tokens.map(async (item) => {
      const response = await fetch(
        `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            message: {
              token: item.fcm_token,
              data: {
                title: title,
                body: body,
                type: actionType
              }
            }
          })
        }
      )
      return response.json()
    })

    const results = await Promise.all(sendRequests)

    return new Response(JSON.stringify({ success: true, results }), {
      headers: { "Content-Type": "application/json" },
      status: 200
    })

  } catch (err) {
    return new Response(JSON.stringify({ error: err.message }), {
      headers: { "Content-Type": "application/json" },
      status: 500
    })
  }
})
