package com.example.myapplication.activities.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.OnboardingAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private TextView tvSkip, tvDots;

    private final String[] titles = {
            "Learn Anytime, Anywhere",
            "Expert Instructors",
            "Track Your Progress"
    };

    private final String[] descriptions = {
            "Access thousands of courses from your phone. Study at your own pace, on your own schedule.",
            "Learn from industry professionals with real-world experience in their fields.",
            "Monitor your learning journey with detailed progress tracking and certificates."
    };

    private final int[] images = {
            R.mipmap.icon_app,
            R.mipmap.icon_app,
            R.mipmap.icon_app
    };

    private final String[] dots = {"● ○ ○", "○ ● ○", "○ ○ ●"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);
        tvDots = findViewById(R.id.tvDots);

        viewPager.setAdapter(new OnboardingAdapter(titles, descriptions, images));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvDots.setText(dots[position]);
                if (position == titles.length - 1) {
                    btnNext.setText("Get Started");
                    tvSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText("Next");
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < titles.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_done", true).apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
