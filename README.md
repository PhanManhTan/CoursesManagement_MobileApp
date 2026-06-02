# 📚 Courses Management - Mobile Application

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Java-green.svg)](https://developer.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-blue.svg)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-orange.svg)](https://developer.android.com/)
[![Database](https://img.shields.io/badge/Backend-Supabase-blueviolet.svg)](https://supabase.com/)
[![Payment](https://img.shields.io/badge/Payment-VNPAY-red.svg)](https://vnpay.vn/)

---

# Courses Management Mobile App

Ứng dụng Android quản lý khóa học và học trực tuyến, hỗ trợ 3 nhóm người dùng chính: **Student**, **Instructor** và **Admin**.
Dự án được xây dựng bằng **Android Native Java**, kết nối dữ liệu qua **Supabase**, hỗ trợ xem video bài học, quản lý khóa học, giỏ hàng, thanh toán, thông báo và thống kê.


---

## Giới thiệu

**Courses Management Mobile App** là ứng dụng mobile phục vụ hệ thống học trực tuyến. Ứng dụng cho phép học viên tìm kiếm, đăng ký và học các khóa học; giảng viên tạo và quản lý nội dung khóa học; quản trị viên theo dõi, kiểm duyệt và quản lý hệ thống.

Ứng dụng hướng đến các chức năng chính của một nền tảng E-learning:

* Xác thực tài khoản.
* Tìm kiếm và xem chi tiết khóa học.
* Quản lý giỏ hàng và thanh toán.
* Xem video bài giảng.
* Theo dõi tiến độ học tập.
* Đánh giá, bình luận khóa học.
* Quản lý khóa học cho giảng viên.
* Quản lý người dùng, khóa học và báo cáo cho admin.
* Gửi thông báo đẩy qua Firebase Cloud Messaging.

---

## Tính năng chính

### 1. Student

Nhóm người dùng **Student** dùng ứng dụng để tìm kiếm, mua và học các khóa học.

Các chức năng chính:

* Đăng ký, đăng nhập, xác thực OTP.
* Quên mật khẩu và đặt lại mật khẩu.
* Xem danh sách khóa học.
* Tìm kiếm khóa học theo từ khóa.
* Xem chi tiết khóa học.
* Xem danh sách chương và bài học.
* Thêm khóa học vào giỏ hàng.
* Thanh toán khóa học qua VNPAY.
* Xem khóa học đã đăng ký.
* Học bài giảng bằng video player.
* Theo dõi tiến độ học.
* Làm quiz.
* Bình luận trong bài học.
* Đánh giá và xếp hạng khóa học.
* Nhận thông báo từ hệ thống.

---

### 2. Instructor

Nhóm người dùng **Instructor** dùng ứng dụng để quản lý khóa học và theo dõi hiệu quả giảng dạy.

Các chức năng chính:

* Xem dashboard giảng viên.
* Quản lý danh sách khóa học cá nhân.
* Tạo, sửa thông tin khóa học.
* Quản lý chương học.
* Quản lý bài học.
* Upload hoặc chỉnh sửa nội dung bài giảng.
* Theo dõi học viên tham gia khóa học.
* Xem thống kê doanh thu.
* Xem báo cáo và biểu đồ thống kê.
* Cập nhật hồ sơ cá nhân.

---

### 3. Admin

Nhóm người dùng **Admin** dùng ứng dụng để quản trị toàn hệ thống.

Các chức năng chính:

* Xem dashboard tổng quan.
* Quản lý người dùng.
* Quản lý khóa học.
* Phê duyệt khóa học của giảng viên.
* Xem trước nội dung học tập.
* Theo dõi báo cáo hệ thống.
* Theo dõi doanh thu và thống kê.
* Quản lý thông báo.

---

## Công nghệ sử dụng

| Nhóm               | Công nghệ                                                           |
| ------------------ | ------------------------------------------------------------------- |
| Ngôn ngữ           | Java                                                                |
| Nền tảng           | Android Native                                                      |
| UI                 | XML Layout, Material Components, RecyclerView, CardView, ViewPager2 |
| Kiến trúc          | MVVM, Repository Pattern                                            |
| State/UI Data      | ViewModel, LiveData                                                 |
| API Client         | Retrofit 2, OkHttp Logging Interceptor                              |
| Database / Backend | Supabase, PostgreSQL                                                |
| Storage            | Supabase Storage                                                    |
| Cloud Function     | Supabase Edge Functions                                             |
| Push Notification  | Firebase Cloud Messaging                                            |
| Video Player       | ExoPlayer                                                           |
| Image Loading      | Glide                                                               |
| Chart              | MPAndroidChart                                                      |
| Payment            | VNPAY                                                               |
| Build Tool         | Gradle Kotlin DSL                                                   |

---

## Kiến trúc dự án

Dự án được tổ chức theo hướng tách lớp để dễ bảo trì:

```text
UI Layer
Activity, Adapter, XML Layout

ViewModel Layer
Xử lý trạng thái giao diện và gọi repository

Repository Layer
Đóng vai trò trung gian giữa ViewModel và API

Remote/API Layer
Retrofit interface, Supabase REST API, Edge Function API

Model Layer
Các class dữ liệu như User, Course, Lesson, Chapter, Review, Cart, Report
```

Luồng xử lý cơ bản:

```text
Activity
   ↓
ViewModel
   ↓
Repository
   ↓
Retrofit API
   ↓
Supabase / Edge Function
```

---

## Cấu trúc thư mục

```text
CoursesManagement_MobileApp/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── androidTest/
│       ├── test/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/myapplication/
│           │   ├── activities/
│           │   │   ├── admin/
│           │   │   ├── auth/
│           │   │   ├── common/
│           │   │   ├── instructor/
│           │   │   └── student/
│           │   ├── adapters/
│           │   ├── data/
│           │   │   ├── remote/
│           │   │   └── repository/
│           │   ├── models/
│           │   ├── services/
│           │   ├── utils/
│           │   ├── viewmodels/
│           │   └── CoursesApplication.java
│           └── res/
├── supabase/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

---

## Yêu cầu môi trường

Trước khi chạy dự án, cần chuẩn bị:

* Android Studio.
* JDK 11 hoặc phiên bản tương thích.
* Android SDK API 35.
* Thiết bị Android thật hoặc Emulator.
* Min SDK: API 28.
* Tài khoản Supabase.
* Tài khoản Firebase.
* Thông tin cấu hình VNPAY sandbox nếu muốn test thanh toán.

---

## Cấu hình dự án

### 1. Clone source code

```bash
git clone https://github.com/PhanManhTan/CoursesManagement_MobileApp.git
cd CoursesManagement_MobileApp
```

---

### 2. Mở project bằng Android Studio

Mở thư mục project bằng Android Studio, sau đó chờ Gradle sync hoàn tất.

---

### 3. Cấu hình `local.properties`

Tạo hoặc cập nhật file `local.properties` ở thư mục gốc của project.

Ví dụ:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk

SUPABASE_URL=https://your-project-id.supabase.co/rest/v1/
SUPABASE_API_KEY=your-supabase-anon-key
SUPABASE_STORAGE_BUCKET=course-media

VNP_TMN_CODE=your-vnpay-tmn-code
VNP_HASH_SECRET=your-vnpay-hash-secret
```

Trong đó:

| Biến                      | Ý nghĩa                             |
| ------------------------- | ----------------------------------- |
| `SUPABASE_URL`            | URL REST API của Supabase project   |
| `SUPABASE_API_KEY`        | Supabase anon/public key            |
| `SUPABASE_STORAGE_BUCKET` | Bucket lưu media khóa học           |
| `VNP_TMN_CODE`            | Mã merchant VNPAY                   |
| `VNP_HASH_SECRET`         | Secret key dùng để ký request VNPAY |

---

### 4. Cấu hình Firebase

Để dùng Firebase Cloud Messaging:

1. Tạo project trên Firebase Console.
2. Thêm Android app với package name:

```text
com.example.myapplication
```

3. Tải file:

```text
google-services.json
```

4. Đặt file vào thư mục:

```text
app/google-services.json
```

---

### 5. Cấu hình Supabase

Dự án sử dụng Supabase cho các phần chính:

* Lưu thông tin người dùng.
* Lưu danh mục khóa học.
* Lưu khóa học, chương học, bài học.
* Lưu giỏ hàng.
* Lưu tiến độ học tập.
* Lưu đánh giá và bình luận.
* Lưu thông báo.
* Lưu dữ liệu báo cáo.
* Lưu media bằng Supabase Storage.
* Gọi Edge Function để xử lý một số tác vụ server-side.

Cần đảm bảo Supabase project đã có đầy đủ bảng, policy và storage bucket phù hợp với source code.

---

## Chạy ứng dụng

Sau khi cấu hình xong:

1. Mở project trong Android Studio.
2. Chọn thiết bị Android hoặc Emulator.
3. Nhấn **Run** hoặc dùng phím tắt:

```text
Shift + F10
```

Có thể build bằng terminal:

```bash
./gradlew assembleDebug
```

Trên Windows:

```bash
gradlew.bat assembleDebug
```

File APK debug sẽ được tạo tại:

```text
app/build/outputs/apk/debug/
```

---

## Một số màn hình chính

### Auth

* Splash
* Onboarding
* Login
* Register
* OTP Verify
* Forgot Password
* Set New Password

### Student

* Home
* Search Course
* Course List
* Course Detail
* My Courses
* Learning
* Review

### Instructor

* Instructor Main
* Instructor Dashboard
* Course List
* Edit Course
* Edit Lesson
* Profile Instructor

### Admin

* Admin Main
* Admin Learning Preview

### Common

* Account
* Edit Profile
* Cart
* Checkout
* VNPAY Payment
* Payment Result
* Notification

---

Dự án được thực hiện bởi **Nhóm 11 — Lớp NT118.Q21**.

| STT | Họ và tên       | MSSV     | GitHub                                            |
| --: | --------------- | -------- | ------------------------------------------------- |
|   1 | Phan Mạnh Tân   | 23521404 | [PhanManhTan](https://github.com/PhanManhTan)     |
|   2 | Nguyễn Văn Sơn  | 23521357 | [noseyug](https://github.com/noseyug)             |
|   3 | Huỳnh Minh Quí  | 23521298 | [HMQui](https://github.com/HMQui)                 |
|   4 | Huỳnh Hoàng Huy | 23520606 | [HuynhHHuy](https://github.com/HuynhHHuy)         |
|   5 | Phạm Minh Quang | 23521290 | [mquangpham575](https://github.com/mquangpham575) |

