package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.activities.instructor.InstructorDashboardActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        // Simple delay to show the splash and then jump to the dashboard
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, InstructorDashboardActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
