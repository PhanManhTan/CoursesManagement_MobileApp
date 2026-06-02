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
import com.example.myapplication.data.remote.AuthApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.utils.LanguageManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        tvLogin = findViewById(R.id.tvLogin);

        btnSendOtp.setOnClickListener(v -> attemptSendOtp());
        tvLogin.setOnClickListener(v -> finish()); // back to Login
    }

    private void attemptSendOtp() {
        String email = etEmail.getText().toString().trim();

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

        btnSendOtp.setEnabled(false);
        btnSendOtp.setText(R.string.sending);

        AuthApi authApi = RetrofitClient.getClient(this).create(AuthApi.class);
        
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        authApi.recover(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText(R.string.send_otp);

                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, R.string.check_email_otp, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerifyActivity.class);
                    intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
                    intent.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_FORGOT);
                    startActivity(intent);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.failed_code, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText(R.string.send_otp);
                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
