package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserManageViewModel extends ViewModel {
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final List<User> allUsers = new ArrayList<>();

    public UserManageViewModel() {
        // Mock data
        allUsers.add(new User("u1", "Phan Manh Tan", "tan@example.com", "hash1", "https://via.placeholder.com/150", "Admin", "Bio for Tan", "2023-01-01"));
        allUsers.add(new User("u2", "Nguyen Van Son", "son@example.com", "hash2", "https://via.placeholder.com/150", "Instructor", "Bio for Son", "2023-02-01"));
        allUsers.add(new User("u3", "Huynh Minh Qui", "qui@example.com", "hash3", "https://via.placeholder.com/150", "Instructor", "Bio for Qui", "2023-03-01"));
        allUsers.add(new User("u4", "Quang Pham", "quang@example.com", "hash4", "https://via.placeholder.com/150", "Student", "Bio for Quang", "2023-04-01"));
        allUsers.add(new User("u5", "Le Van A", "a@example.com", "hash5", "https://via.placeholder.com/150", "Student", "Bio for A", "2023-05-01"));
        users.setValue(new ArrayList<>(allUsers));
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    // Filters the mock list based on role
    public void filterUsers(String role) {
        if (role == null || role.equals("All")) {
            users.setValue(new ArrayList<>(allUsers));
        } else {
            List<User> filtered = new ArrayList<>();
            for (User user : allUsers) {
                if (user.getRole().equalsIgnoreCase(role)) {
                    filtered.add(user);
                }
            }
            users.setValue(filtered);
        }
    }

    // Searches the mock list by name or email
    public void searchUsers(String query) {
        if (query == null || query.isEmpty()) {
            users.setValue(new ArrayList<>(allUsers));
        } else {
            List<User> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (User user : allUsers) {
                if (user.getFullName().toLowerCase().contains(lowerQuery) || 
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filtered.add(user);
                }
            }
            users.setValue(filtered);
        }
    }
}
