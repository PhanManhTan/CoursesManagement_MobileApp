package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_result_activity);

        TextView tvTransactionId = findViewById(R.id.tvTransactionId);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        // Giả sử bạn nhận được mã giao dịch từ Intent (khi xử lý Deep Link VNPay trả về)
        String vnp_ResponseCode = getIntent().getStringExtra("vnp_ResponseCode");
        String transactionId = getIntent().getStringExtra("vnp_TransactionNo");

        if (transactionId != null) {
            tvTransactionId.setText("#" + transactionId);
        }

        btnBackToHome.setOnClickListener(v -> {
            // Quay về HomeActivity và xóa sạch các Activity trung gian (Cart, Checkout)
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
