package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.models.Course;
import java.util.List;

public class CourseApprovalViewModel extends AndroidViewModel {
    private final CourseRepository repository;
    private final MutableLiveData<List<Course>> pendingCourses = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CourseApprovalViewModel(@NonNull Application application) {
        super(application);
        repository = new CourseRepository(application);
        fetchPendingCourses();
    }

    public void fetchPendingCourses() {
        repository.getByStatus("pending", new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> data) {
                pendingCourses.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void approveCourse(Course course) {
        course.setStatus("approved");
        repository.update(course.getId(), course, new CourseRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                fetchPendingCourses(); // Refresh list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void rejectCourse(Course course) {
        course.setStatus("rejected");
        repository.update(course.getId(), course, new CourseRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                notifyInstructorCourseRejected(course);
                fetchPendingCourses(); // Refresh list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    private void notifyInstructorCourseRejected(Course course) {
        if (course == null || !hasValue(course.getInstructorId())) {
            return;
        }

        String courseTitle = hasValue(course.getTitle())
                ? course.getTitle().trim()
                : getApplication().getString(R.string.untitled_course);
        Notification notification = new Notification(
                null,
                course.getInstructorId(),
                getApplication().getString(R.string.notification_course_rejected_title),
                getApplication().getString(R.string.notification_course_rejected_body, courseTitle),
                false,
                null
        );

        notificationRepository.insert(notification, new NotificationRepository.RepositoryCallback<Void>() {
            @Override public void onSuccess(Void data) {}

            @Override
            public void onError(String message) {
                errorMessage.setValue(getApplication().getString(R.string.notification_create_failed, message));
            }
        });
    }

    public LiveData<List<Course>> getPendingCourses() {
        return pendingCourses;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
