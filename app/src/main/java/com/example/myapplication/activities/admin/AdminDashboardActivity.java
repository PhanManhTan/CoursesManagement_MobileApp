package com.example.myapplication.activities.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.viewmodels.AdminViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_admin_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_home) {
                return true;
            } else if (id == R.id.nav_admin_users) {
                startActivity(new android.content.Intent(this, UserManageActivity.class));
                return true;
            } else if (id == R.id.nav_admin_approval) {
                startActivity(new android.content.Intent(this, CourseApprovalActivity.class));
                return true;
            } else if (id == R.id.nav_admin_reports) {
                startActivity(new android.content.Intent(this, ReportActivity.class));
                return true;
            } else if (id == R.id.nav_admin_account) {
                android.content.Intent accountIntent = new android.content.Intent(this, com.example.myapplication.activities.common.AccountActivity.class);
                accountIntent.putExtra("email", getIntent().getStringExtra("email"));
                startActivity(accountIntent);
                return true;
            }
            return false;
        });
    }
}
