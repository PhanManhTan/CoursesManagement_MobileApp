package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseApprovalViewModel extends ViewModel {
    private final MutableLiveData<List<Course>> pendingCourses = new MutableLiveData<>();

    public CourseApprovalViewModel() {
        // Mock pending courses
        List<Course> mockList = new ArrayList<>();
        mockList.add(new Course("c1", "inst_1", "Advanced Android Development", "Learn complex patterns.", "https://via.placeholder.com/150", 99.99, 89.99, "pending", "cat_1", "2023-10-01"));
        mockList.add(new Course("c2", "inst_2", "Intro to Firebase", "Realtime DB basics.", "https://via.placeholder.com/150", 49.99, 39.99, "pending", "cat_2", "2023-10-05"));
        mockList.add(new Course("c3", "inst_3", "UI/UX Masterclass", "Figma to Android.", "https://via.placeholder.com/150", 79.99, 69.99, "pending", "cat_3", "2023-10-10"));
        pendingCourses.setValue(mockList);
    }

    public LiveData<List<Course>> getPendingCourses() {
        return pendingCourses;
    }
}
