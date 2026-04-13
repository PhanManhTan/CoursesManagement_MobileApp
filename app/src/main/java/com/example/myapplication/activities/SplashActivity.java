package com.example.myapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.activities.auth.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean onboardingDone = prefs.getBoolean("onboarding_done", false);

            Class<?> destination = onboardingDone ? LoginActivity.class : OnboardingActivity.class;
            startActivity(new Intent(this, destination));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
