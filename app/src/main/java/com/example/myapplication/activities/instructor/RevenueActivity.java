package com.example.myapplication.activities.instructor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.EnrollmentTransactionAdapter;
import com.example.myapplication.viewmodels.RevenueViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class RevenueActivity extends AppCompatActivity {

    private RevenueViewModel viewModel;
    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvTotalRevenue, tvEmptyTransactions;
    private RecyclerView rvTransactions;
    private EnrollmentTransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvEmptyTransactions = findViewById(R.id.tvEmptyTransactions);
        rvTransactions = findViewById(R.id.rvTransactions);

        viewModel = new ViewModelProvider(this).get(RevenueViewModel.class);

        setupCharts();
        setupRecyclerView();
        observeData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupCharts() {
        // BarChart styling
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(Color.WHITE);

        // PieChart styling
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.getLegend().setTextColor(Color.WHITE);
    }

    private void setupRecyclerView() {
        transactionAdapter = new EnrollmentTransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void observeData() {
        viewModel.getBarEntries().observe(this, entries -> {
            BarDataSet dataSet = new BarDataSet(entries, "Monthly Revenue");
            dataSet.setColor(Color.parseColor("#5A4FCF"));
            dataSet.setValueTextColor(Color.WHITE);
            
            BarData data = new BarData(dataSet);
            barChart.setData(data);
            barChart.invalidate();
        });

        viewModel.getPieEntries().observe(this, entries -> {
            PieDataSet dataSet = new PieDataSet(entries, "Course Sales");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);
            
            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.invalidate();
        });

        viewModel.getTotalRevenue().observe(this, revenue -> tvTotalRevenue.setText(revenue));

        viewModel.getRecentTransactions().observe(this, transactions -> {
            transactionAdapter.setTransactions(transactions);
            boolean isEmpty = transactions == null || transactions.isEmpty();
            tvEmptyTransactions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvTransactions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }
}
