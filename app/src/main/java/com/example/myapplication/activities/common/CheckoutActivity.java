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
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.utils.VNPayUtils;

import java.util.ArrayList;

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
                    processSuccessfulPayment();
                } else {
                    Toast.makeText(this, R.string.payment_failed_or_cancelled, Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);

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
                processSuccessfulPayment();
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
        String paymentUrl = VNPayUtils.createOrder((long) totalAmount);
        Intent intent = new Intent(CheckoutActivity.this, VNPAYActivity.class);
        intent.putExtra("VNPAY_URL", paymentUrl);
        vnPayLauncher.launch(intent);
    }

    private void processSuccessfulPayment() {
        String userId = sessionManager.getUserId();
        if (userId == null || courseIds == null || cartIds == null) {
            Toast.makeText(this, R.string.payment_data_error, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, totalAmount <= 0 ? getString(R.string.processing_enrollment) : getString(R.string.processing_order), Toast.LENGTH_LONG).show();

        for (String courseId : courseIds) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUserId(userId);
            enrollment.setCourseId(courseId);

            Double price = coursePrices.get(courseId);
            if (price == null) price = 0.0;
            enrollment.setPaidAmount(price);

            enrollmentRepository.insert(enrollment, new EnrollmentRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    System.out.println("DEBUG_CHECKOUT: Enrollment successful for courseId: " + courseId);
                }
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(CheckoutActivity.this, getString(R.string.enrollment_add_failed, message), Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
        for (String cartId : cartIds) {
            cartRepository.removeFromCart(cartId, new CartRepository.RepositoryCallback<Void>() {
                @Override public void onSuccess(Void d) {}
                @Override public void onError(String e) {}
            });
        }

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

        startActivity(new Intent(CheckoutActivity.this, PaymentResultActivity.class));
        finish();
    }
}
