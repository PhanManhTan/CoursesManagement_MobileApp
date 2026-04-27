package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.MockData;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Course>> featuredCourses = new MutableLiveData<>();

    public HomeViewModel() {
        loadData();
    }

    private void loadData() {
        categories.setValue(MockData.getCategories());
        featuredCourses.setValue(MockData.getCourses());
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Course>> getFeaturedCourses() {
        return featuredCourses;
    }
}
