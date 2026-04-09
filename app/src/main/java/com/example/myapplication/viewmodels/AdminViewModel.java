package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminViewModel extends ViewModel {
    private final MutableLiveData<String> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<String> activeCourses = new MutableLiveData<>();
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();

    public AdminViewModel() {
        totalUsers.setValue("15.2k");
        activeCourses.setValue("482");
        totalRevenue.setValue("$124k");
    }

    public LiveData<String> getTotalUsers() { return totalUsers; }
    public LiveData<String> getActiveCourses() { return activeCourses; }
    public LiveData<String> getTotalRevenue() { return totalRevenue; }
}
