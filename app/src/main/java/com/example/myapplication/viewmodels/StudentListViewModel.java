package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentListViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final String instructorId;

    private final MutableLiveData<List<User>> students = new MutableLiveData<>();
    private List<User> allMyStudents = new ArrayList<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

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
        if (instructorId == null) return;

        // 1. Get Instructor's Courses
        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> instructorCourses) {
                if (instructorCourses != null && !instructorCourses.isEmpty()) {
                    fetchEnrollmentsForCourses(instructorCourses);
                } else {
                    students.setValue(new ArrayList<>());
                }
            }
            @Override public void onError(String message) { errorMessage.setValue(message); }
        });
    }

    private void fetchEnrollmentsForCourses(List<Course> instructorCourses) {
        // 2. Get All Enrollments
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                Set<String> studentIds = new HashSet<>();
                if (allEnrollments != null) {
                    for (Course course : instructorCourses) {
                        for (Enrollment e : allEnrollments) {
                            if (e.getCourseId() != null && e.getCourseId().equals(course.getId())) {
                                studentIds.add(e.getUserId());
                            }
                        }
                    }
                }
                fetchUserProfiles(studentIds);
            }
            @Override public void onError(String message) { errorMessage.setValue(message); }
        });
    }

    private void fetchUserProfiles(Set<String> studentIds) {
        // 3. Get All Users and filter by our enrolled student IDs
        userRepository.getAll(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> allUsers) {
                List<User> myStudents = new ArrayList<>();
                if (allUsers != null) {
                    for (User user : allUsers) {
                        if (studentIds.contains(user.getId())) {
                            myStudents.add(user);
                        }
                    }
                }
                allMyStudents = myStudents;
                students.setValue(myStudents);
            }
            @Override public void onError(String message) { errorMessage.setValue(message); }
        });
    }

    public void searchStudents(String query) {
        if (query == null || query.isEmpty()) {
            students.setValue(new ArrayList<>(allMyStudents));
        } else {
            List<User> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (User user : allMyStudents) {
                if ((user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerQuery)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                    filtered.add(user);
                }
            }
            students.setValue(filtered);
        }
    }

    public LiveData<List<User>> getStudents() { return students; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
