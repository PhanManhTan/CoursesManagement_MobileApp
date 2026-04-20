package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.activities.student.MyCoursesActivity;
import com.example.myapplication.activities.student.SearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        bottomNav = findViewById(R.id.bottomNav);

        SessionManager session = new SessionManager(this);
        String fullName = session.getUserFullName();
        String email    = session.getUserEmail();
        if (fullName != null && !fullName.isEmpty()) {
            tvWelcome.setText("Welcome, " + fullName);
        } else if (email != null && !email.isEmpty()) {
            tvWelcome.setText("Welcome, " + capitalize(email.split("@")[0]));
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already home
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_courses) {
                startActivity(new Intent(this, MyCoursesActivity.class));
                return true;
            } else if (id == R.id.nav_notification) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
