package com.example.myapplication.activities.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ReportAdapter;
import com.example.myapplication.viewmodels.ReportViewModel;

public class ReportActivity extends AppCompatActivity {

    private ReportViewModel viewModel;
    private ReportAdapter adapter;
    private TextView tvTotalAnnualRevenue, tvRevenueTrend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        RecyclerView rvReports = findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReportAdapter();
        rvReports.setAdapter(adapter);

        tvTotalAnnualRevenue = findViewById(R.id.tvTotalAnnualRevenue);
        tvRevenueTrend = findViewById(R.id.tvRevenueTrend);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        
        viewModel.getReports().observe(this, reports -> adapter.setReports(reports));
        
        viewModel.getTotalAnnualRevenue().observe(this, revenue -> {
            if (revenue != null) tvTotalAnnualRevenue.setText(revenue);
        });
        
        viewModel.getRevenueTrend().observe(this, trend -> {
            if (trend != null) tvRevenueTrend.setText(trend);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
