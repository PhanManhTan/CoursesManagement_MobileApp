package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.R;
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
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();
    private final MutableLiveData<String> totalTransactions = new MutableLiveData<>("0");
    private final MutableLiveData<String> averageTransactionValue = new MutableLiveData<>("$0.00");
    private final MutableLiveData<String> selectedCourseName = new MutableLiveData<>();
    private List<Course> allCourses = new ArrayList<>();
    private List<Enrollment> allEnrollments = new ArrayList<>();
    private String selectedCourseId;

    public RevenueViewModel(@NonNull Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        
        SessionManager sessionManager = new SessionManager(application);
        this.instructorId = sessionManager.getUserId();
        selectedCourseName.setValue(getApplication().getString(R.string.all_courses));
        
        fetchRevenueData();
    }

    public void fetchRevenueData() {
        if (instructorId == null) {
            publishEmptyState();
            return;
        }

        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> instructorCourses) {
                allCourses = instructorCourses != null ? instructorCourses : new ArrayList<>();
                courses.setValue(allCourses);
                ensureSelectedCourseExists();
                fetchEnrollments();
            }
            @Override public void onError(String message) {
                publishEmptyState();
            }
        });
    }

    public void setCourseFilter(String courseId) {
        selectedCourseId = hasValue(courseId) ? courseId : null;
        ensureSelectedCourseExists();
        aggregateEnrollmentData();
    }

    private void fetchEnrollments() {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                RevenueViewModel.this.allEnrollments = allEnrollments != null ? allEnrollments : new ArrayList<>();
                aggregateEnrollmentData();
            }
            @Override public void onError(String message) {
                RevenueViewModel.this.allEnrollments = new ArrayList<>();
                aggregateEnrollmentData();
            }
        });
    }

    private void aggregateEnrollmentData() {
        Map<String, Float> courseRevenueMap = new HashMap<>();
        Map<Integer, Float> monthlyRevenueMap = new HashMap<>();
        Map<String, Course> courseById = new HashMap<>();
        List<Enrollment> transactions = new ArrayList<>();
        double total = 0;

        for (Course course : allCourses) {
            if (course.getId() != null) {
                courseById.put(course.getId(), course);
            }
        }

        for (Enrollment enrollment : allEnrollments) {
            if (enrollment.getCourseId() == null) {
                continue;
            }
            Course course = courseById.get(enrollment.getCourseId());
            if (course == null) {
                continue;
            }
            if (selectedCourseId != null && !selectedCourseId.equals(course.getId())) {
                continue;
            }

            double amount = resolveAmount(enrollment, course);
            if (enrollment.getPaidAmount() <= 0 && amount > 0) {
                enrollment.setPaidAmount(amount);
            }
            if (enrollment.getCourse() == null) {
                enrollment.setCourse(course);
            }

            total += amount;
            transactions.add(enrollment);
            String title = hasValue(course.getTitle()) ? course.getTitle() : getApplication().getString(R.string.untitled_course);
            courseRevenueMap.put(title, courseRevenueMap.getOrDefault(title, 0f) + (float) amount);

            try {
                String date = hasValue(enrollment.getCreatedAt()) ? enrollment.getCreatedAt() : enrollment.getEnrolledAt();
                if (date != null && date.length() >= 7) {
                    int month = Integer.parseInt(date.substring(5, 7));
                    monthlyRevenueMap.put(month, monthlyRevenueMap.getOrDefault(month, 0f) + (float) amount);
                }
            } catch (Exception ignored) {}
        }

        List<BarEntry> barData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            barData.add(new BarEntry(i, monthlyRevenueMap.getOrDefault(i, 0f)));
        }
        barEntries.setValue(barData);

        List<PieEntry> pieData = new ArrayList<>();
        for (Map.Entry<String, Float> entry : courseRevenueMap.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
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
        totalTransactions.setValue(String.valueOf(transactions.size()));
        double average = transactions.isEmpty() ? 0 : total / transactions.size();
        averageTransactionValue.setValue(String.format(Locale.US, "$%.2f", average));
        selectedCourseName.setValue(resolveSelectedCourseName());
    }

    private double resolveAmount(Enrollment enrollment, Course course) {
        if (enrollment.getPaidAmount() > 0) {
            return enrollment.getPaidAmount();
        }
        if (course.getDiscountPrice() > 0) {
            return course.getDiscountPrice();
        }
        return course.getPrice();
    }

    private String resolveSelectedCourseName() {
        if (selectedCourseId == null) {
            return getApplication().getString(R.string.all_courses);
        }
        for (Course course : allCourses) {
            if (selectedCourseId.equals(course.getId())) {
                return hasValue(course.getTitle()) ? course.getTitle() : getApplication().getString(R.string.untitled_course);
            }
        }
        return getApplication().getString(R.string.all_courses);
    }

    private void ensureSelectedCourseExists() {
        if (selectedCourseId == null || allCourses == null || allCourses.isEmpty()) {
            return;
        }
        for (Course course : allCourses) {
            if (selectedCourseId.equals(course.getId())) {
                return;
            }
        }
        selectedCourseId = null;
    }

    private void publishEmptyState() {
        allCourses = new ArrayList<>();
        allEnrollments = new ArrayList<>();
        barEntries.setValue(new ArrayList<>());
        pieEntries.setValue(new ArrayList<>());
        recentTransactions.setValue(new ArrayList<>());
        courses.setValue(new ArrayList<>());
        totalRevenue.setValue("$0.00");
        totalTransactions.setValue("0");
        averageTransactionValue.setValue("$0.00");
        selectedCourseName.setValue(getApplication().getString(R.string.all_courses));
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public LiveData<List<BarEntry>> getBarEntries() { return barEntries; }
    public LiveData<List<PieEntry>> getPieEntries() { return pieEntries; }
    public LiveData<List<Enrollment>> getRecentTransactions() { return recentTransactions; }
    public LiveData<List<Course>> getCourses() { return courses; }
    public LiveData<String> getTotalRevenue() { return totalRevenue; }
    public LiveData<String> getTotalTransactions() { return totalTransactions; }
    public LiveData<String> getAverageTransactionValue() { return averageTransactionValue; }
    public LiveData<String> getSelectedCourseName() { return selectedCourseName; }
    public String getSelectedCourseIdValue() { return selectedCourseId; }
}
