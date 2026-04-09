package com.example.myapplication.activities.admin;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        RecyclerView rvReports = findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReportAdapter();
        rvReports.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        viewModel.getReports().observe(this, reports -> adapter.setReports(reports));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
