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
        mockList.add(new Report("r1", "c1", "Course", "user_1", "Inappropriate content."));
        mockList.add(new Report("r2", "l5", "Lecture", "user_2", "Audio quality is terrible."));
        mockList.add(new Report("r3", "u10", "User", "user_3", "Spamming in comments."));
        reports.setValue(mockList);
    }

    public LiveData<List<Report>> getReports() {
        return reports;
    }
}
