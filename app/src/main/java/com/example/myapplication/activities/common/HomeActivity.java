package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CategoryAdapter;
import com.example.myapplication.adapters.CourseAdapter;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.data.repository.CartRepository;
import com.example.myapplication.models.Cart;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.viewmodels.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvWelcome, tvCartBadge;
    private View btnCartContainer;
    private RecyclerView rvCategories, rvFeaturedCourses;
    private TextView tvEmptyCategories, tvEmptyFeaturedCourses;
    private CategoryAdapter categoryAdapter;
    private CourseAdapter courseAdapter;
    private HomeViewModel homeViewModel;
    private CartRepository cartRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_home);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        cartRepository = new CartRepository(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnCartContainer = findViewById(R.id.btnCartContainer);
        bottomNav = findViewById(R.id.bottomNav);
        rvCategories = findViewById(R.id.rvCategories);
        rvFeaturedCourses = findViewById(R.id.rvFeaturedCourses);
        tvEmptyCategories = findViewById(R.id.tvEmptyCategories);
        tvEmptyFeaturedCourses = findViewById(R.id.tvEmptyFeaturedCourses);

        setupRecyclerViews();

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getCategories().observe(this, categories -> {
            java.util.List<com.example.myapplication.models.Category> updatedCategories = new java.util.ArrayList<>();
            com.example.myapplication.models.Category allCategory = new com.example.myapplication.models.Category("", getString(R.string.all_courses), "all", null, null, null);
            updatedCategories.add(allCategory);
            if (categories != null) {
                updatedCategories.addAll(categories);
            }
            categoryAdapter.setCategories(updatedCategories);
            tvEmptyCategories.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
        });

        // Show all courses without client-side status filtering
        homeViewModel.getFeaturedCourses().observe(this, courses -> {
            java.util.List<Course> displayCourses = courses != null ? courses : new java.util.ArrayList<>();
            courseAdapter.setCourses(displayCourses);

            if (displayCourses.isEmpty()) {
                tvEmptyFeaturedCourses.setVisibility(View.VISIBLE);
                rvFeaturedCourses.setVisibility(View.GONE);
                android.util.Log.d("HomeActivity", "No courses available to display.");
            } else {
                tvEmptyFeaturedCourses.setVisibility(View.GONE);
                rvFeaturedCourses.setVisibility(View.VISIBLE);
                android.util.Log.d("HomeActivity", "Successfully loaded " + displayCourses.size() + " courses.");
            }
        });

        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            String name = email.split("@")[0];
            tvWelcome.setText(getString(R.string.welcome_user, capitalize(name)));
        }

        btnCartContainer.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        checkNotificationPermissionAndFetchFCMToken();
    }

    private void checkNotificationPermissionAndFetchFCMToken() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    android.util.Log.w("HomeActivity", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                android.util.Log.d("HomeActivity", "FCM Registration Token: " + token);

                String userId = sessionManager.getUserId();
                if (userId != null) {
                    new com.example.myapplication.data.repository.FcmRepository(this).uploadToken(userId, token);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Error initializing Firebase Messaging: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        updateCartBadge();
        BottomNavigationHelper.updateNotificationBadge(this, bottomNav);
        if (homeViewModel != null) {
            homeViewModel.refreshData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, 0);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateCartBadge() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }

        cartRepository.getByUserId(userId, new CartRepository.RepositoryCallback<List<Cart>>() {
            @Override
            public void onSuccess(List<Cart> data) {
                runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        tvCartBadge.setText(String.valueOf(data.size()));
                        tvCartBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvCartBadge.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> tvCartBadge.setVisibility(View.GONE));
            }
        });
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setOnItemClickListener(category -> {
            homeViewModel.fetchFeaturedCoursesByCategory(category.getId());
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        courseAdapter = new CourseAdapter();
        courseAdapter.setOnItemClickListener(course -> {
            Intent intent = new Intent(this, com.example.myapplication.activities.student.CourseDetailActivity.class);
            intent.putExtra("COURSE_ID", course.getId());
            startActivity(intent);
        });
        rvFeaturedCourses.setLayoutManager(new LinearLayoutManager(this));
        rvFeaturedCourses.setNestedScrollingEnabled(false);
        rvFeaturedCourses.setAdapter(courseAdapter);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
