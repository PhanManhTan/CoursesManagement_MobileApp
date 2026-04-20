package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.activities.admin.AdminDashboardActivity;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.activities.auth.OnboardingActivity;
import com.example.myapplication.activities.instructor.InstructorDashboardActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
        SessionManager session = new SessionManager(this);
        Intent intent;

        if (!session.isOnboardingDone()) {
            intent = new Intent(this, OnboardingActivity.class);
        } else if (session.isLoggedIn()) {
            String role = session.getUserRole();
            if ("admin".equals(role)) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if ("instructor".equals(role)) {
                intent = new Intent(this, InstructorDashboardActivity.class);
            } else {
                intent = new Intent(this, HomeActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
