package com.example.myapplication.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.AuthRepository;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;
    private TextView tvLogin;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        authRepository = new AuthRepository(this);
        etEmail        = findViewById(R.id.etEmail);
        btnSendOtp     = findViewById(R.id.btnSendOtp);
        tvLogin        = findViewById(R.id.tvLogin);

        btnSendOtp.setOnClickListener(v -> attemptSendOtp());

        // Pressing Done on keyboard triggers send OTP
        etEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSendOtp();
                return true;
            }
            return false;
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnSendOtp.setEnabled(true);
        btnSendOtp.setText("Send OTP");
    }

    private void attemptSendOtp() {
        String email = etEmail.getText().toString().trim();

        etEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Sending...");

        authRepository.forgotPassword(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPasswordActivity.this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerifyActivity.class);
                    intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
                    intent.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_FORGOT);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    btnSendOtp.setText("Send OTP");
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
