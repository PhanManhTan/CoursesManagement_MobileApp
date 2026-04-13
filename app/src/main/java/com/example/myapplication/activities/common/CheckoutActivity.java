package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class CheckoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);

        Button btnPayNow = findViewById(R.id.btnPayNow);
        btnPayNow.setOnClickListener(v -> {
            startActivity(new Intent(CheckoutActivity.this, PaymentResultActivity.class));
            finish();
        });
    }
}