package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InstructorViewModel extends ViewModel {
    private final MutableLiveData<String> totalStudents = new MutableLiveData<>();
    private final MutableLiveData<String> monthlyRevenue = new MutableLiveData<>();
    private final MutableLiveData<String> avgRating = new MutableLiveData<>();

    public InstructorViewModel() {
        // Initialize with mock data
        totalStudents.setValue("1,234");
        monthlyRevenue.setValue("$5,678");
        avgRating.setValue("4.8 ★");
    }

    public LiveData<String> getTotalStudents() {
        return totalStudents;
    }

    public LiveData<String> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public LiveData<String> getAvgRating() {
        return avgRating;
    }

    // Methods to refresh data from Firebase would go here
}
