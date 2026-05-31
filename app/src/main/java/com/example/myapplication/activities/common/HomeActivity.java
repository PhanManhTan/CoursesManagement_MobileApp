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
import com.example.myapplication.data.repository.CartRepository;
import com.example.myapplication.models.Cart;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.viewmodels.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvWelcome, tvCartBadge;
    private View btnCartContainer;
    private RecyclerView rvCategories, rvFeaturedCourses;
    private CategoryAdapter categoryAdapter;
    private CourseAdapter courseAdapter;
    private HomeViewModel homeViewModel;
    private CartRepository cartRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        cartRepository = new CartRepository(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnCartContainer = findViewById(R.id.btnCartContainer);
        bottomNav = findViewById(R.id.bottomNav);
        rvCategories = findViewById(R.id.rvCategories);
        rvFeaturedCourses = findViewById(R.id.rvFeaturedCourses);

        setupRecyclerViews();
        
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getCategories().observe(this, categories -> categoryAdapter.setCategories(categories));
        homeViewModel.getFeaturedCourses().observe(this, courses -> courseAdapter.setCourses(courses));

        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            String name = email.split("@")[0];
            tvWelcome.setText("Welcome, " + capitalize(name));
        }

        btnCartContainer.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // ĐỒNG BỘ: Sử dụng Helper thay vì set listener thủ công
        bottomNav.setSelectedItemId(R.id.nav_home);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        // Cập nhật badge thông báo mỗi khi quay lại Home
        BottomNavigationHelper.updateNotificationBadge(this, bottomNav);
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
            Intent intent = new Intent(this, com.example.myapplication.activities.student.SearchActivity.class);
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        courseAdapter = new CourseAdapter();
        courseAdapter.setOnItemClickListener(course -> {
            Intent intent = new Intent(this, com.example.myapplication.activities.student.CourseDetailActivity.class);
            intent.putExtra("COURSE_ID", course.getId());
            startActivity(intent);
        });
        rvFeaturedCourses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFeaturedCourses.setAdapter(courseAdapter);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}