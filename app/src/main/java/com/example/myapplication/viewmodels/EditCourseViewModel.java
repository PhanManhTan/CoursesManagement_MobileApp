package com.example.myapplication.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.Course;

public class EditCourseViewModel extends ViewModel {
    private final MutableLiveData<Course> course = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public void setCourse(Course courseData) {
        course.setValue(courseData);
    }

    public MutableLiveData<Course> getCourse() {
        return course;
    }

    public MutableLiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public void saveCourse(String title, String description, double price) {
        Course current = course.getValue();
        if (current != null) {
            current.setTitle(title);
            current.setDescription(description);
            current.setPrice(price);
            // Mock saving to Firebase
            saveSuccess.setValue(true);
        }
    }
}
