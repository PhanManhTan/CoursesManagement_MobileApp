package com.example.myapplication.activities.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.viewmodels.AdminViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminViewModel viewModel;
    private TextView tvTotalUsers, tvActiveCourses, tvTotalRevenue;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveCourses = findViewById(R.id.tvActiveCourses);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        bottomNav = findViewById(R.id.bottomNav);

        setupBottomNav();

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        viewModel.getTotalUsers().observe(this, val -> tvTotalUsers.setText(val));
        viewModel.getActiveCourses().observe(this, val -> {
            if (val != null) tvActiveCourses.setText(val);
            else tvActiveCourses.setText("0");
        });
        viewModel.getTotalRevenue().observe(this, val -> tvTotalRevenue.setText(val));
        viewModel.getPendingCourses().observe(this, val -> {
            TextView tvPending = findViewById(R.id.tvPendingCourses);
            if (tvPending != null) tvPending.setText(val);
        });

        findViewById(R.id.btnCourseApproval).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CourseApprovalActivity.class));
        });

        findViewById(R.id.btnUserManagement).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, UserManageActivity.class));
        });

        findViewById(R.id.btnSystemReports).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ReportActivity.class));
        });

        checkNotificationPermissionAndFetchFCMToken();
    }

    private void checkNotificationPermissionAndFetchFCMToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("AdminDashboard", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Log.d("AdminDashboard", "FCM Registration Token: " + token);

                // Sync token with Supabase user_fcm_tokens table
                com.example.myapplication.utils.SessionManager sessionManager = 
                        new com.example.myapplication.utils.SessionManager(AdminDashboardActivity.this);
                String userId = sessionManager.getUserId();
                if (userId != null) {
                    new com.example.myapplication.data.repository.FcmRepository(AdminDashboardActivity.this)
                            .uploadToken(userId, token);
                }
            });
        } catch (Exception e) {
            Log.e("AdminDashboard", "Error initializing Firebase Messaging: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshStats();
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_admin_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_home) {
                return true;
            }
            android.content.Intent intent;
            if (id == R.id.nav_admin_users) {
                intent = new android.content.Intent(this, UserManageActivity.class);
            } else if (id == R.id.nav_admin_approval) {
                intent = new android.content.Intent(this, CourseApprovalActivity.class);
            } else if (id == R.id.nav_admin_reports) {
                intent = new android.content.Intent(this, ReportActivity.class);
            } else if (id == R.id.nav_admin_account) {
                intent = new android.content.Intent(this, com.example.myapplication.activities.common.AccountActivity.class);
                intent.putExtra("email", getIntent().getStringExtra("email"));
            } else {
                return false;
            }
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        });
    }
}
