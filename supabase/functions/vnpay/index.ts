import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1"
import hmacSHA512 from "https://esm.sh/crypto-js/hmac-sha512"
import Hex from "https://esm.sh/crypto-js/enc-hex"

// Hàm hỗ trợ sắp xếp các key theo alphabet chuẩn VNPay
function sortObject(obj: any) {
  let sorted: any = {};
  let str = [];
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      str.push(encodeURIComponent(key));
    }
  }
  str.sort();
  for (let key = 0; key < str.length; key++) {
    sorted[str[key]] = encodeURIComponent(obj[str[key]]).replace(/%20/g, "+");
  }
  return sorted;
}

serve(async (req) => {
  try {
    const url = new URL(req.url)

    // ---------------------------------------------------------
    // API 1: TẠO URL THANH TOÁN (App sẽ gọi API này)
    // ---------------------------------------------------------
    if (req.method === 'POST' && url.pathname.endsWith('/create-url')) {
      const VNP_TMN_CODE = Deno.env.get('VNP_TMN_CODE')
      const VNP_HASH_SECRET = Deno.env.get('VNP_HASH_SECRET')

      const payload = await req.json()
      const { amount, orderInfo, returnUrl } = payload

      if (!amount) throw new Error("App không gửi số tiền (amount) lên Server");

      let vnp_Params: any = {};
      vnp_Params['vnp_Version'] = '2.1.0';
      vnp_Params['vnp_Command'] = 'pay';
      vnp_Params['vnp_TmnCode'] = VNP_TMN_CODE;
      vnp_Params['vnp_Locale'] = 'vn';
      vnp_Params['vnp_CurrCode'] = 'VND';
      vnp_Params['vnp_TxnRef'] = new Date().getTime().toString();
      vnp_Params['vnp_OrderInfo'] = orderInfo || 'Thanh toan';
      vnp_Params['vnp_OrderType'] = 'other';
      vnp_Params['vnp_Amount'] = amount * 100;
      vnp_Params['vnp_ReturnUrl'] = returnUrl;
      vnp_Params['vnp_IpAddr'] = '127.0.0.1';

      // Xử lý múi giờ chuẩn VN (GMT+7)
      const vnTime = new Date(new Date().getTime() + 7 * 60 * 60 * 1000);
      const createDate = vnTime.toISOString().replace(/[-:T.]/g, '').slice(0, 14);
      vnp_Params['vnp_CreateDate'] = createDate;

      const expireDate = new Date(vnTime.getTime() + 15 * 60 * 1000);
      vnp_Params['vnp_ExpireDate'] = expireDate.toISOString().replace(/[-:T.]/g, '').slice(0, 14);

      vnp_Params = sortObject(vnp_Params);

      // Tạo chuỗi mã hóa HMAC SHA512
      const signData = Object.keys(vnp_Params).map(key => `${key}=${vnp_Params[key]}`).join('&');
      const hmac = hmacSHA512(signData, VNP_HASH_SECRET);
      vnp_Params['vnp_SecureHash'] = hmac.toString(Hex);

      const paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" +
        Object.keys(vnp_Params).map(key => `${key}=${vnp_Params[key]}`).join('&');

      return new Response(JSON.stringify({ paymentUrl }), { headers: { "Content-Type": "application/json" } })
    }

    // ---------------------------------------------------------
    // API 2: XÁC THỰC TỪ APP & TẠO ENROLLMENT (IPN)
    // ---------------------------------------------------------
    if (req.method === 'GET' && url.pathname.endsWith('/ipn')) {
      try {
        let vnp_Params = Object.fromEntries(url.searchParams);
        const secureHash = vnp_Params['vnp_SecureHash'];

        delete vnp_Params['vnp_SecureHash'];
        delete vnp_Params['vnp_SecureHashType'];

        vnp_Params = sortObject(vnp_Params);
        const signData = Object.keys(vnp_Params).map(key => `${key}=${vnp_Params[key]}`).join('&');

        // Đã chuyển sang đọc bảo mật từ Environment Variables
        const VNP_HASH_SECRET = Deno.env.get('VNP_HASH_SECRET');
        const checkSum = hmacSHA512(signData, VNP_HASH_SECRET).toString(Hex);

        if (secureHash !== checkSum) {
          console.error("❌ LỖI CHỮ KÝ: VNPay gửi (" + secureHash + ") - Server tính ra (" + checkSum + ")");
          return new Response(JSON.stringify({ error: 'Invalid Checksum' }), { status: 400, headers: { "Content-Type": "application/json" } })
        }

        if (vnp_Params['vnp_ResponseCode'] === '00') {
          const supabaseAdmin = createClient(
            Deno.env.get('SUPABASE_URL') ?? '',
            Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
          )

          // Lấy dữ liệu gốc từ URL để tránh lỗi giải mã ký tự "|"
          const orderInfo = url.searchParams.get('vnp_OrderInfo') || "";
          const [userId, courseIdsStr] = orderInfo.split('|');

          if (!courseIdsStr) {
            throw new Error("Không thể tách được User và Course từ chuỗi: " + orderInfo);
          }

          const courseIds = courseIdsStr.split(',');

          console.log("✅ Bắt đầu thêm khóa học cho User: " + userId + ", Khóa: " + courseIdsStr);

          // Insert Database với cấu trúc snake_case đồng bộ
          for (const courseId of courseIds) {
            const { error } = await supabaseAdmin.from('enrollments').insert({
              user_id: userId,
              course_id: courseId
            });

            if (error) {
              console.error("❌ LỖI DATABASE: ", error);
              throw new Error(error.message);
            }
          }

          // Xóa giỏ hàng sau khi đăng ký thành công
          await supabaseAdmin.from('carts').delete().eq('user_id', userId);

          console.log("🎉 Hoàn tất xác thực và lưu Database!");
          return new Response(JSON.stringify({ success: true, message: 'Verified and Enrolled' }), { status: 200, headers: { "Content-Type": "application/json" } })
        }

        return new Response(JSON.stringify({ error: 'Giao dịch thất bại từ VNPay' }), { status: 400, headers: { "Content-Type": "application/json" } })

      } catch (err: any) {
        console.error("❌ LỖI SERVER (500): ", err.message);
        return new Response(JSON.stringify({ error: err.message }), { status: 500, headers: { "Content-Type": "application/json" } })
      }
    }

    return new Response(JSON.stringify({ error: "Endpoint Not Found" }), { status: 404, headers: { "Content-Type": "application/json" } })

  } catch (error: any) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 400,
      headers: { "Content-Type": "application/json" }
    })
  }
})