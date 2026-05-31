package com.example.myapplication.activities.common;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.NotificationAdapter;
import com.example.myapplication.data.repository.NotificationRepository;
import com.example.myapplication.models.Notification;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private NotificationRepository repository;
    private SessionManager sessionManager;
    private NotificationAdapter adapter;
    private List<Notification> notificationsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(notificationsList);
        rvNotifications.setAdapter(adapter);

        sessionManager = new SessionManager(this);
        repository = new NotificationRepository(this);

        loadNotifications();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_notification);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    private void loadNotifications() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            return;
        }

        repository.getByUserId(userId, new NotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                runOnUiThread(() -> {
                    if (data != null) {
                        notificationsList = data;
                        adapter.setNotifications(notificationsList);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(NotificationActivity.this, "Failed to load notifications: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}

