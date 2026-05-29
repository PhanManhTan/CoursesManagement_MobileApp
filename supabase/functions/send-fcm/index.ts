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

    // 2. Fetch all administrators
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

    const adminIds = admins.map((a) => a.id)

    // 3. Fetch FCM tokens for all administrators
    const { data: tokens, error: tokenError } = await supabaseClient
      .from('user_fcm_tokens')
      .select('fcm_token')
      .in('user_id', adminIds)

    if (tokenError || !tokens || tokens.length === 0) {
      return new Response(JSON.stringify({ message: "No FCM tokens registered for admins." }), {
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

    // 5. Setup notification messages based on triggering table
    let title = "Yêu cầu xử lý mới"
    let body = "Vui lòng đăng nhập trang admin để kiểm tra."
    let actionType = "default"

    if (table === 'courses') {
      if (record.status !== 'pending') {
        return new Response(JSON.stringify({ message: "Course status is not pending. Skipping notification." }), {
          headers: { "Content-Type": "application/json" },
          status: 200
        })
      }
      title = "Yêu cầu duyệt khóa học mới"
      body = `Khóa học "${record.title || 'Không tên'}" đang chờ bạn phê duyệt.`
      actionType = "course_pending_approval"
    } else if (table === 'reports') {
      title = "Báo cáo vi phạm mới"
      body = `Khóa học bị báo cáo vi phạm với lý do: "${record.reason || 'N/A'}"`
      actionType = "course_violation_report"
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
