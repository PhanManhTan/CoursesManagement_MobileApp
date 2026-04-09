package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;

public class RevenueViewModel extends ViewModel {
    private final MutableLiveData<List<BarEntry>> barEntries = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> pieEntries = new MutableLiveData<>();

    public RevenueViewModel() {
        // Mock data for BarChart (12 months)
        List<BarEntry> barData = new ArrayList<>();
        barData.add(new BarEntry(1f, 1200f));
        barData.add(new BarEntry(2f, 1500f));
        barData.add(new BarEntry(3f, 1100f));
        barData.add(new BarEntry(4f, 2200f));
        barData.add(new BarEntry(5f, 2500f));
        barData.add(new BarEntry(6f, 2100f));
        barEntries.setValue(barData);

        // Mock data for PieChart (Courses)
        List<PieEntry> pieData = new ArrayList<>();
        pieData.add(new PieEntry(40f, "Android Dev"));
        pieData.add(new PieEntry(25f, "Java Core"));
        pieData.add(new PieEntry(20f, "Firebase"));
        pieData.add(new PieEntry(15f, "UI/UX"));
        pieEntries.setValue(pieData);
    }

    public LiveData<List<BarEntry>> getBarEntries() {
        return barEntries;
    }

    public LiveData<List<PieEntry>> getPieEntries() {
        return pieEntries;
    }
}
