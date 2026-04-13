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
import com.example.myapplication.activities.HomeActivity;

public class OtpVerifyActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_REGISTER = "register";
    public static final String MODE_FORGOT = "forgot";

    private EditText[] otpFields;
    private Button btnVerify;
    private TextView tvResend, tvCountdown, tvTarget;
    private CountDownTimer countDownTimer;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        mode = getIntent().getStringExtra(EXTRA_MODE);

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

        if (email != null) tvTarget.setText(email);

        setupOtpAutoFocus();
        startCountdown();

        btnVerify.setOnClickListener(v -> attemptVerify());

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                clearOtpFields();
                startCountdown();
                Toast.makeText(this, "OTP resent", Toast.LENGTH_SHORT).show();
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

        // TODO: verify otp with backend
        Toast.makeText(this, "Verifying OTP: " + otp, Toast.LENGTH_SHORT).show();

        if (MODE_REGISTER.equals(mode)) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // forgot password — proceed to set new password
            startActivity(new Intent(this, SetNewPasswordActivity.class));
        }
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
