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

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository    = new AuthRepository(this);
        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etPhone           = findViewById(R.id.etPhone);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister       = findViewById(R.id.btnRegister);
        tvLogin           = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());

        // Pressing Done on keyboard triggers register
        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnRegister.setEnabled(true);
        btnRegister.setText("Create Account");
    }

    private void attemptRegister() {
        String fullName       = etFullName.getText().toString().trim();
        String email          = etEmail.getText().toString().trim();
        String phone          = etPhone.getText().toString().trim();
        String password       = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Clear previous errors
        etFullName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        if (fullName.length() < 2) {
            etFullName.setError("Full name must be at least 2 characters");
            etFullName.requestFocus();
            return;
        }
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
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }
        if (!phone.matches("^0[0-9]{9}$")) {
            etPhone.setError("Enter a valid 10-digit phone number (e.g. 0901234567)");
            etPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        authRepository.register(email, password, fullName, phone, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Check your email for the OTP code", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, OtpVerifyActivity.class);
                    intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
                    intent.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_REGISTER);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create Account");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
