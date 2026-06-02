package com.example.myapplication.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.remote.AuthApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.utils.LanguageManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Spinner spRole;
    private Button btnRegister;
    private TextView tvLogin;
    private AuthApi authApi;
    private String[] roleCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);

        authApi = RetrofitClient.getClient(this).create(AuthApi.class);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        spRole = findViewById(R.id.spRole);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        roleCodes = new String[]{"student", "instructor"};
        String[] roles = {getString(R.string.student_fallback), getString(R.string.instructor_fallback)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_common_spinner, roles);
        adapter.setDropDownViewResource(R.layout.item_common_spinner);
        spRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        int selectedRole = Math.max(0, spRole.getSelectedItemPosition());
        String role = roleCodes[selectedRole];

        // ===== VALIDATE =====
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.full_name_required));
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.email_required));
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.password_required));
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.min_6_characters));
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.confirm_password_required));
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            etConfirmPassword.requestFocus();
            return;
        }

        // ===== PREPARE DATA =====
        Map<String, Object> data = new HashMap<>();
        data.put("full_name", fullName);
        data.put("role", role);

        AuthApi.SignUpRequest request =
                new AuthApi.SignUpRequest(email, password, data);

        // ===== UI LOADING =====
        btnRegister.setEnabled(false);
        btnRegister.setText(R.string.processing);

        // ===== CALL API =====
        authApi.signUp(request).enqueue(new Callback<AuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> response) {

                btnRegister.setEnabled(true);
                btnRegister.setText(R.string.create_account);

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            R.string.registration_success_verify_otp,
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, OtpVerifyActivity.class);
                    intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
                    intent.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_REGISTER);
                    intent.putExtra(OtpVerifyActivity.EXTRA_ROLE, role);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            getString(R.string.register_failed_code, response.code()),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText(R.string.create_account);
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.error_with_message, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
