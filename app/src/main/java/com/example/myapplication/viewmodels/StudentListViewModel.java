package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

public class StudentListViewModel extends ViewModel {
    private final MutableLiveData<List<User>> students = new MutableLiveData<>();
    private final List<User> allStudents = new ArrayList<>();

    public StudentListViewModel() {
        // Mock data
        allStudents.add(new User("s1", "Nguyen Van A", "a@example.com", "Student"));
        allStudents.add(new User("s2", "Tran Thi B", "b@example.com", "Student"));
        allStudents.add(new User("s3", "Le Van C", "c@example.com", "Student"));
        allStudents.add(new User("s4", "Phan Thi D", "d@example.com", "Student"));
        students.setValue(new ArrayList<>(allStudents));
    }

    public LiveData<List<User>> getStudents() {
        return students;
    }

    // Searches the mock list by name or email
    public void searchStudents(String query) {
        if (query == null || query.isEmpty()) {
            students.setValue(new ArrayList<>(allStudents));
        } else {
            List<User> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (User user : allStudents) {
                if (user.getName().toLowerCase().contains(lowerQuery) || 
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filtered.add(user);
                }
            }
            students.setValue(filtered);
        }
    }
}
