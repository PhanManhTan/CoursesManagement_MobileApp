package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.utils.MockData;

public class AdminViewModel extends ViewModel {
    private final MutableLiveData<String> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<String> activeCourses = new MutableLiveData<>();
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();

    public AdminViewModel() {
        totalUsers.setValue(String.valueOf(MockData.getUsers().size()));
        activeCourses.setValue(String.valueOf(MockData.getCourses().size()));
        totalRevenue.setValue("$124k"); // Revenue can stay mock or calculated if needed
    }

    public LiveData<String> getTotalUsers() { return totalUsers; }
    public LiveData<String> getActiveCourses() { return activeCourses; }
    public LiveData<String> getTotalRevenue() { return totalRevenue; }
}
