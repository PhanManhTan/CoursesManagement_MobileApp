package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.StudentCourseProgress;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StudentListViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final String instructorId;

    private final MutableLiveData<List<StudentCourseProgress>> students = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> totalEnrollments = new MutableLiveData<>("0");
    private final MutableLiveData<String> uniqueStudents = new MutableLiveData<>("0");
    private final MutableLiveData<String> averageProgress = new MutableLiveData<>("0%");
    private final MutableLiveData<String> completedEnrollments = new MutableLiveData<>("0");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private List<StudentCourseProgress> allStudentProgress = new ArrayList<>();
    private String selectedCourseId;
    private String searchQuery = "";

    public StudentListViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        
        SessionManager sessionManager = new SessionManager(application);
        this.instructorId = sessionManager.getUserId();
        
        fetchStudents();
    }

    public void fetchStudents() {
        if (instructorId == null) {
            publishEmptyState();
            errorMessage.setValue(getApplication().getString(R.string.missing_instructor_session));
            return;
        }

        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> instructorCourses) {
                List<Course> safeCourses = instructorCourses != null ? instructorCourses : new ArrayList<>();
                courses.setValue(safeCourses);
                ensureSelectedCourseExists(safeCourses);
                if (!safeCourses.isEmpty()) {
                    fetchEnrollmentsForCourses(safeCourses);
                } else {
                    publishEmptyState();
                }
            }
            @Override public void onError(String message) {
                publishEmptyState();
                errorMessage.setValue(message);
            }
        });
    }

    private void fetchEnrollmentsForCourses(List<Course> instructorCourses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                Map<String, Course> courseById = new HashMap<>();
                for (Course course : instructorCourses) {
                    if (course.getId() != null) {
                        courseById.put(course.getId(), course);
                    }
                }
                fetchUserProfiles(courseById, allEnrollments != null ? allEnrollments : new ArrayList<>());
            }
            @Override public void onError(String message) {
                publishEmptyState();
                errorMessage.setValue(message);
            }
        });
    }

    private void fetchUserProfiles(Map<String, Course> courseById, List<Enrollment> allEnrollments) {
        userRepository.getAll(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> allUsers) {
                Map<String, User> userById = new HashMap<>();
                if (allUsers != null) {
                    for (User user : allUsers) {
                        if (user.getId() != null) {
                            userById.put(user.getId(), user);
                        }
                    }
                }

                List<StudentCourseProgress> progressRows = new ArrayList<>();
                for (Enrollment enrollment : allEnrollments) {
                    if (enrollment.getCourseId() == null || enrollment.getUserId() == null) {
                        continue;
                    }
                    Course course = courseById.get(enrollment.getCourseId());
                    User student = userById.get(enrollment.getUserId());
                    if (course == null || student == null) {
                        continue;
                    }
                    if (enrollment.getCourse() == null) {
                        enrollment.setCourse(course);
                    }
                    progressRows.add(new StudentCourseProgress(student, course, enrollment));
                }

                Collections.sort(progressRows, (first, second) -> {
                    String firstCourse = safeLower(first.getCourse() != null ? first.getCourse().getTitle() : "");
                    String secondCourse = safeLower(second.getCourse() != null ? second.getCourse().getTitle() : "");
                    int courseCompare = firstCourse.compareTo(secondCourse);
                    if (courseCompare != 0) return courseCompare;

                    String firstName = safeLower(first.getStudent() != null ? first.getStudent().getFullName() : "");
                    String secondName = safeLower(second.getStudent() != null ? second.getStudent().getFullName() : "");
                    return firstName.compareTo(secondName);
                });

                allStudentProgress = progressRows;
                applyFilters();
            }
            @Override public void onError(String message) {
                publishEmptyState();
                errorMessage.setValue(message);
            }
        });
    }

    public void searchStudents(String query) {
        searchQuery = query != null ? query.trim() : "";
        applyFilters();
    }

    public void setCourseFilter(String courseId) {
        selectedCourseId = hasValue(courseId) ? courseId : null;
        applyFilters();
    }

    private void applyFilters() {
        List<StudentCourseProgress> filtered = new ArrayList<>();
        String lowerQuery = safeLower(searchQuery);

        for (StudentCourseProgress row : allStudentProgress) {
            Course course = row.getCourse();
            User student = row.getStudent();
            if (selectedCourseId != null && (course == null || !selectedCourseId.equals(course.getId()))) {
                continue;
            }
            if (!lowerQuery.isEmpty() && !matchesQuery(row, lowerQuery)) {
                continue;
            }
            filtered.add(row);
        }

        students.setValue(filtered);
        updateSummary(filtered);
    }

    private boolean matchesQuery(StudentCourseProgress row, String lowerQuery) {
        User student = row.getStudent();
        Course course = row.getCourse();
        return containsLower(student != null ? student.getFullName() : null, lowerQuery)
                || containsLower(student != null ? student.getEmail() : null, lowerQuery)
                || containsLower(course != null ? course.getTitle() : null, lowerQuery);
    }

    private void updateSummary(List<StudentCourseProgress> visibleRows) {
        Set<String> uniqueStudentIds = new HashSet<>();
        int completed = 0;
        int progressTotal = 0;

        for (StudentCourseProgress row : visibleRows) {
            if (row.getStudent() != null && row.getStudent().getId() != null) {
                uniqueStudentIds.add(row.getStudent().getId());
            }
            if (row.isCompleted()) {
                completed++;
            }
            progressTotal += row.getProgressPercent();
        }

        int average = visibleRows.isEmpty() ? 0 : Math.round(progressTotal / (float) visibleRows.size());
        totalEnrollments.setValue(String.valueOf(visibleRows.size()));
        uniqueStudents.setValue(String.valueOf(uniqueStudentIds.size()));
        averageProgress.setValue(average + "%");
        completedEnrollments.setValue(String.valueOf(completed));
    }

    private void publishEmptyState() {
        allStudentProgress = new ArrayList<>();
        students.setValue(new ArrayList<>());
        updateSummary(new ArrayList<>());
    }

    private void ensureSelectedCourseExists(List<Course> safeCourses) {
        if (selectedCourseId == null) return;
        for (Course course : safeCourses) {
            if (selectedCourseId.equals(course.getId())) {
                return;
            }
        }
        selectedCourseId = null;
    }

    private boolean containsLower(String value, String lowerQuery) {
        return value != null && safeLower(value).contains(lowerQuery);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US);
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public String getSelectedCourseIdValue() { return selectedCourseId; }
    public LiveData<List<StudentCourseProgress>> getStudents() { return students; }
    public LiveData<List<Course>> getCourses() { return courses; }
    public LiveData<String> getTotalEnrollments() { return totalEnrollments; }
    public LiveData<String> getUniqueStudents() { return uniqueStudents; }
    public LiveData<String> getAverageProgress() { return averageProgress; }
    public LiveData<String> getCompletedEnrollments() { return completedEnrollments; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
