package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.MockData;
import java.util.ArrayList;
import java.util.List;

public class UserManageViewModel extends ViewModel {
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final List<User> allUsers;

    public UserManageViewModel() {
        allUsers = MockData.getUsers();
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
