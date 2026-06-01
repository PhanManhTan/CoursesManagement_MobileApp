package com.example.myapplication.utils;

import android.app.Activity;
import android.content.Intent;
import com.example.myapplication.R;
import com.example.myapplication.activities.common.AccountActivity;
import com.example.myapplication.activities.common.HomeActivity;
import com.example.myapplication.activities.common.NotificationActivity;
import com.example.myapplication.activities.student.MyCoursesActivity;
import com.example.myapplication.activities.student.SearchActivity;
import com.example.myapplication.data.repository.NotificationRepository;
import com.example.myapplication.models.Notification;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class BottomNavigationHelper {

    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNav) {
        // Luôn cập nhật Badge khi setup
        updateNotificationBadge(activity, bottomNav);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> targetClass = null;

            if (id == R.id.nav_home) targetClass = HomeActivity.class;
            else if (id == R.id.nav_search) targetClass = SearchActivity.class;
            else if (id == R.id.nav_courses) targetClass = MyCoursesActivity.class;
            else if (id == R.id.nav_notification) targetClass = NotificationActivity.class;
            else if (id == R.id.nav_account) targetClass = AccountActivity.class;

            if (targetClass != null && !activity.getClass().equals(targetClass)) {
                Intent intent = new Intent(activity, targetClass);
                
                // Sử dụng REORDER_TO_FRONT để tránh khởi tạo lại Activity nếu nó đã tồn tại
                // Giúp chuyển đổi mượt mà hơn và giữ trạng thái trang
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                
                activity.startActivity(intent);
                
                // Tắt hiệu ứng chuyển trang mặc định để tạo cảm giác chuyển tab mượt mà
                activity.overridePendingTransition(0, 0);
                
                // Chỉ finish nếu không phải là HomeActivity để giữ Home làm gốc
                if (!(activity instanceof HomeActivity)) {
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                }
                return true;
            }
            return activity.getClass().equals(targetClass);
        });
    }

    public static void updateNotificationBadge(Activity activity, BottomNavigationView bottomNav) {
        if (bottomNav == null) return;
        
        SessionManager sessionManager = new SessionManager(activity);
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        NotificationRepository repository = new NotificationRepository(activity);
        repository.getUnreadNotifications(userId, new NotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                activity.runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_notification);
                        badge.setNumber(data.size());
                        badge.setVisible(true);
                        // Sửa lỗi: Sử dụng các phương thức setter thay vì gán trực tiếp
                        badge.setBackgroundColor(activity.getResources().getColor(R.color.status_error));
                        badge.setBadgeTextColor(activity.getResources().getColor(R.color.white));
                    } else {
                        bottomNav.removeBadge(R.id.nav_notification);
                    }
                });
            }

            @Override
            public void onError(String message) {
                // Ignore error for badge update
            }
        });
    }
}
