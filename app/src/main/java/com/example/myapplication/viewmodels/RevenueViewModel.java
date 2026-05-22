package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.utils.SessionManager;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RevenueViewModel extends AndroidViewModel {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final String instructorId;

    private final MutableLiveData<List<BarEntry>> barEntries = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> pieEntries = new MutableLiveData<>();
    private final MutableLiveData<List<Enrollment>> recentTransactions = new MutableLiveData<>();
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();

    public RevenueViewModel(@NonNull Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        
        SessionManager sessionManager = new SessionManager(application);
        this.instructorId = sessionManager.getUserId();
        
        fetchRevenueData();
    }

    public void fetchRevenueData() {
        if (instructorId == null) return;

        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> instructorCourses) {
                aggregateEnrollmentData(instructorCourses);
            }
            @Override public void onError(String message) {
                barEntries.setValue(new ArrayList<>());
                pieEntries.setValue(new ArrayList<>());
                recentTransactions.setValue(new ArrayList<>());
                totalRevenue.setValue("$0.00");
            }
        });
    }

    private void aggregateEnrollmentData(List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                Map<String, Float> courseRevenueMap = new HashMap<>();
                Map<Integer, Float> monthlyRevenueMap = new HashMap<>();
                Map<String, Course> courseById = new HashMap<>();
                List<Enrollment> transactions = new ArrayList<>();
                double total = 0;
                
                if (courses != null) {
                    for (Course course : courses) {
                        courseById.put(course.getId(), course);
                    }
                }

                if (courses != null && allEnrollments != null) {
                    for (Course course : courses) {
                        float courseTotal = 0;
                        for (Enrollment enrollment : allEnrollments) {
                            if (enrollment.getCourseId() != null && enrollment.getCourseId().equals(course.getId())) {
                                double amount = enrollment.getPaidAmount();
                                courseTotal += amount;
                                total += amount;

                                if (enrollment.getCourse() == null) {
                                    enrollment.setCourse(courseById.get(enrollment.getCourseId()));
                                }
                                transactions.add(enrollment);
                                
                                try {
                                    if (enrollment.getCreatedAt() != null && enrollment.getCreatedAt().length() >= 7) {
                                        int month = Integer.parseInt(enrollment.getCreatedAt().substring(5, 7));
                                        monthlyRevenueMap.put(month, monthlyRevenueMap.getOrDefault(month, 0f) + (float) amount);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                        if (courseTotal > 0 && course.getTitle() != null) {
                            courseRevenueMap.put(course.getTitle(), courseTotal);
                        }
                    }
                }

                List<BarEntry> barData = new ArrayList<>();
                for (int i = 1; i <= 12; i++) {
                    barData.add(new BarEntry(i, monthlyRevenueMap.getOrDefault(i, 0f)));
                }
                barEntries.setValue(barData);

                List<PieEntry> pieData = new ArrayList<>();
                for (Map.Entry<String, Float> entry : courseRevenueMap.entrySet()) {
                    pieData.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
                pieEntries.setValue(pieData);

                Collections.sort(transactions, (first, second) -> {
                    String firstDate = first.getCreatedAt() != null ? first.getCreatedAt() : first.getEnrolledAt();
                    String secondDate = second.getCreatedAt() != null ? second.getCreatedAt() : second.getEnrolledAt();
                    if (firstDate == null) firstDate = "";
                    if (secondDate == null) secondDate = "";
                    return secondDate.compareTo(firstDate);
                });
                recentTransactions.setValue(transactions);
                totalRevenue.setValue(String.format(Locale.US, "$%.2f", total));
            }
            @Override public void onError(String message) {
                barEntries.setValue(new ArrayList<>());
                pieEntries.setValue(new ArrayList<>());
                recentTransactions.setValue(new ArrayList<>());
                totalRevenue.setValue("$0.00");
            }
        });
    }

    public LiveData<List<BarEntry>> getBarEntries() { return barEntries; }
    public LiveData<List<PieEntry>> getPieEntries() { return pieEntries; }
    public LiveData<List<Enrollment>> getRecentTransactions() { return recentTransactions; }
    public LiveData<String> getTotalRevenue() { return totalRevenue; }
}
