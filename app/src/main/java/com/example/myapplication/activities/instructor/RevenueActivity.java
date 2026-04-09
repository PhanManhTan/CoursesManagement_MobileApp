package com.example.myapplication.activities.instructor;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

        viewModel = new ViewModelProvider(this).get(RevenueViewModel.class);

        setupCharts();
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
    }
}
