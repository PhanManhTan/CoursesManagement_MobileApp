package com.example.myapplication.activities.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.activities.admin.AdminDashboardActivity;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.activities.auth.OnboardingActivity;
import com.example.myapplication.activities.instructor.InstructorMainActivity;
import com.example.myapplication.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        progressBar = findViewById(R.id.progressBar);
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 5;
                handler.post(() -> progressBar.setProgress(progressStatus));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            handler.post(this::navigateToNextScreen);
        }).start();
    }

    private void navigateToNextScreen() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean("onboarding_done", false);

        Intent intent;
        if (!onboardingDone) {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        } else if (sessionManager.isLoggedIn()) {
            intent = getSessionIntent();
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private Intent getSessionIntent() {
        String role = sessionManager.getRole();
        if (role == null) {
            return new Intent(SplashActivity.this, LoginActivity.class);
        }

        String normalizedRole = role.trim().toLowerCase();
        if ("admin".equals(normalizedRole)) {
            return new Intent(SplashActivity.this, AdminDashboardActivity.class);
        } else if ("instructor".equals(normalizedRole)) {
            return new Intent(SplashActivity.this, InstructorMainActivity.class);
        }
        return new Intent(SplashActivity.this, HomeActivity.class);
    }
}
