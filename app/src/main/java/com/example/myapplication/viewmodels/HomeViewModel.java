package com.example.myapplication.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Course>> featuredCourses = new MutableLiveData<>();
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionManager sessionManager;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        sessionManager = new SessionManager(application);
        loadData();
    }

    public void refreshData() {
        loadData();
    }

    private void loadData() {
        categoryRepository.getAll(new CategoryRepository.RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.postValue(data);
            }

            @Override
            public void onError(String message) {
                Log.e("HomeViewModel", message);
            }
        });

        fetchFilteredFeaturedCourses(null);
    }

    public void fetchFeaturedCoursesByCategory(String categoryId) {
        fetchFilteredFeaturedCourses(categoryId);
    }

    private void fetchFilteredFeaturedCourses(String categoryId) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            featuredCourses.postValue(new ArrayList<>());
            return;
        }

        // 1. Fetch user's enrollments
        enrollmentRepository.getByUserId(userId, new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> enrollments) {
                Set<String> enrolledCourseIds = new HashSet<>();
                if (enrollments != null) {
                    for (Enrollment enrollment : enrollments) {
                        if (enrollment.getCourseId() != null) {
                            enrolledCourseIds.add(enrollment.getCourseId());
                        }
                    }
                }

                // 2. Fetch courses based on categoryId
                if (categoryId == null || categoryId.isEmpty()) {
                    courseRepository.getAll(new CourseRepository.RepositoryCallback<List<Course>>() {
                        @Override
                        public void onSuccess(List<Course> courses) {
                            filterAndPostCourses(courses, enrolledCourseIds);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("HomeViewModel", "Failed to fetch all courses: " + message);
                        }
                    });
                } else {
                    courseRepository.getByCategoryId(categoryId, new CourseRepository.RepositoryCallback<List<Course>>() {
                        @Override
                        public void onSuccess(List<Course> courses) {
                            filterAndPostCourses(courses, enrolledCourseIds);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("HomeViewModel", "Failed to fetch category courses: " + message);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Log.e("HomeViewModel", "Failed to fetch user enrollments: " + message);
                // Even if fetching enrollments fails, display empty to be safe
                featuredCourses.postValue(new ArrayList<>());
            }
        });
    }

    private void filterAndPostCourses(List<Course> courses, Set<String> enrolledCourseIds) {
        List<Course> filtered = new ArrayList<>();
        if (courses != null) {
            for (Course course : courses) {
                // Only show courses that are visible to students and NOT already enrolled
                if (course.getId() != null 
                        && isVisibleToStudent(course) 
                        && !enrolledCourseIds.contains(course.getId())) {
                    filtered.add(course);
                }
            }
        }
        featuredCourses.postValue(filtered);
    }

    private boolean isVisibleToStudent(Course course) {
        if (course.getStatus() == null || course.getStatus().isEmpty()) {
            return true; // default to true if status is not set
        }
        String status = course.getStatus().trim().toLowerCase(java.util.Locale.US);
        // Do not show pending, review, rejected, draft or private/hidden courses
        if (status.contains("pending") || status.contains("review") || status.contains("reject")
                || status.contains("draft") || status.contains("private") || status.contains("hidden")
                || status.contains("inactive")) {
            return false;
        }
        return true;
    }

    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<List<Course>> getFeaturedCourses() { return featuredCourses; }
}