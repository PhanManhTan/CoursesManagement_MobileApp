package com.example.myapplication.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.myapplication.R;
import com.example.myapplication.activities.common.AccountActivity;
import com.example.myapplication.activities.common.HomeActivity;
import com.example.myapplication.activities.common.NotificationActivity;
import com.example.myapplication.activities.student.MyCoursesActivity;
import com.example.myapplication.activities.student.SearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home && !(activity instanceof HomeActivity)) {
                intent = new Intent(activity, HomeActivity.class);
                // Dọn sạch các Activity nằm trên Home, chỉ dùng lại instance Home gốc
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_search && !(activity instanceof SearchActivity)) {
                intent = new Intent(activity, SearchActivity.class);
            } else if (id == R.id.nav_courses && !(activity instanceof MyCoursesActivity)) {
                intent = new Intent(activity, MyCoursesActivity.class);
            } else if (id == R.id.nav_notification && !(activity instanceof NotificationActivity)) {
                intent = new Intent(activity, NotificationActivity.class);
            } else if (id == R.id.nav_account && !(activity instanceof AccountActivity)) {
                intent = new Intent(activity, AccountActivity.class);
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);

                if (!(activity instanceof HomeActivity) && id != R.id.nav_home) {
                    activity.finish();
                }
                return true;
            }
            return false;
        });
    }
}