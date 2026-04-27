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
import com.example.myapplication.activities.student.MyCoursesActivity;
import com.example.myapplication.activities.student.SearchActivity;
import com.example.myapplication.activities.common.AccountActivity;
import com.example.myapplication.activities.common.NotificationActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvWelcome;
    private RecyclerView rvCategories, rvFeaturedCourses;
    private CategoryAdapter categoryAdapter;
    private CourseAdapter courseAdapter;
    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
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

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already home
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_courses) {
                startActivity(new Intent(this, MyCoursesActivity.class));
                return true;
            } else if (id == R.id.nav_notification) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                Intent accountIntent = new Intent(this, AccountActivity.class);
                accountIntent.putExtra("email", getIntent().getStringExtra("email"));
                startActivity(accountIntent);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter();
        // Link category click to SearchActivity with category name
        categoryAdapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        courseAdapter = new CourseAdapter();
        // Link course click to CourseDetailActivity
        courseAdapter.setOnItemClickListener(course -> {
            Intent intent = new Intent(this, com.example.myapplication.activities.student.CourseDetailActivity.class);
            intent.putExtra("course_id", course.getId());
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
