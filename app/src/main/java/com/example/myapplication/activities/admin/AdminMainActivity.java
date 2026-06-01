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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
            } else if ("account".equalsIgnoreCase(target) || "admin_account".equalsIgnoreCase(target)) {
                bottomNav.setSelectedItemId(R.id.nav_admin_account);
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
        adapter.setListener(user -> {
            String currentUserId = sessionManager.getUserId();
            if (user.getId() != null && user.getId().equals(currentUserId)) {
                Toast.makeText(this, "Bạn không thể tự khóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("admin".equalsIgnoreCase(user.getRole())) {
                Toast.makeText(this, "Không thể khóa tài khoản Admin!", Toast.LENGTH_SHORT).show();
                return;
            }
            userManageViewModel.toggleBanUser(user);
        });
        userManageViewModel.getUsers().observe(this, adapter::setUsers);

        EditText etSearchUser = root.findViewById(R.id.etSearchUser);
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                userManageViewModel.searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        Spinner spFilterRole = root.findViewById(R.id.spFilterRole);
        if (spFilterRole != null) {
            String[] roles = {getString(R.string.all), getString(R.string.student), getString(R.string.instructor_fallback)};
            String[] roleValues = {"All", "Student", "Instructor"};
            ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_compact, roles);
            roleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_compact);
            spFilterRole.setAdapter(roleAdapter);
            spFilterRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    userManageViewModel.filterUsers(roleValues[position]);
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

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

        EnrollmentTransactionAdapter transactionAdapter = new EnrollmentTransactionAdapter();
        rvReports.setAdapter(transactionAdapter); // Only show transactions, remove violations

        TextView tvTotalAnnualRevenue = root.findViewById(R.id.tvTotalAnnualRevenue);
        TextView tvRevenueTrend = root.findViewById(R.id.tvRevenueTrend);
        TextView tvReportTitle = root.findViewById(R.id.tvReportTitle);

        reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        reportViewModel.getEnrollments().observe(this, transactionAdapter::setTransactions);

        reportViewModel.getTotalAnnualRevenue().observe(this, revenue -> {
            if (revenue != null) tvTotalAnnualRevenue.setText(revenue);
        });
        reportViewModel.getRevenueTrend().observe(this, trend -> {
            if (trend != null) tvRevenueTrend.setText(trend);
        });

        TabLayout tabLayout = root.findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.setVisibility(View.GONE); // No longer needed as we removed Violations
        }
        tvReportTitle.setText(R.string.system_transactions);

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
        ImageView ivAvatar = root.findViewById(R.id.ivAvatar);
        View btnEditProfile = root.findViewById(R.id.btnEditProfile);
        View btnLogout = root.findViewById(R.id.btnLogout);
        setupAccountLanguage(root);
        applyAccountLanguageText(root);

        boolean hasCachedProfile = bindCachedAccountProfile(tvFullName, tvEmail, tvBio, ivAvatar, R.string.admin_role);
        if (!hasCachedProfile) {
            tvFullName.setText(R.string.loading);
            tvEmail.setText("");
            if (tvBio != null) {
                tvBio.setText(R.string.loading);
            }
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            redirectToLogin();
            return;
        }

        if (!hasCachedProfile || !sessionManager.isProfileLoadedMemory()) {
            userRepository.getById(userId, new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        if (user != null) {
                            sessionManager.saveProfile(user.getFullName(), user.getEmail(), user.getBio(), user.getAvatarUrl());
                            bindAccountProfile(
                                    tvFullName,
                                    tvEmail,
                                    tvBio,
                                    ivAvatar,
                                    user.getFullName(),
                                    user.getEmail(),
                                    user.getBio(),
                                    user.getAvatarUrl(),
                                    R.string.admin_role
                            );
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(AdminMainActivity.this, R.string.admin_error_loading_account, Toast.LENGTH_SHORT).show());
                }
            });
        }

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupAccountLanguage(View root) {
        Spinner spLanguage = root.findViewById(R.id.spLanguage);
        if (spLanguage == null) return;

        String[] labels = {getString(R.string.language_english), getString(R.string.language_vietnamese)};
        String[] codes = {LanguageManager.LANGUAGE_ENGLISH, LanguageManager.LANGUAGE_VIETNAMESE};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_compact, labels);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_compact);
        spLanguage.setAdapter(adapter);
        spLanguage.setSelection(findLanguageIndex(codes, LanguageManager.getSavedLanguage(this)), false);
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= codes.length) return;
                String selectedCode = codes[position];
                if (!selectedCode.equals(LanguageManager.getSavedLanguage(AdminMainActivity.this))) {
                    getIntent().putExtra("TARGET_TAB", "account");
                    LanguageManager.saveLanguage(AdminMainActivity.this, selectedCode);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int findLanguageIndex(String[] codes, String selectedCode) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(selectedCode)) {
                return i;
            }
        }
        return 0;
    }

    private void applyAccountLanguageText(View root) {
        setText(root, R.id.tvAccountTitle, getString(R.string.my_account));
        setText(root, R.id.tvBioLabel, getString(R.string.bio_label));
        setText(root, R.id.tvLanguageLabel, getString(R.string.app_language));
        setText(root, R.id.tvLanguageHint, getString(R.string.choose_display_language));
        setText(root, R.id.btnEditProfile, getString(R.string.edit_profile_upper));
        setText(root, R.id.btnLogout, getString(R.string.logout_upper));
    }

    private void setText(View root, int viewId, String text) {
        TextView textView = root.findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    private boolean bindCachedAccountProfile(TextView tvFullName, TextView tvEmail, TextView tvBio,
                                             ImageView ivAvatar, int fallbackNameRes) {
        String cachedFullName = sessionManager.getFullName();
        String cachedEmail = sessionManager.getEmail();
        String cachedBio = sessionManager.getBio();
        String cachedAvatarUrl = sessionManager.getAvatarUrl();
        if (!hasValue(cachedFullName) && !hasValue(cachedEmail)
                && !hasValue(cachedBio) && !hasValue(cachedAvatarUrl)) {
            return false;
        }

        bindAccountProfile(
                tvFullName,
                tvEmail,
                tvBio,
                ivAvatar,
                cachedFullName,
                cachedEmail,
                cachedBio,
                cachedAvatarUrl,
                fallbackNameRes
        );
        return true;
    }

    private void bindAccountProfile(TextView tvFullName, TextView tvEmail, TextView tvBio, ImageView ivAvatar,
                                    String fullName, String email, String bio, String avatarUrl,
                                    int fallbackNameRes) {
        tvFullName.setText(hasValue(fullName) ? fullName : getString(fallbackNameRes));
        tvEmail.setText(hasValue(email) ? email : "");
        if (tvBio != null) {
            tvBio.setText(hasValue(bio) ? bio : getString(R.string.no_bio_available));
        }
        if (ivAvatar == null) {
            return;
        }
        if (hasValue(avatarUrl)) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_camera_24)
                    .error(R.drawable.ic_camera_24)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_camera_24);
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void redirectToLogin() {
        sessionManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
