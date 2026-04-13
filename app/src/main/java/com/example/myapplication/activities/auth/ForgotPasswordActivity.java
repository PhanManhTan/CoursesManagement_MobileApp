package com.example.myapplication.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        tvLogin = findViewById(R.id.tvLogin);

        btnSendOtp.setOnClickListener(v -> attemptSendOtp());
        tvLogin.setOnClickListener(v -> finish()); // back to Login
    }

    private void attemptSendOtp() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // TODO: call API to send OTP
        Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, OtpVerifyActivity.class);
        intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
        intent.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_FORGOT);
        startActivity(intent);
    }
}
