package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

public class StudentListViewModel extends ViewModel {
    private final MutableLiveData<List<User>> students = new MutableLiveData<>();

    public StudentListViewModel() {
        // Mock data
        List<User> mockList = new ArrayList<>();
        mockList.add(new User("s1", "Nguyen Van A", "a@example.com", "Student"));
        mockList.add(new User("s2", "Tran Thi B", "b@example.com", "Student"));
        mockList.add(new User("s3", "Le Van C", "c@example.com", "Student"));
        mockList.add(new User("s4", "Phan Thi D", "d@example.com", "Student"));
        students.setValue(mockList);
    }

    public LiveData<List<User>> getStudents() {
        return students;
    }

    public void searchStudents(String query) {
        // Implement search logic if needed
    }
}
