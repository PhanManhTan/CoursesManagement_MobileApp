package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.example.myapplication.models.Enrollment;
import java.util.List;
import java.util.Locale;

public class AdminViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final MutableLiveData<String> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<String> activeCourses = new MutableLiveData<>();
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();
    private final MutableLiveData<String> pendingCourses = new MutableLiveData<>();

    public AdminViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        refreshStats();
    }

    public void refreshStats() {
        // Fetch Total Users
        userRepository.getAll(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                if (data != null) totalUsers.setValue(String.valueOf(data.size()));
                else totalUsers.setValue("0");
            }
            @Override public void onError(String message) { totalUsers.setValue("0"); }
        });

        // Fetch Active Courses
        courseRepository.getByStatus("approved", new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> data) {
                if (data != null) activeCourses.setValue(String.valueOf(data.size()));
                else activeCourses.setValue("0");
            }
            @Override public void onError(String message) { activeCourses.setValue("0"); }
        });

        // Fetch Pending Courses
        courseRepository.getByStatus("pending", new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> data) {
                if (data != null) pendingCourses.setValue(String.valueOf(data.size()));
                else pendingCourses.setValue("0");
            }
            @Override public void onError(String message) { pendingCourses.setValue("0"); }
        });

        // Fetch Total Revenue
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> data) {
                double total = 0;
                if (data != null) {
                    for (Enrollment e : data) {
                        total += e.getPaidAmount();
                    }
                }
                totalRevenue.setValue(String.format(Locale.US, "$%.1fk", total / 1000.0));
            }
            @Override public void onError(String message) { totalRevenue.setValue("$0k"); }
        });
    }

    public LiveData<String> getTotalUsers() { return totalUsers; }
    public LiveData<String> getActiveCourses() { return activeCourses; }
    public LiveData<String> getTotalRevenue() { return totalRevenue; }
    public LiveData<String> getPendingCourses() { return pendingCourses; }
}
