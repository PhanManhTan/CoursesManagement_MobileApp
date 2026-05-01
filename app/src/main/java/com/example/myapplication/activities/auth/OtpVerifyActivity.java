package com.example.myapplication.activities.auth;
import com.example.myapplication.data.remote.AuthApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.utils.SessionManager;

import retrofit2.Call;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class OtpVerifyActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_REGISTER = "register";
    public static final String MODE_FORGOT = "forgot";
    public static final String EXTRA_ROLE = "EXTRA_ROLE";

    // Thêm cờ để LoginActivity biết là vừa đăng ký xong
    public static final String EXTRA_FROM_REGISTER_OTP = "extra_from_register_otp";

    private EditText[] otpFields;
    private Button btnVerify;
    private TextView tvResend, tvCountdown, tvTarget;
    private CountDownTimer countDownTimer;
    private String mode;
    private String userEmail;
    private String userRole;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        sessionManager = new SessionManager(this);
        userEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        userRole = getIntent().getStringExtra(EXTRA_ROLE); // Lấy vai trò cho Register

        tvTarget = findViewById(R.id.tvTarget);
        tvResend = findViewById(R.id.tvResend);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnVerify = findViewById(R.id.btnVerify);

        otpFields = new EditText[]{
                findViewById(R.id.etOtp1),
                findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3),
                findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5),
                findViewById(R.id.etOtp6)
        };

        if (userEmail != null) tvTarget.setText(userEmail);

        setupOtpAutoFocus();
        startCountdown();

        btnVerify.setOnClickListener(v -> attemptVerify());

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {

                AuthApi authApi = RetrofitClient.getClient(this).create(AuthApi.class);
                
                String type = MODE_FORGOT.equals(mode) ? "recovery" : "signup";

                AuthApi.ResendRequest request =
                        new AuthApi.ResendRequest(userEmail, type);

                authApi.resend(request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(OtpVerifyActivity.this, "OTP resent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OtpVerifyActivity.this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(OtpVerifyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                clearOtpFields();
                startCountdown();
            }
        });
    }

    private void setupOtpAutoFocus() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                    if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void startCountdown() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.4f);
        tvCountdown.setVisibility(View.VISIBLE);

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(60_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("Resend in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                tvResend.setEnabled(true);
                tvResend.setAlpha(1f);
            }
        }.start();
    }

    private void attemptVerify() {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            String digit = field.getText().toString().trim();
            if (digit.isEmpty()) {
                Toast.makeText(this, "Please enter the complete OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            otp.append(digit);
        }

        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        AuthApi authApi = RetrofitClient.getClient(this).create(AuthApi.class);
        
        // Xác định loại verify dựa trên mode
        // recovery nếu là forgot password, signup nếu là đăng ký
        String type = MODE_FORGOT.equals(mode) ? "recovery" : "signup";
        
        // Cập nhật class VerifyOtpRequest trong AuthApi nếu cần, 
        // hoặc đảm bảo server nhận đúng type. 
        // Supabase cần type để biết verify cho cái gì.

        AuthApi.VerifyOtpRequest request =
                new AuthApi.VerifyOtpRequest(userEmail, otp.toString(), type);

        authApi.verifyOtp(request).enqueue(new Callback<AuthApi.LoginResponse>() {
            @Override
            public void onResponse(Call<AuthApi.LoginResponse> call, Response<AuthApi.LoginResponse> response) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verify");

                if (response.isSuccessful() && response.body() != null) {

                    String accessToken = response.body().getAccessToken();

                    // Sử dụng SessionManager để lưu token đồng bộ với RetrofitClient
                    sessionManager.saveSession(accessToken, response.body().getUser().getId(), "student");

                    Toast.makeText(OtpVerifyActivity.this, "Verified successfully!", Toast.LENGTH_SHORT).show();

                    if (MODE_FORGOT.equals(mode)) {
                        Intent intent = new Intent(OtpVerifyActivity.this, SetNewPasswordActivity.class);
                        intent.putExtra(SetNewPasswordActivity.EXTRA_EMAIL, userEmail);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(OtpVerifyActivity.this, LoginActivity.class);
                        intent.putExtra(EXTRA_EMAIL, userEmail);
                        intent.putExtra(EXTRA_FROM_REGISTER_OTP, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(OtpVerifyActivity.this, "OTP invalid or expired", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthApi.LoginResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verify");
                Toast.makeText(OtpVerifyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearOtpFields() {
        for (EditText field : otpFields) field.setText("");
        otpFields[0].requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
