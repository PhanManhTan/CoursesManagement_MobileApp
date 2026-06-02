package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.LanguageManager;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_payment_result);

        View layoutTransaction = findViewById(R.id.layoutTransaction);
        TextView tvTransactionId = findViewById(R.id.tvTransactionId);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        // Nhận mã giao dịch thật từ CheckoutActivity
        String transactionId = getIntent().getStringExtra("TRANSACTION_ID");

        if (transactionId != null && !transactionId.isEmpty()) {
            tvTransactionId.setText("#" + transactionId);
            layoutTransaction.setVisibility(View.VISIBLE); // Hiển thị mã
        } else {
            layoutTransaction.setVisibility(View.GONE); // Ẩn đi nếu là khóa free
        }

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
