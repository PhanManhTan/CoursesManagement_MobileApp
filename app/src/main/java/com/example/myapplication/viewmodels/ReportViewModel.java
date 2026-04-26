package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.Report;
import java.util.ArrayList;
import java.util.List;

public class ReportViewModel extends ViewModel {
    private final MutableLiveData<List<Report>> reports = new MutableLiveData<>();

    public ReportViewModel() {
        // Mock reports
        List<Report> mockList = new ArrayList<>();
        mockList.add(new Report("r1", "user_1", "c1", "Inappropriate content.", "pending", "2023-11-01"));
        mockList.add(new Report("r2", "user_2", "c2", "Copyright violation.", "pending", "2023-11-05"));
        mockList.add(new Report("r3", "user_3", "c3", "Spamming in comments.", "resolved", "2023-11-10"));
        reports.setValue(mockList);
    }

    public LiveData<List<Report>> getReports() {
        return reports;
    }
}
