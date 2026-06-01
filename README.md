# 📚 Courses Management - Mobile Application

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Java-green.svg)](https://developer.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-blue.svg)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-orange.svg)](https://developer.android.com/)
[![Database](https://img.shields.io/badge/Backend-Supabase-blueviolet.svg)](https://supabase.com/)
[![Payment](https://img.shields.io/badge/Payment-VNPAY-red.svg)](https://vnpay.vn/)

Đây là ứng dụng di động quản lý khóa học và học trực tuyến (E-learning) được phát triển native trên nền tảng **Android (Java)**. Ứng dụng cung cấp giải pháp toàn diện cho việc học tập trực tuyến, kết nối giữa Học viên, Giảng viên và Quản trị viên trên một nền tảng đồng nhất.

---

## 🏛️ Kiến Trúc & Công Nghệ Sử Dụng

Dự án được xây dựng dựa trên các tiêu chuẩn phát triển Android hiện đại:

- **Kiến trúc MVVM (Model-View-ViewModel)**: Đảm bảo mã nguồn sạch sẽ, tách biệt logic nghiệp vụ khỏi giao diện người dùng (UI), dễ bảo trì và viết unit test. Sử dụng `LiveData` để cập nhật UI thời gian thực theo trạng thái dữ liệu.
- **Mạng & API (Retrofit 2 & OkHttp 3)**: Kết nối và giao tiếp dữ liệu với Backend thông qua các endpoint RESTful APIs.
- **Backend & Database (Supabase)**:
  - **PostgreSQL Database**: Lưu trữ dữ liệu về tài khoản, thông tin khóa học, chương học, bài giảng, giỏ hàng, tiến trình học và các tương tác của người dùng.
  - **Supabase Storage**: Lưu trữ đám mây và tải dữ liệu đa phương tiện (avatar người dùng, ảnh bìa khóa học, video bài giảng).
  - **Supabase Edge Functions**: Triển khai hàm xử lý Cloud (Function `send-fcm`) dùng để trigger gửi thông báo đẩy tự động.
- **Thông báo đẩy (Firebase Cloud Messaging - FCM)**: Nhận thông báo thời gian thực khi có sự kiện phát sinh (Ví dụ: thông báo có khóa học mới, mua hàng thành công, phản hồi bình luận).
- **Cổng thanh toán (VNPAY)**: Tích hợp SDK thanh toán của VNPAY, cho phép người dùng thanh toán khóa học bằng thẻ ngân hàng hoặc ví điện tử nội địa một cách an toàn.
- **Trình phát Video (Google ExoPlayer)**: Trình phát video chuyên nghiệp hỗ trợ stream các bài giảng video mượt mà, hỗ trợ kiểm soát trạng thái phát.
- **Vẽ biểu đồ (MPAndroidChart)**: Trực quan hóa dữ liệu thống kê doanh thu và báo cáo lượng học viên cho Giảng viên và Admin.
- **Quản lý hình ảnh (Glide)**: Tải, bộ nhớ đệm (caching) và tối ưu hóa hiển thị ảnh từ các URL lưu trữ.

---

## 👥 Các Phân Hệ & Tính Năng Chi Tiết

Dự án phân chia chức năng theo 3 nhóm đối tượng người dùng chính:

### 1. Phân hệ Học Viên (Student)
- **Xác thực tài khoản**: Đăng ký, Đăng nhập, Quên mật khẩu, Xác thực mã OTP thông qua Email đăng ký.
- **Khám phá khóa học**:
  - Tìm kiếm khóa học theo từ khóa hoặc bộ lọc danh mục.
  - Xem chi tiết khóa học: Đề cương chi tiết (Chương/Bài học), thông tin giảng viên hướng dẫn, đánh giá xếp hạng từ các học viên khác.
- **Giỏ hàng & Thanh toán**:
  - Thêm các khóa học yêu thích vào giỏ hàng.
  - Tiến hành thanh toán trực tuyến qua cổng **VNPAY**, nhận phản hồi kết quả thanh toán ngay trên ứng dụng và kích hoạt khóa học tự động.
- **Học tập trực quan**:
  - Xem danh sách bài học và xem video bài giảng bằng **ExoPlayer**.
  - Theo dõi tiến độ học tập cá nhân (đánh dấu bài học đã hoàn thành).
  - Làm bài kiểm tra trắc nghiệm (Quiz) đánh giá năng lực cuối mỗi chương học.
- **Tương tác xã hội**: Bình luận, trao đổi bài học trực tiếp với giảng viên/học viên khác; viết đánh giá và xếp hạng (Rating) cho khóa học đã mua.

### 2. Phân hệ Giảng Viên (Instructor)
- **Quản lý khóa học cá nhân**: Tạo khóa học mới, thiết lập mô tả, giá bán và hình ảnh đại diện.
- **Thiết lập chương trình giảng dạy**:
  - Thêm/Sửa/Xóa các chương học (Chapters).
  - Thêm/Sửa/Xóa các bài giảng (Lessons) và tải trực tiếp video bài giảng lên **Supabase Storage** ngay từ thiết bị di động.
- **Thống kê hiệu suất & Doanh thu**: Theo dõi tổng doanh thu, số lượng học viên đăng ký qua các biểu đồ cột và đường sinh động (**MPAndroidChart**).
- **Quản lý hồ sơ**: Cập nhật thông tin tiểu sử, chuyên môn giảng dạy.

### 3. Phân hệ Quản Trị Viên (Admin)
- **Màn hình Dashboard**: Theo dõi số liệu toàn hệ thống về tổng số người dùng, số khóa học, doanh thu tổng.
- **Quản lý & Phê duyệt**: Xem xét và duyệt các khóa học mới được tạo bởi giảng viên trước khi cho phép hiển thị công khai trên ứng dụng.

---

## ⚙️ Hướng Dẫn Cấu Hình & Cài Đặt Dự Án

### 1. Cấu hình biến môi trường (`local.properties`)
Bạn cần tạo hoặc cập nhật file `local.properties` tại thư mục gốc của dự án Android Studio với nội dung như sau:

```properties
# Đường dẫn SDK Android (Tự động thiết lập bởi Android Studio)
sdk.dir=C\:\\Users\\Tancoder\\AppData\\Local\\Android\\Sdk

# Cấu hình kết nối Supabase API
SUPABASE_URL=https://<your-supabase-project-id>.supabase.co/rest/v1/
SUPABASE_API_KEY=<your-supabase-anon-key>
SUPABASE_STORAGE_BUCKET=course-media

# Cấu hình cổng thanh toán VNPAY (Sandbox)
VNP_TMN_CODE=<your-vnpay-tmn-code>
VNP_HASH_SECRET=<your-vnpay-hash-secret>
```

### 2. Cấu hình Firebase Cloud Messaging (`google-services.json`)
- Tạo dự án trên **Firebase Console**.
- Thêm ứng dụng Android với Package Name là `com.example.myapplication`.
- Tải file `google-services.json` xuống và đặt vào thư mục `app/` của dự án (`app/google-services.json`).

### 3. Xây dựng và chạy dự án (Build & Run)
1. Mở thư mục dự án bằng **Android Studio**.
2. Đợi Android Studio hoàn thành quá trình đồng bộ hóa Gradle (`Sync Project with Gradle Files`).
3. Kết nối thiết bị Android thật (bật chế độ USB Debugging) hoặc khởi động máy ảo Emulator (yêu cầu API Level >= 28).
4. Nhấn nút **Run 'app'** (phím tắt `Shift + F10`) để biên dịch và cài đặt ứng dụng lên thiết bị.
