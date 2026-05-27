package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CategoryAdapter;
import com.example.myapplication.adapters.CourseAdapter;
import com.example.myapplication.viewmodels.HomeViewModel;
import com.example.myapplication.activities.student.SearchActivity;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvWelcome;
    private RecyclerView rvCategories, rvFeaturedCourses;
    private CategoryAdapter categoryAdapter;
    private CourseAdapter courseAdapter;
    private HomeViewModel homeViewModel;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        bottomNav = findViewById(R.id.bottomNav);
        rvCategories = findViewById(R.id.rvCategories);
        rvFeaturedCourses = findViewById(R.id.rvFeaturedCourses);

        userEmail = getIntent().getStringExtra("email");

        setupRecyclerViews();

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getCategories().observe(this, categories -> categoryAdapter.setCategories(categories));
        homeViewModel.getFeaturedCourses().observe(this, courses -> courseAdapter.setCourses(courses));

        if (userEmail != null && !userEmail.isEmpty()) {
            String name = userEmail.split("@")[0];
            tvWelcome.setText("Welcome, " + capitalize(name));
        }

        bottomNav.setSelectedItemId(R.id.nav_home);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("category_id", category.getId());
            if (userEmail != null && !userEmail.isEmpty()) {
                intent.putExtra("email", userEmail);
            }
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