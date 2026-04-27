package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

import com.example.myapplication.utils.MockData;

public class StudentListViewModel extends ViewModel {
    private final MutableLiveData<List<User>> students = new MutableLiveData<>();
    private final List<User> allStudents = new ArrayList<>();

    public StudentListViewModel() {
        // Use MockData and filter by Student role
        List<User> users = MockData.getUsers();
        for (User u : users) {
            if ("Student".equalsIgnoreCase(u.getRole())) {
                allStudents.add(u);
            }
        }
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
                if (user.getFullName().toLowerCase().contains(lowerQuery) ||
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filtered.add(user);
                }
            }
            students.setValue(filtered);
        }
    }
}
