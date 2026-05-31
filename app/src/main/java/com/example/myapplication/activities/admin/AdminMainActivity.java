package com.example.myapplication.activities.admin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.activities.common.EditProfileActivity;
import com.example.myapplication.adapters.CourseApprovalAdapter;
import com.example.myapplication.adapters.EnrollmentTransactionAdapter;
import com.example.myapplication.adapters.ReportAdapter;
import com.example.myapplication.adapters.UserAdapter;
import com.example.myapplication.data.repository.FcmRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.ApiErrorFormatter;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.viewmodels.AdminViewModel;
import com.example.myapplication.viewmodels.CourseApprovalViewModel;
import com.example.myapplication.viewmodels.ReportViewModel;
import com.example.myapplication.viewmodels.UserManageViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;

public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";

    private FrameLayout contentContainer;
    private BottomNavigationView bottomNav;

    // ViewModels
    private AdminViewModel adminViewModel;
    private UserManageViewModel userManageViewModel;
    private CourseApprovalViewModel courseApprovalViewModel;
    private ReportViewModel reportViewModel;

    // Repositories & Helpers
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        String role = sessionManager.getRole();
        if (role == null || !"admin".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Access denied: Admins only", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin_main);

        contentContainer = findViewById(R.id.contentContainer);
        bottomNav = findViewById(R.id.bottomNav);
        userRepository = new UserRepository(this);

        setupBottomNav();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null) {
            if (!sessionManager.isLoggedIn() || !"admin".equalsIgnoreCase(sessionManager.getRole())) {
                redirectToLogin();
            }
        }
    }


    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("TARGET_TAB")) {
            String target = intent.getStringExtra("TARGET_TAB");
            if ("course_pending_approval".equalsIgnoreCase(target)) {
                bottomNav.setSelectedItemId(R.id.nav_admin_approval);
            } else if ("course_violation_report".equalsIgnoreCase(target)) {
                bottomNav.setSelectedItemId(R.id.nav_admin_reports);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_admin_home);
            }
        } else {
            // Default to home/dashboard if not selected
            if (bottomNav.getSelectedItemId() == R.id.nav_admin_home) {
                showDashboard();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_admin_home);
            }
        }
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            clearAdminObservers();
            if (id == R.id.nav_admin_home) {
                showDashboard();
                return true;
            } else if (id == R.id.nav_admin_users) {
                showUserManage();
                return true;
            } else if (id == R.id.nav_admin_approval) {
                showCourseApproval();
                return true;
            } else if (id == R.id.nav_admin_reports) {
                showReports();
                return true;
            } else if (id == R.id.nav_admin_account) {
                showAccount();
                return true;
            }
            return false;
        });
    }

    private View inflateContent(int layoutResId) {
        contentContainer.removeAllViews();
        View root = getLayoutInflater().inflate(layoutResId, contentContainer, false);
        contentContainer.addView(root);
        return root;
    }

    private void clearAdminObservers() {
        if (adminViewModel != null) {
            adminViewModel.getTotalUsers().removeObservers(this);
            adminViewModel.getActiveCourses().removeObservers(this);
            adminViewModel.getTotalRevenue().removeObservers(this);
            adminViewModel.getPendingCourses().removeObservers(this);
        }
        if (userManageViewModel != null) {
            userManageViewModel.getUsers().removeObservers(this);
        }
        if (courseApprovalViewModel != null) {
            courseApprovalViewModel.getPendingCourses().removeObservers(this);
        }
        if (reportViewModel != null) {
            reportViewModel.getReports().removeObservers(this);
            reportViewModel.getEnrollments().removeObservers(this);
            reportViewModel.getTotalAnnualRevenue().removeObservers(this);
            reportViewModel.getRevenueTrend().removeObservers(this);
        }
    }

    // ─── [1] Tab: Dashboard ───
    private void showDashboard() {
        View root = inflateContent(R.layout.activity_admin_dashboard);

        // Hide inner bottomNav if present in sub-layouts
        View innerNav = root.findViewById(R.id.bottomNav);
        if (innerNav != null) innerNav.setVisibility(View.GONE);

        TextView tvTotalUsers = root.findViewById(R.id.tvTotalUsers);
        TextView tvActiveCourses = root.findViewById(R.id.tvActiveCourses);
        TextView tvTotalRevenue = root.findViewById(R.id.tvTotalRevenue);
        TextView tvPendingCourses = root.findViewById(R.id.tvPendingCourses);

        root.findViewById(R.id.btnCourseApproval).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_admin_approval));
        root.findViewById(R.id.btnUserManagement).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_admin_users));
        root.findViewById(R.id.btnSystemReports).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_admin_reports));

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        adminViewModel.getTotalUsers().observe(this, val -> tvTotalUsers.setText(val));
        adminViewModel.getActiveCourses().observe(this, val -> {
            if (val != null) tvActiveCourses.setText(val);
            else tvActiveCourses.setText("0");
        });
        adminViewModel.getTotalRevenue().observe(this, val -> tvTotalRevenue.setText(val));
        adminViewModel.getPendingCourses().observe(this, val -> {
            if (tvPendingCourses != null) tvPendingCourses.setText(val);
        });

        adminViewModel.refreshStats();
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
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Log.d(TAG, "FCM Registration Token: " + token);

                String userId = sessionManager.getUserId();
                if (userId != null) {
                    new FcmRepository(this).uploadToken(userId, token);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Messaging: " + e.getMessage());
        }
    }

    // ─── [2] Tab: User Manage ───
    private void showUserManage() {
        View root = inflateContent(R.layout.activity_user_manage);

        View innerNav = root.findViewById(R.id.bottomNav);
        if (innerNav != null) innerNav.setVisibility(View.GONE);

        // Hide back button in tab mode
        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        RecyclerView rvUsers = root.findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        UserAdapter adapter = new UserAdapter();
        rvUsers.setAdapter(adapter);

        userManageViewModel = new ViewModelProvider(this).get(UserManageViewModel.class);
        adapter.setListener(user -> userManageViewModel.toggleBanUser(user));
        userManageViewModel.getUsers().observe(this, adapter::setUsers);

        EditText etSearchUser = root.findViewById(R.id.etSearchUser);
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                userManageViewModel.searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        root.findViewById(R.id.tvFilterAll).setOnClickListener(v -> userManageViewModel.filterUsers("All"));
        root.findViewById(R.id.tvFilterStudent).setOnClickListener(v -> userManageViewModel.filterUsers("Student"));
        root.findViewById(R.id.tvFilterInstructor).setOnClickListener(v -> userManageViewModel.filterUsers("Instructor"));

        userManageViewModel.fetchUsers();
    }

    // ─── [3] Tab: Course Approval ───
    private void showCourseApproval() {
        View root = inflateContent(R.layout.activity_course_approval);

        View innerNav = root.findViewById(R.id.bottomNav);
        if (innerNav != null) innerNav.setVisibility(View.GONE);

        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        RecyclerView rvCourses = root.findViewById(R.id.rvCoursesPending);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        CourseApprovalAdapter adapter = new CourseApprovalAdapter();
        rvCourses.setAdapter(adapter);

        courseApprovalViewModel = new ViewModelProvider(this).get(CourseApprovalViewModel.class);
        courseApprovalViewModel.getPendingCourses().observe(this, adapter::setCourses);

        adapter.setListener(new CourseApprovalAdapter.OnApprovalListener() {
            @Override
            public void onApprove(Course course) {
                courseApprovalViewModel.approveCourse(course);
            }

            @Override
            public void onReject(Course course) {
                courseApprovalViewModel.rejectCourse(course);
            }

            @Override
            public void onCourseClick(Course course) {
                Intent intent = new Intent(AdminMainActivity.this, com.example.myapplication.activities.student.CourseDetailActivity.class);
                intent.putExtra("COURSE_ID", course.getId());
                startActivity(intent);
            }
        });

        TabLayout tabLayout = root.findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.removeAllTabs();
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_pending_approvals));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_active_courses));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        courseApprovalViewModel.fetchPendingCourses();
                    } else {
                        courseApprovalViewModel.fetchApprovedCourses();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        courseApprovalViewModel.fetchPendingCourses();
                    } else {
                        courseApprovalViewModel.fetchApprovedCourses();
                    }
                }
            });
        }

        courseApprovalViewModel.fetchPendingCourses();
    }

    // ─── [4] Tab: Reports ───
    private void showReports() {
        View root = inflateContent(R.layout.activity_report);

        View innerNav = root.findViewById(R.id.bottomNav);
        if (innerNav != null) innerNav.setVisibility(View.GONE);

        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        RecyclerView rvReports = root.findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));

        ReportAdapter reportAdapter = new ReportAdapter();
        EnrollmentTransactionAdapter transactionAdapter = new EnrollmentTransactionAdapter();
        rvReports.setAdapter(reportAdapter); // Default to reports/violations

        TextView tvTotalAnnualRevenue = root.findViewById(R.id.tvTotalAnnualRevenue);
        TextView tvRevenueTrend = root.findViewById(R.id.tvRevenueTrend);
        TextView tvReportTitle = root.findViewById(R.id.tvReportTitle);

        reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        reportViewModel.getReports().observe(this, reportAdapter::setReports);
        reportViewModel.getEnrollments().observe(this, transactionAdapter::setTransactions);

        reportViewModel.getTotalAnnualRevenue().observe(this, revenue -> {
            if (revenue != null) tvTotalAnnualRevenue.setText(revenue);
        });
        reportViewModel.getRevenueTrend().observe(this, trend -> {
            if (trend != null) tvRevenueTrend.setText(trend);
        });

        TabLayout tabLayout = root.findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.removeAllTabs();
            tabLayout.addTab(tabLayout.newTab().setText(R.string.violations));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.transactions));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        tvReportTitle.setText(R.string.user_reports_flags);
                        rvReports.setAdapter(reportAdapter);
                    } else {
                        tvReportTitle.setText(R.string.system_transactions);
                        rvReports.setAdapter(transactionAdapter);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        reportViewModel.fetchReports();
        reportViewModel.fetchRevenueStats();
    }

    // ─── [5] Tab: Account ───
    private void showAccount() {
        View root = inflateContent(R.layout.account_activity);

        View innerNav = root.findViewById(R.id.bottomNav);
        if (innerNav != null) innerNav.setVisibility(View.GONE);

        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        TextView tvFullName = root.findViewById(R.id.tvFullName);
        TextView tvEmail = root.findViewById(R.id.tvEmail);
        TextView tvBio = root.findViewById(R.id.tvBio);
        View btnEditProfile = root.findViewById(R.id.btnEditProfile);
        View btnLogout = root.findViewById(R.id.btnLogout);
        tvFullName.setText(R.string.loading);
        tvEmail.setText("");
        if (tvBio != null) {
            tvBio.setText(R.string.loading);
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            redirectToLogin();
            return;
        }

        userRepository.getById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        tvFullName.setText(user.getFullName() != null ? user.getFullName() : getString(R.string.admin_role));
                        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                        if (tvBio != null) {
                            tvBio.setText(user.getBio() != null && !user.getBio().trim().isEmpty()
                                    ? user.getBio()
                                    : getString(R.string.no_bio_available));
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(AdminMainActivity.this, R.string.admin_error_loading_account, Toast.LENGTH_SHORT).show());
            }
        });

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void redirectToLogin() {
        sessionManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
