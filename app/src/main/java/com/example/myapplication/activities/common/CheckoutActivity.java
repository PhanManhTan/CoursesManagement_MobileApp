package com.example.myapplication.activities.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.CartRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.utils.VNPayUtils;
import com.example.myapplication.data.repository.CourseRepository;

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

    // Lắng nghe kết quả trả về từ VNPAYActivity
    private final ActivityResultLauncher<Intent> vnPayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // VNPay trả về mã 00 - Thành công
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

        cartRepository = new CartRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        courseRepository = new CourseRepository(this);
        sessionManager = new SessionManager(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        Button btnPayNow = findViewById(R.id.btnPayNow);

        // Nhận dữ liệu từ Cart
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);
        cartIds = getIntent().getStringArrayListExtra("CART_IDS");
        courseIds = getIntent().getStringArrayListExtra("COURSE_IDS");

        tvTotalAmount.setText(String.format("%,.0fđ", totalAmount));

        btnBack.setOnClickListener(v -> finish());

        btnPayNow.setOnClickListener(v -> {
            if (totalAmount <= 0) return;
            verifyPricesAndPay();
        });
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

        // Kiểm tra xem dữ liệu truyền sang có bị mất không
        if (userId == null || courseIds == null || cartIds == null) {
            Toast.makeText(this, R.string.payment_data_error, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, R.string.processing_order, Toast.LENGTH_LONG).show();

        // 1. Lặp để Add vào bảng Enrollments
        for (String courseId : courseIds) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUserId(userId);
            enrollment.setCourseId(courseId);
            
            Double price = coursePrices.get(courseId);
            if (price == null) {
                price = 0.0;
            }
            enrollment.setPaidAmount(price);

            enrollmentRepository.insert(enrollment, new EnrollmentRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    // In ra logcat để biết đã thành công
                    System.out.println("DEBUG_CHECKOUT: Đã thêm thành công courseId: " + courseId);
                }
                @Override
                public void onError(String message) {
                    // Hiển thị lỗi lên màn hình nếu Supabase từ chối
                    runOnUiThread(() -> {
                        Toast.makeText(CheckoutActivity.this, getString(R.string.enrollment_add_failed, message), Toast.LENGTH_LONG).show();
                        System.out.println("DEBUG_CHECKOUT: Lỗi Insert Enrollment: " + message);
                    });
                }
            });
        }

        // 2. Lặp để Xóa khỏi bảng Carts
        for (String cartId : cartIds) {
            cartRepository.removeFromCart(cartId, new CartRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    System.out.println("DEBUG_CHECKOUT: Đã xóa thành công khỏi giỏ hàng cartId: " + cartId);
                }
                @Override
                public void onError(String message) {
                    System.out.println("DEBUG_CHECKOUT: Lỗi xóa giỏ hàng: " + message);
                }
            });
        }

        // Chuyển hướng tới trang thành công
        startActivity(new Intent(CheckoutActivity.this, PaymentResultActivity.class));
        finish();
    }
}
