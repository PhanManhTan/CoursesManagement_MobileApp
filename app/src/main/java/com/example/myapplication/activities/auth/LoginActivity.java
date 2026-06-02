package com.example.myapplication.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.activities.common.HomeActivity;
import com.example.myapplication.activities.admin.AdminMainActivity;
import com.example.myapplication.activities.instructor.InstructorMainActivity;
import com.example.myapplication.data.remote.AuthApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private AuthApi authApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        RetrofitClient.setAuthErrorListener(null);
        sessionManager = new SessionManager(this);
        authApi = RetrofitClient.getClient(this).create(AuthApi.class);

        etEmail = findViewById(R.id.editTextTextEmailAddress);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        if (getIntent().getBooleanExtra(OtpVerifyActivity.EXTRA_FROM_REGISTER_OTP, false)) {
            String registeredEmail = getIntent().getStringExtra(OtpVerifyActivity.EXTRA_EMAIL);
            if (registeredEmail != null) {
                etEmail.setText(registeredEmail);
                Toast.makeText(this, R.string.registration_success_login, Toast.LENGTH_LONG).show();
            }
        }

        String extraEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        if (extraEmail != null) {
            etEmail.setText(extraEmail);
        }

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.email_required));
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.valid_email_required));
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.password_required));
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.logging_in);

        AuthApi.LoginRequest request = new AuthApi.LoginRequest(email, password);
        authApi.signIn(request).enqueue(new Callback<AuthApi.LoginResponse>() {
            @Override
            public void onResponse(Call<AuthApi.LoginResponse> call, Response<AuthApi.LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    String accessToken = response.body().getAccessToken();
                    String refreshToken = response.body().getRefreshToken();
                    String userId = response.body().getUser().getId();
                    
                    // 1. Lưu token ban đầu
                    sessionManager.saveSession(accessToken, refreshToken, userId, "student");
                    
                    // 2. Truy vấn thông tin thực tế
                    fetchRealRoleAndRedirect(userId, email, accessToken, refreshToken);
                } else {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login);
                    Toast.makeText(LoginActivity.this, R.string.invalid_email_or_password, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthApi.LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText(R.string.login);
                Toast.makeText(LoginActivity.this, getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRealRoleAndRedirect(String userId, String email, String token, String refreshToken) {
        // Gọi API lấy profile từ bảng users dựa trên ID, thêm status để check ban
        authApi.getUserProfile("eq." + userId, "role,full_name,email,bio,avatar_url,status").enqueue(new Callback<List<AuthApi.UserProfile>>() {
            @Override
            public void onResponse(Call<List<AuthApi.UserProfile>> call, Response<List<AuthApi.UserProfile>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText(R.string.login);

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    handleSessionVerificationFailed(getString(R.string.session_expired_login_again));
                    return;
                }

                String role = "student";
                String status = "active";
                AuthApi.UserProfile profile = response.body().get(0);
                if (profile != null) {
                    role = normalizeValue(profile.getRole(), "student");
                    status = normalizeValue(profile.getStatus(), "active");
                }

                // 🔥 Check if BANNED
                if ("banned".equals(status)) {
                    Toast.makeText(LoginActivity.this, R.string.account_banned_contact_support, Toast.LENGTH_LONG).show();
                    sessionManager.clear(); // Clear the temporary session
                    return;
                }

                // Safe session update
                sessionManager.saveSession(token, refreshToken, userId, role);
                if (profile != null) {
                    String profileEmail = hasValue(profile.getEmail()) ? profile.getEmail() : email;
                    sessionManager.saveProfile(profile.getFullName(), profileEmail, profile.getBio(), profile.getAvatarUrl());
                }

                Toast.makeText(LoginActivity.this, getString(R.string.login_as_role, getRoleLabel(role)), Toast.LENGTH_SHORT).show();

                Intent intent;
                if ("admin".equals(role)) {
                    intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                } else if ("instructor".equals(role)) {
                    intent = new Intent(LoginActivity.this, InstructorMainActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                }

                intent.putExtra("email", email != null ? email : "");
                intent.putExtra("role", role);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<List<AuthApi.UserProfile>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText(R.string.login);
                Log.e("LOGIN_DEBUG", "API Failure: " + t.getMessage());
                handleSessionVerificationFailed(getString(R.string.session_expired_login_again));
            }
        });
    }

    private String getRoleLabel(String role) {
        if ("admin".equals(role)) {
            return getString(R.string.admin_role);
        }
        if ("instructor".equals(role)) {
            return getString(R.string.instructor_fallback);
        }
        return getString(R.string.student_fallback);
    }

    private String normalizeValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.trim().toLowerCase();
        return normalized.isEmpty() ? fallback : normalized;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void handleSessionVerificationFailed(String message) {
        sessionManager.clear();
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
