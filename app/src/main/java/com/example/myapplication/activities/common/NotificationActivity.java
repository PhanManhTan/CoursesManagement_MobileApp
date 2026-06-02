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
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationRepository notificationRepository;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_notification);

        sessionManager = new SessionManager(this);
        notificationRepository = new NotificationRepository(this);

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(notificationList);
        adapter.setOnNotificationClickListener(notification -> {
            if (!notification.isRead()) {
                markAsRead(notification);
            }
        });
        rvNotifications.setAdapter(adapter);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_notification);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

        loadNotifications();
    }

    private void markAsRead(Notification notification) {
        notificationRepository.markAsRead(notification.getId(), new NotificationRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();
                    BottomNavigationHelper.updateNotificationBadge(NotificationActivity.this, bottomNav);
                });
            }

            @Override
            public void onError(String message) {
                // Keep it silent or show toast
            }
        });
    }

    private void loadNotifications() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            return;
        }

        notificationRepository.getByUserId(userId, new NotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                runOnUiThread(() -> {
                    notificationList.clear();
                    if (data != null) {
                        notificationList.addAll(data);
                        Collections.sort(notificationList, (a, b) -> {
                            String dateA = a != null && a.getCreatedAt() != null ? a.getCreatedAt() : "";
                            String dateB = b != null && b.getCreatedAt() != null ? b.getCreatedAt() : "";
                            return dateB.compareTo(dateA);
                        });
                    }
                    adapter.setNotifications(notificationList);
                    BottomNavigationHelper.updateNotificationBadge(NotificationActivity.this, bottomNav);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(NotificationActivity.this, "Failed to load notifications: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, 0);
    }
}
