package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditCourseViewModel extends AndroidViewModel {
    private final CourseRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<Course> course = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public EditCourseViewModel(@NonNull Application application) {
        super(application);
        repository = new CourseRepository(application);
        sessionManager = new SessionManager(application);
    }

    public void setCourse(Course courseData) {
        course.setValue(courseData);
    }

    public LiveData<Course> getCourse() {
        return course;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void saveCourse(String title, String description, double price, String thumbnailUrl) {
        Course current = course.getValue();
        if (current != null) {
            current.setTitle(title);
            current.setDescription(description);
            current.setPrice(price);
            current.setThumbnailUrl(thumbnailUrl);
            
            CourseRepository.RepositoryCallback<Void> callback = new CourseRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveSuccess.setValue(true);
                }

                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                }
            };

            if (current.getId() != null && !current.getId().equals("tmp")) {
                repository.update(current.getId(), current, callback);
            } else {
                // Set default values for new course
                current.setId(null); 
                current.setInstructorId(sessionManager.getUserId());
                current.setStatus("pending");
                current.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
                
                repository.insert(current, callback);
            }
        }
    }
}
