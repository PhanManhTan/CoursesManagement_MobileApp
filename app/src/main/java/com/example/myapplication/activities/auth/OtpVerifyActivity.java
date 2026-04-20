package com.example.myapplication.activities.auth;

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

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.AuthRepository;
import com.example.myapplication.activities.common.HomeActivity;
import com.example.myapplication.models.AuthResponse;

public class OtpVerifyActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_MODE  = "extra_mode";
    public static final String MODE_REGISTER = "register";
    public static final String MODE_FORGOT   = "forgot";

    private EditText[] otpFields;
    private Button btnVerify;
    private TextView tvResend, tvCountdown, tvTarget;
    private CountDownTimer countDownTimer;
    private String mode, email;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        authRepository = new AuthRepository(this);
        email = getIntent().getStringExtra(EXTRA_EMAIL);
        mode  = getIntent().getStringExtra(EXTRA_MODE);

        tvTarget    = findViewById(R.id.tvTarget);
        tvResend    = findViewById(R.id.tvResend);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnVerify   = findViewById(R.id.btnVerify);

        otpFields = new EditText[]{
                findViewById(R.id.etOtp1),
                findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3),
                findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5),
                findViewById(R.id.etOtp6)
        };

        if (email != null) tvTarget.setText(email);

        setupOtpAutoFocus();
        startCountdown();

        btnVerify.setOnClickListener(v -> attemptVerify());

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                resendOtp();
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
            public void onTick(long ms) {
                tvCountdown.setText("Resend in " + (ms / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                tvResend.setEnabled(true);
                tvResend.setAlpha(1f);
            }
        }.start();
    }

    private void resendOtp() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.4f);
        clearOtpFields();

        if (MODE_REGISTER.equals(mode)) {
            authRepository.resendSignupOtp(email, new AuthRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(OtpVerifyActivity.this, "OTP resent to " + email, Toast.LENGTH_SHORT).show();
                        startCountdown();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(OtpVerifyActivity.this, message, Toast.LENGTH_SHORT).show();
                        tvResend.setEnabled(true);
                        tvResend.setAlpha(1f);
                    });
                }
            });
        } else {
            // Forgot password — gửi lại recovery email
            authRepository.forgotPassword(email, new AuthRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(OtpVerifyActivity.this, "OTP resent to " + email, Toast.LENGTH_SHORT).show();
                        startCountdown();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(OtpVerifyActivity.this, message, Toast.LENGTH_SHORT).show();
                        tvResend.setEnabled(true);
                        tvResend.setAlpha(1f);
                    });
                }
            });
        }
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

        String otpType = MODE_FORGOT.equals(mode) ? "recovery" : "signup";

        authRepository.verifyOtp(email, otp.toString(), otpType, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                runOnUiThread(() -> {
                    if (MODE_REGISTER.equals(mode)) {
                        Intent intent = new Intent(OtpVerifyActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(OtpVerifyActivity.this, SetNewPasswordActivity.class);
                        intent.putExtra(SetNewPasswordActivity.EXTRA_ACCESS_TOKEN, response.accessToken);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnVerify.setEnabled(true);
                    btnVerify.setText("Verify");
                    Toast.makeText(OtpVerifyActivity.this, message, Toast.LENGTH_SHORT).show();
                });
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
