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
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.NotificationRepository;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Notification;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.utils.VNPayUtils;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private double totalAmount = 0;
    private ArrayList<String> cartIds;
    private ArrayList<String> courseIds;

    private CartRepository cartRepository;
    private EnrollmentRepository enrollmentRepository;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> vnPayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    processSuccessfulPayment();
                } else {
                    Toast.makeText(this, "Payment failed or cancelled!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);

        // Request permission for Android 13+
        requestNotificationPermission();

        cartRepository = new CartRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        sessionManager = new SessionManager(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        Button btnPayNow = findViewById(R.id.btnPayNow);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);
        cartIds = getIntent().getStringArrayListExtra("CART_IDS");
        courseIds = getIntent().getStringArrayListExtra("COURSE_IDS");

        tvTotalAmount.setText(String.format("%,.0fđ", totalAmount));

        if (totalAmount <= 0) {
            btnPayNow.setText("Enroll for Free");
        }

        btnBack.setOnClickListener(v -> finish());

        btnPayNow.setOnClickListener(v -> {
            if (totalAmount <= 0) {
                processSuccessfulPayment();
            } else {
                String paymentUrl = VNPayUtils.createOrder((long) totalAmount);
                Intent intent = new Intent(CheckoutActivity.this, VNPAYActivity.class);
                intent.putExtra("VNPAY_URL", paymentUrl);
                vnPayLauncher.launch(intent);
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
        String channelId = "payment_alert_v10"; // Completely new ID to reset config
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

    private void processSuccessfulPayment() {
        String userId = sessionManager.getUserId();
        if (userId == null || courseIds == null || cartIds == null) {
            Toast.makeText(this, "Data error: No courses found to process!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, totalAmount <= 0 ? "Processing enrollment..." : "Processing order...", Toast.LENGTH_LONG).show();

        // Handle enrollment & remove from cart
        for (String courseId : courseIds) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUserId(userId);
            enrollment.setCourseId(courseId);
            enrollmentRepository.insert(enrollment, new EnrollmentRepository.RepositoryCallback<Void>() {
                @Override public void onSuccess(Void d) {}
                @Override public void onError(String e) {}
            });
        }
        for (String cartId : cartIds) {
            cartRepository.removeFromCart(cartId, new CartRepository.RepositoryCallback<Void>() {
                @Override public void onSuccess(Void d) {}
                @Override public void onError(String e) {}
            });
        }

        // Save notification to DB and send system notification
        String statusTitle = totalAmount <= 0 ? "Enrollment Successful 🎉" : "Payment Successful 🎉";
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(statusTitle);
        notification.setMessage("You have successfully enrolled in the course! Happy learning.");

        Context appContext = getApplicationContext(); // Keep context safe before finish()
        NotificationRepository notifRepo = new NotificationRepository(appContext);
        notifRepo.insert(notification, new NotificationRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                sendSystemNotification(appContext, statusTitle, "Open the app to start learning now!");
            }
            @Override public void onError(String message) {
                // If network error saving to DB, still try to send system notification
                sendSystemNotification(appContext, statusTitle, "Open the app to start learning now!");
            }
        });

        startActivity(new Intent(CheckoutActivity.this, PaymentResultActivity.class));
        finish();
    }
}
