package com.example.myapplication.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserManageViewModel extends ViewModel {
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();

    public UserManageViewModel() {
        // Mock data
        List<User> mockList = new ArrayList<>();
        mockList.add(new User("u1", "Phan Manh Tan", "tan@example.com", "Admin"));
        mockList.add(new User("u2", "Nguyen Van Son", "son@example.com", "Instructor"));
        mockList.add(new User("u3", "Huynh Minh Qui", "qui@example.com", "Instructor"));
        mockList.add(new User("u4", "Quang Pham", "quang@example.com", "Student"));
        users.setValue(mockList);
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }
}
