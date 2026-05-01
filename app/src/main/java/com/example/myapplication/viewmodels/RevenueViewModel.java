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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevenueViewModel extends AndroidViewModel {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final String instructorId;

    private final MutableLiveData<List<BarEntry>> barEntries = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> pieEntries = new MutableLiveData<>();

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
            @Override public void onError(String message) {}
        });
    }

    private void aggregateEnrollmentData(List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                Map<String, Float> courseRevenueMap = new HashMap<>();
                Map<Integer, Float> monthlyRevenueMap = new HashMap<>();
                
                if (courses != null && allEnrollments != null) {
                    for (Course course : courses) {
                        float courseTotal = 0;
                        for (Enrollment enrollment : allEnrollments) {
                            if (enrollment.getCourseId() != null && enrollment.getCourseId().equals(course.getId())) {
                                courseTotal += enrollment.getPaidAmount();
                                
                                try {
                                    if (enrollment.getCreatedAt() != null && enrollment.getCreatedAt().length() >= 7) {
                                        int month = Integer.parseInt(enrollment.getCreatedAt().substring(5, 7));
                                        monthlyRevenueMap.put(month, monthlyRevenueMap.getOrDefault(month, 0f) + (float)enrollment.getPaidAmount());
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
            }
            @Override public void onError(String message) {}
        });
    }

    public LiveData<List<BarEntry>> getBarEntries() { return barEntries; }
    public LiveData<List<PieEntry>> getPieEntries() { return pieEntries; }
}
