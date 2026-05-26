package com.example.myapplication.activities.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ReportAdapter;
import com.example.myapplication.adapters.EnrollmentTransactionAdapter;
import com.example.myapplication.viewmodels.ReportViewModel;
import com.google.android.material.tabs.TabLayout;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReportActivity extends AppCompatActivity {

    private ReportViewModel viewModel;
    private ReportAdapter reportAdapter;
    private EnrollmentTransactionAdapter transactionAdapter;
    private TextView tvTotalAnnualRevenue, tvRevenueTrend, tvReportTitle;
    private RecyclerView rvReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        rvReports = findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));

        reportAdapter = new ReportAdapter();
        transactionAdapter = new EnrollmentTransactionAdapter();
        rvReports.setAdapter(reportAdapter); // Default to reports/violations list

        tvTotalAnnualRevenue = findViewById(R.id.tvTotalAnnualRevenue);
        tvRevenueTrend = findViewById(R.id.tvRevenueTrend);
        tvReportTitle = findViewById(R.id.tvReportTitle);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        
        viewModel.getReports().observe(this, reports -> reportAdapter.setReports(reports));
        viewModel.getEnrollments().observe(this, transactions -> transactionAdapter.setTransactions(transactions));
        
        viewModel.getTotalAnnualRevenue().observe(this, revenue -> {
            if (revenue != null) tvTotalAnnualRevenue.setText(revenue);
        });
        
        viewModel.getRevenueTrend().observe(this, trend -> {
            if (trend != null) tvRevenueTrend.setText(trend);
        });

        setupTabLayout();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNav(bottomNav);
    }

    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Violations"));
            tabLayout.addTab(tabLayout.newTab().setText("Transactions"));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        tvReportTitle.setText("User Reports & Flags");
                        rvReports.setAdapter(reportAdapter);
                    } else {
                        tvReportTitle.setText("System Transactions");
                        rvReports.setAdapter(transactionAdapter);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupBottomNav(BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(R.id.nav_admin_reports);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_reports) {
                return true;
            }
            Intent intent;
            if (id == R.id.nav_admin_home) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if (id == R.id.nav_admin_users) {
                intent = new Intent(this, UserManageActivity.class);
            } else if (id == R.id.nav_admin_approval) {
                intent = new Intent(this, CourseApprovalActivity.class);
            } else if (id == R.id.nav_admin_account) {
                intent = new Intent(this, com.example.myapplication.activities.common.AccountActivity.class);
            } else {
                return false;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        });
    }
}
