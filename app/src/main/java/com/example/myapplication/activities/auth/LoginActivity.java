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
import com.example.myapplication.activities.admin.AdminDashboardActivity;
import com.example.myapplication.activities.common.HomeActivity;
import com.example.myapplication.activities.instructor.InstructorDashboardActivity;
import com.example.myapplication.models.AuthResponse;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository   = new AuthRepository(this);
        etEmail          = findViewById(R.id.editTextTextEmailAddress);
        etPassword       = findViewById(R.id.editTextTextPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister       = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());

        // Pressing Done on keyboard triggers login
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
    }

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Clear previous errors
        etEmail.setError(null);
        etPassword.setError(null);

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

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                navigateByRole(authRepository.getSession().getUserRole());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void navigateByRole(String role) {
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("instructor".equals(role)) {
            intent = new Intent(this, InstructorDashboardActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
