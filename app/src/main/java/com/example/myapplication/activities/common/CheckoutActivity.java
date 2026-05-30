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

    // Lắng nghe kết quả trả về từ VNPAYActivity
    private final ActivityResultLauncher<Intent> vnPayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // VNPay trả về mã 00 - Thành công
                    processSuccessfulPayment();
                } else {
                    Toast.makeText(this, "Thanh toán thất bại hoặc đã bị hủy!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);

        cartRepository = new CartRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
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

            // 1. Tạo URL VNPay
            String paymentUrl = VNPayUtils.createOrder((long) totalAmount);

            // 2. Mở VNPAYActivity
            Intent intent = new Intent(CheckoutActivity.this, VNPAYActivity.class);
            intent.putExtra("VNPAY_URL", paymentUrl);
            vnPayLauncher.launch(intent);
        });
    }

    private void processSuccessfulPayment() {
        String userId = sessionManager.getUserId();

        // Kiểm tra xem dữ liệu truyền sang có bị mất không
        if (userId == null || courseIds == null || cartIds == null) {
            Toast.makeText(this, "Lỗi dữ liệu: Không tìm thấy khóa học để xử lý!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Đang xử lý đơn hàng...", Toast.LENGTH_LONG).show();

        // 1. Lặp để Add vào bảng Enrollments
        for (String courseId : courseIds) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUserId(userId);
            enrollment.setCourseId(courseId);

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
                        Toast.makeText(CheckoutActivity.this, "Lỗi thêm Enrollment: " + message, Toast.LENGTH_LONG).show();
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