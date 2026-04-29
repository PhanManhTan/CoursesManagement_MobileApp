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
import com.example.myapplication.activities.admin.AdminDashboardActivity;
import com.example.myapplication.activities.instructor.InstructorDashboardActivity;
import com.example.myapplication.data.remote.AuthApi;
import com.example.myapplication.data.remote.RetrofitClient;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
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
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        AuthApi.LoginRequest request = new AuthApi.LoginRequest(email, password);
        authApi.signIn(request).enqueue(new Callback<AuthApi.LoginResponse>() {
            @Override
            public void onResponse(Call<AuthApi.LoginResponse> call, Response<AuthApi.LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lưu token để dùng cho các request sau
                    sessionManager.saveToken(response.body().getAccessToken());
                    String userId = response.body().getUser().getId();
                    
                    // 2. Truy vấn thông tin thực tế từ database
                    fetchRealRoleAndRedirect(userId, email);
                } else {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthApi.LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRealRoleAndRedirect(String userId, String email) {
        // Gọi API lấy profile từ bảng users dựa trên ID
        authApi.getUserProfile("eq." + userId, "role,full_name").enqueue(new Callback<List<AuthApi.UserProfile>>() {
            @Override
            public void onResponse(Call<List<AuthApi.UserProfile>> call, Response<List<AuthApi.UserProfile>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                String role = "student"; // Mặc định
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    role = response.body().get(0).getRole().toLowerCase();
                    Log.d("LOGIN_DEBUG", "Fetched role from DB: " + role);
                } else {
                    Log.e("LOGIN_DEBUG", "Failed to fetch profile or profile empty. Code: " + response.code());
                }

                Toast.makeText(LoginActivity.this, "Login as: " + role, Toast.LENGTH_SHORT).show();

                Intent intent;
                if (role.contains("admin")) {
                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                } else if (role.contains("instructor")) {
                    intent = new Intent(LoginActivity.this, InstructorDashboardActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                }

                intent.putExtra("email", email);
                intent.putExtra("role", role);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<List<AuthApi.UserProfile>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Log.e("LOGIN_DEBUG", "API Failure: " + t.getMessage());
                // Fallback về Home nếu có lỗi
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
}
