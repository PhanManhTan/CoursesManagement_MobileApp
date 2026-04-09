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
        mockList.add(new Course("c1", "Advanced Android Development", "inst_1", "Learn complex patterns.", 99.99));
        mockList.add(new Course("c2", "Intro to Firebase", "inst_2", "Realtime DB basics.", 49.99));
        mockList.add(new Course("c3", "UI/UX Masterclass", "inst_3", "Figma to Android.", 79.99));
        pendingCourses.setValue(mockList);
    }

    public LiveData<List<Course>> getPendingCourses() {
        return pendingCourses;
    }
}
