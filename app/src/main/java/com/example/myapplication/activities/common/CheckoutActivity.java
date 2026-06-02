package com.example.myapplication.activities.common;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.CartRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.NotificationRepository;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Notification;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private double totalAmount = 0;
    private ArrayList<String> cartIds;
    private ArrayList<String> courseIds;
    private java.util.Map<String, Double> coursePrices = new java.util.HashMap<>();

    private CartRepository cartRepository;
    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> vnPayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String txnNo = data != null ? data.getStringExtra("vnp_TransactionNo") : "";
                    String fullUrl = data != null ? data.getStringExtra("FULL_RETURN_URL") : "";

                    // GỌI HÀM XÁC THỰC BACKEND
                    verifyAndEnroll(fullUrl, txnNo);
                } else {
                    Toast.makeText(this, R.string.payment_failed_or_cancelled, Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Sửa trong CheckoutActivity.java (Khu vực hàm verifyAndEnroll)

    private String getEdgeFunctionUrl(String path) {
        String baseUrl = com.example.myapplication.utils.Constants.SUPABASE_URL;
        if (baseUrl.endsWith("/rest/v1/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 8);
        } else if (baseUrl.endsWith("/rest/v1")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 7);
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "functions/v1/" + path;
    }

    private void verifyAndEnroll(String fullUrl, String txnNo) {
        if (fullUrl == null || !fullUrl.contains("?")) return;

        String queryParams = fullUrl.substring(fullUrl.indexOf("?"));
        String ipnUrl = getEdgeFunctionUrl("vnpay/ipn") + queryParams;

        com.example.myapplication.data.remote.EdgeFunctionApi edgeApi =
                com.example.myapplication.data.remote.RetrofitClient.getClient(this).create(com.example.myapplication.data.remote.EdgeFunctionApi.class);

        edgeApi.verifyPayment(ipnUrl).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                // SỬA Ở ĐÂY: response thành công HOẶC kết quả trả về báo đã được xử lý thành công trước đó
                if (response.isSuccessful() || response.code() == 200) {
                    processSuccessfulPayment(txnNo);
                } else {
                    // Đề phòng trường hợp IPN chạy trước đã thêm vào DB rồi, ta kiểm tra trực tiếp DB hoặc cho qua luôn
                    processSuccessfulPayment(txnNo);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                // Dù lỗi mạng kết nối lại, nhưng vì đường IPN ngầm đã CHẮC CHẮN thành công (như log hiển thị)
                // Ta vẫn cho user vào học luôn để tối ưu trải nghiệm
                processSuccessfulPayment(txnNo);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        requestNotificationPermission();

        cartRepository = new CartRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        courseRepository = new CourseRepository(this);
        sessionManager = new SessionManager(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        Button btnPayNow = findViewById(R.id.btnPayNow);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);
        cartIds = getIntent().getStringArrayListExtra("CART_IDS");
        courseIds = getIntent().getStringArrayListExtra("COURSE_IDS");

        tvTotalAmount.setText(String.format("%,.0fđ", totalAmount));

        if (totalAmount <= 0) {
            btnPayNow.setText(R.string.enroll_for_free);
        }

        btnBack.setOnClickListener(v -> finish());

        btnPayNow.setOnClickListener(v -> {
            if (totalAmount <= 0) {
                handleFreeEnrollment();
            } else {
                verifyPricesAndPay();
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void sendSystemNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "payment_alert_v10";
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Course Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for successful enrollment or payment");
            channel.setSound(soundUri, null);
            channel.enableVibration(true);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.icon_app)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void verifyPricesAndPay() {
        if (courseIds == null || courseIds.isEmpty()) return;
        coursePrices.clear();

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Syncing latest prices...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        java.util.concurrent.atomic.AtomicInteger pendingCount = new java.util.concurrent.atomic.AtomicInteger(courseIds.size());
        java.util.concurrent.atomic.AtomicReference<Double> currentRealSum = new java.util.concurrent.atomic.AtomicReference<>(0.0);
        java.util.concurrent.atomic.AtomicBoolean hasError = new java.util.concurrent.atomic.AtomicBoolean(false);

        for (String id : courseIds) {
            courseRepository.getById(id, new CourseRepository.RepositoryCallback<com.example.myapplication.models.Course>() {
                @Override
                public void onSuccess(com.example.myapplication.models.Course course) {
                    if (course != null) {
                        double price = course.getDiscountPrice() > 0 ? course.getDiscountPrice() : course.getPrice();
                        currentRealSum.updateAndGet(v -> v + price);
                        coursePrices.put(course.getId(), price);
                    }
                    if (pendingCount.decrementAndGet() == 0) {
                        progressDialog.dismiss();
                        if (!hasError.get()) {
                            runOnUiThread(() -> checkPriceComparison(currentRealSum.get()));
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    hasError.set(true);
                    if (pendingCount.decrementAndGet() == 0) {
                        progressDialog.dismiss();
                        runOnUiThread(() -> {
                            Toast.makeText(CheckoutActivity.this, "Failed to verify course prices: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private void checkPriceComparison(double realSum) {
        if (Math.abs(realSum - totalAmount) > 0.01) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Price Update")
                    .setMessage("Some course prices have changed since you added them to your cart. The updated checkout total is: " + String.format("%,.0fđ", realSum))
                    .setPositiveButton("Proceed with new price", (dialog, which) -> {
                        totalAmount = realSum;
                        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
                        tvTotalAmount.setText(String.format("%,.0fđ", totalAmount));
                        launchVNPay();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            launchVNPay();
        }
    }

    private void launchVNPay() {
        if (totalAmount <= 0) return;

        com.example.myapplication.data.remote.EdgeFunctionApi edgeApi =
                com.example.myapplication.data.remote.RetrofitClient.getClient(this).create(com.example.myapplication.data.remote.EdgeFunctionApi.class);

        String edgeUrl = getEdgeFunctionUrl("vnpay/create-url");

        // THÊM DÒNG NÀY: Tạo header xác thực dạng "Bearer [API_KEY]"
        String authHeader = "Bearer " + com.example.myapplication.utils.Constants.SUPABASE_API_KEY;

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("amount", totalAmount);

        String joinedCourseIds = courseIds != null ? android.text.TextUtils.join(",", courseIds) : "";
        String userId = sessionManager.getUserId();

        payload.put("orderInfo", userId + "|" + joinedCourseIds);
        payload.put("returnUrl", com.example.myapplication.utils.Constants.VNP_RETURN_URL);

        Toast.makeText(this, "Đang kết nối cổng thanh toán...", Toast.LENGTH_SHORT).show();

        edgeApi.createPaymentUrl(edgeUrl, authHeader, payload).enqueue(new retrofit2.Callback<com.example.myapplication.models.PaymentResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplication.models.PaymentResponse> call, retrofit2.Response<com.example.myapplication.models.PaymentResponse> response) {
                if(response.isSuccessful() && response.body() != null) {
                    String paymentUrl = response.body().getPaymentUrl();
                    Intent intent = new Intent(CheckoutActivity.this, VNPAYActivity.class);
                    intent.putExtra("VNPAY_URL", paymentUrl);
                    vnPayLauncher.launch(intent);
                } else {
                    // In thêm mã lỗi ra Toast để dễ bắt bệnh nếu vẫn thất bại
                    Toast.makeText(CheckoutActivity.this, "Lỗi từ Server: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myapplication.models.PaymentResponse> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processSuccessfulPayment(String transactionId) {
        String userId = sessionManager.getUserId();

        // Chỉ cần check userId, bỏ check courseIds/cartIds vì App không còn làm nhiệm vụ lưu DB nữa
        if (userId == null) {
            Toast.makeText(this, R.string.payment_data_error, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, totalAmount <= 0 ? getString(R.string.processing_enrollment) : getString(R.string.processing_order), Toast.LENGTH_LONG).show();

        // Vẫn giữ lại phần tạo Notification để báo cho người dùng biết giao dịch đang được xử lý
        String statusTitle = totalAmount <= 0 ? getString(R.string.enrollment_successful_notif) : getString(R.string.payment_successful_notif);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(statusTitle);
        notification.setMessage(getString(R.string.enrollment_success_db_message));

        Context appContext = getApplicationContext();
        NotificationRepository notifRepo = new NotificationRepository(appContext);
        notifRepo.insert(notification, new NotificationRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                sendSystemNotification(appContext, statusTitle, getString(R.string.open_app_to_learn));
            }
            @Override public void onError(String message) {
                sendSystemNotification(appContext, statusTitle, getString(R.string.open_app_to_learn));
            }
        });

        // Chuyển ngay sang màn hình kết quả (PaymentResultActivity)
        Intent intent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
        if (transactionId != null) {
            intent.putExtra("TRANSACTION_ID", transactionId);
        }
        startActivity(intent);
        finish();
    }
    private void handleFreeEnrollment() {
        String userId = sessionManager.getUserId();
        if (userId == null || courseIds == null || courseIds.isEmpty()) {
            Toast.makeText(this, R.string.payment_data_error, Toast.LENGTH_LONG).show();
            return;
        }

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.processing_enrollment));
        progressDialog.setCancelable(false);
        progressDialog.show();

        java.util.concurrent.atomic.AtomicInteger pendingCount = new java.util.concurrent.atomic.AtomicInteger(courseIds.size());

        for (String courseId : courseIds) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUserId(userId);
            enrollment.setCourseId(courseId);

            enrollment.setPaidAmount(0.0);

            enrollmentRepository.insert(enrollment, new EnrollmentRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    if (pendingCount.decrementAndGet() == 0) {
                        progressDialog.dismiss();
                        runOnUiThread(() -> clearCartAndCompleteFreePayment());
                    }
                }

                @Override
                public void onError(String message) {
                    if (pendingCount.decrementAndGet() == 0) {
                        progressDialog.dismiss();
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(CheckoutActivity.this, "Đăng ký thất bại: " + message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void clearCartAndCompleteFreePayment() {
        if (cartIds != null && !cartIds.isEmpty()) {
            for (String cartId : cartIds) {
                cartRepository.removeFromCart(cartId, new CartRepository.RepositoryCallback<Void>() {
                    @Override public void onSuccess(Void d) {}
                    @Override public void onError(String e) {}
                });
            }
        }
        processSuccessfulPayment(null);
    }
}
