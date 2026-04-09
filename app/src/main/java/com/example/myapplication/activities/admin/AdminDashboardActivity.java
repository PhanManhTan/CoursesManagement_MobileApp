package com.example.myapplication.activities.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.viewmodels.AdminViewModel;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminViewModel viewModel;
    private TextView tvTotalUsers, tvActiveCourses, tvTotalRevenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveCourses = findViewById(R.id.tvActiveCourses);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        viewModel.getTotalUsers().observe(this, val -> tvTotalUsers.setText(val));
        viewModel.getActiveCourses().observe(this, val -> tvActiveCourses.setText(val));
        viewModel.getTotalRevenue().observe(this, val -> tvTotalRevenue.setText(val));

        findViewById(R.id.btnCourseApproval).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CourseApprovalActivity.class));
        });

        findViewById(R.id.btnUserManagement).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, UserManageActivity.class));
        });

        findViewById(R.id.btnSystemReports).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ReportActivity.class));
        });

        // Temporary way to access Instructor Dashboard for verification
        findViewById(R.id.header).setOnLongClickListener(v -> {
            startActivity(new android.content.Intent(this, com.example.myapplication.activities.instructor.InstructorDashboardActivity.class));
            return true;
        });
    }
}
