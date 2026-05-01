package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Review;
import com.example.myapplication.utils.SessionManager;
import java.util.List;
import java.util.Locale;

public class InstructorViewModel extends AndroidViewModel {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final String instructorId;

    private final MutableLiveData<String> totalStudents = new MutableLiveData<>();
    private final MutableLiveData<String> monthlyRevenue = new MutableLiveData<>();
    private final MutableLiveData<String> avgRating = new MutableLiveData<>();
    private final MutableLiveData<String> liveCourses = new MutableLiveData<>();

    public InstructorViewModel(@NonNull Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        reviewRepository = new ReviewRepository(application);
        
        SessionManager sessionManager = new SessionManager(application);
        this.instructorId = sessionManager.getUserId();
        
        refreshStats();
    }

    public void refreshStats() {
        if (instructorId == null) return;

        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                int liveCount = 0;
                if (courses != null) {
                    for (Course c : courses) {
                        if ("approved".equalsIgnoreCase(c.getStatus())) {
                            liveCount++;
                        }
                    }
                }
                liveCourses.setValue(String.valueOf(liveCount));
                fetchEnrollmentStats(courses);
                fetchReviewStats(courses);
            }
            @Override public void onError(String message) {
                liveCourses.setValue("0");
            }
        });
    }

    private void fetchEnrollmentStats(List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                int studentCount = 0;
                double revenue = 0;
                
                if (courses != null && allEnrollments != null) {
                    for (Course course : courses) {
                        for (Enrollment enrollment : allEnrollments) {
                            if (enrollment.getCourseId() != null && enrollment.getCourseId().equals(course.getId())) {
                                studentCount++;
                                
                                // Fallback: If enrollment has no price, use the course price
                                double amount = enrollment.getPaidAmount();
                                if (amount <= 0) {
                                    amount = course.getPrice();
                                }
                                revenue += amount;
                            }
                        }
                    }
                }
                
                totalStudents.setValue(String.valueOf(studentCount));
                monthlyRevenue.setValue(String.format(Locale.US, "$%.0f", revenue));
            }
            @Override public void onError(String message) {
                totalStudents.setValue("0");
                monthlyRevenue.setValue("$0");
            }
        });
    }

    private void fetchReviewStats(List<Course> courses) {
        reviewRepository.getAll(new ReviewRepository.RepositoryCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> allReviews) {
                double totalRating = 0;
                int ratingCount = 0;

                if (courses != null && allReviews != null) {
                    for (Course course : courses) {
                        for (Review review : allReviews) {
                            if (review.getCourseId() != null && review.getCourseId().equals(course.getId())) {
                                totalRating += review.getRating();
                                ratingCount++;
                            }
                        }
                    }
                }

                if (ratingCount > 0) {
                    avgRating.setValue(String.format(Locale.US, "%.1f ★", totalRating / ratingCount));
                } else {
                    avgRating.setValue("0.0 ★");
                }
            }

            @Override
            public void onError(String message) {
                avgRating.setValue("0.0 ★");
            }
        });
    }

    public LiveData<String> getTotalStudents() { return totalStudents; }
    public LiveData<String> getMonthlyRevenue() { return monthlyRevenue; }
    public LiveData<String> getAvgRating() { return avgRating; }
    public LiveData<String> getLiveCourses() { return liveCourses; }
}
