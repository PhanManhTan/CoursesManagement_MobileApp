package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.activities.admin.AdminDashboardActivity;
import com.example.myapplication.viewmodels.InstructorViewModel;

public class InstructorDashboardActivity extends AppCompatActivity {

    private InstructorViewModel viewModel;
    private TextView tvTotalStudents, tvMonthlyRevenue, tvAvgRating, tvLiveCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_dashboard);

        initViews();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvMonthlyRevenue = findViewById(R.id.tvMonthlyRevenue);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvLiveCourses = findViewById(R.id.tvLiveCourses);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(InstructorViewModel.class);

        // Sử dụng if-null check để tránh crash nếu ID trong XML sai
        viewModel.getTotalStudents().observe(this, students -> {
            if (tvTotalStudents != null) tvTotalStudents.setText(students);
        });
        viewModel.getMonthlyRevenue().observe(this, revenue -> {
            if (tvMonthlyRevenue != null) tvMonthlyRevenue.setText(revenue);
        });
        viewModel.getAvgRating().observe(this, rating -> {
            if (tvAvgRating != null) tvAvgRating.setText(rating);
        });
    }

    private void setupListeners() {
        // Mở danh sách khóa học
        View btnViewCourseList = findViewById(R.id.btnViewCourseList);
        if (btnViewCourseList != null) {
            btnViewCourseList.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.myapplication.activities.instructor.CourseListActivity.class);
                startActivity(intent);
            });
        }

        View btnViewRevenue = findViewById(R.id.btnViewRevenue);
        if (btnViewRevenue != null) {
            btnViewRevenue.setOnClickListener(v -> {
                startActivity(new Intent(this, RevenueActivity.class));
            });
        }

        View btnManageStudents = findViewById(R.id.btnManageStudents);
        if (btnManageStudents != null) {
            btnManageStudents.setOnClickListener(v -> {
                startActivity(new Intent(this, StudentListActivity.class));
            });
        }

        // Nhấn giữ tiêu đề (nếu có id header)
        View header = findViewById(R.id.header);
        if (header != null) {
            header.setOnLongClickListener(v -> {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            });
        }
    }
}
