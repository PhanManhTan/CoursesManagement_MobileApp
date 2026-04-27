package com.example.myapplication.activities.instructor;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.viewmodels.InstructorViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InstructorDashboardActivity extends AppCompatActivity {

    private InstructorViewModel viewModel;
    private TextView tvTotalStudents, tvMonthlyRevenue, tvAvgRating;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_dashboard);

        // Initialize UI components
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvMonthlyRevenue = findViewById(R.id.tvMonthlyRevenue);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        bottomNav = findViewById(R.id.bottomNav);

        setupBottomNav();

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(InstructorViewModel.class);

        // Observe data
        viewModel.getTotalStudents().observe(this, students -> tvTotalStudents.setText(students));
        viewModel.getMonthlyRevenue().observe(this, revenue -> tvMonthlyRevenue.setText(revenue));
        viewModel.getAvgRating().observe(this, rating -> tvAvgRating.setText(rating));

        // Click listeners for actions
        findViewById(R.id.btnCreateCourse).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, EditCourseActivity.class));
        });

        findViewById(R.id.btnViewRevenue).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, RevenueActivity.class));
        });

        findViewById(R.id.btnManageStudents).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, StudentListActivity.class));
        });

        // Temporary way to access Admin Dashboard for verification
        findViewById(R.id.header).setOnLongClickListener(v -> {
            startActivity(new android.content.Intent(this, com.example.myapplication.activities.admin.AdminDashboardActivity.class));
            return true;
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_instructor_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_instructor_home) {
                return true;
            } else if (id == R.id.nav_instructor_students) {
                startActivity(new android.content.Intent(this, StudentListActivity.class));
                return true;
            } else if (id == R.id.nav_instructor_revenue) {
                startActivity(new android.content.Intent(this, RevenueActivity.class));
                return true;
            } else if (id == R.id.nav_instructor_account) {
                android.content.Intent accountIntent = new android.content.Intent(this, com.example.myapplication.activities.common.AccountActivity.class);
                accountIntent.putExtra("email", getIntent().getStringExtra("email"));
                startActivity(accountIntent);
                return true;
            }
            return false;
        });
    }
}
