package com.example.myapplication.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.AuthRepository;

public class SetNewPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_ACCESS_TOKEN = "extra_access_token";

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private AuthRepository authRepository;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);

        authRepository    = new AuthRepository(this);
        accessToken       = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        etNewPassword     = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword  = findViewById(R.id.btnResetPassword);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(v -> attemptReset());
        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { attemptReset(); return true; }
            return false;
        });
        tvBackToLogin.setOnClickListener(v -> goToLogin());
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnResetPassword.setEnabled(true);
        btnResetPassword.setText("Reset Password");
    }

    private void attemptReset() {
        String password = etNewPassword.getText().toString();
        String confirm  = etConfirmPassword.getText().toString();

        etNewPassword.setError(null);
        etConfirmPassword.setError(null);

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Updating...");

        authRepository.updatePassword(accessToken, password, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(SetNewPasswordActivity.this, "Password updated. Please login.", Toast.LENGTH_LONG).show();
                    goToLogin();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Reset Password");
                    Toast.makeText(SetNewPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
