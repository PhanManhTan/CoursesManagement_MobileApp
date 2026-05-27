package com.example.myapplication.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Course>> featuredCourses = new MutableLiveData<>();
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        courseRepository = new CourseRepository(application);
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

        courseRepository.getAll(new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> data) {
                featuredCourses.postValue(data);
            }

            @Override
            public void onError(String message) {
                Log.e("HomeViewModel", message);
            }
        });
    }

    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<List<Course>> getFeaturedCourses() { return featuredCourses; }
}