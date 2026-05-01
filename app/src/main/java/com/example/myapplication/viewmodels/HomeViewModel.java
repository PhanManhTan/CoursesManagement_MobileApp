package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.MockData;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Course>> featuredCourses = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        loadMockData();
    }

    private void loadMockData() {
        categories.setValue(MockData.getCategories());
        featuredCourses.setValue(MockData.getCourses());
    }

    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<List<Course>> getFeaturedCourses() { return featuredCourses; }
}
