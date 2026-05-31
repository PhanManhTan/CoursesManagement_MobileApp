package com.example.myapplication.activities.common;

import android.os.Bundle;

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
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> notifications = new ArrayList<>();
        NotificationAdapter adapter = new NotificationAdapter(notifications);
        rvNotifications.setAdapter(adapter);
        loadNotifications(notifications, adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_notification);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    private void loadNotifications(List<Notification> notifications, NotificationAdapter adapter) {
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        NotificationRepository repository = new NotificationRepository(this);
        repository.getByUserId(userId, new NotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                runOnUiThread(() -> {
                    notifications.clear();
                    if (data != null) {
                        notifications.addAll(data);
                        Collections.sort(notifications, (a, b) -> {
                            String dateA = a != null && a.getCreatedAt() != null ? a.getCreatedAt() : "";
                            String dateB = b != null && b.getCreatedAt() != null ? b.getCreatedAt() : "";
                            return dateB.compareTo(dateA);
                        });
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    notifications.clear();
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}
