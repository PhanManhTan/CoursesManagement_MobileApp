package com.example.myapplication.activities.common;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.NotificationAdapter;
import com.example.myapplication.models.Notification;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.LanguageManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> mockNotifications = new ArrayList<>();
        mockNotifications.add(new Notification("n1", "user123", getString(R.string.notification_payment_success_title), getString(R.string.notification_payment_success_body), false, "2023-10-25"));
        mockNotifications.add(new Notification("n2", "user123", getString(R.string.notification_new_course_title), getString(R.string.notification_new_course_body), true, "2023-10-26"));

        NotificationAdapter adapter = new NotificationAdapter(mockNotifications);
        rvNotifications.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_notification);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }
}
