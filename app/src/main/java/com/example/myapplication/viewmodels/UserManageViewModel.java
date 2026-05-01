package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserManageViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private List<User> allUsersList = new ArrayList<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserManageViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        fetchUsers();
    }

    public void fetchUsers() {
        repository.getAll(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                if (data != null) {
                    allUsersList = data;
                    users.setValue(data);
                } else {
                    users.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void filterUsers(String role) {
        if (role == null || role.equalsIgnoreCase("All")) {
            users.setValue(new ArrayList<>(allUsersList));
        } else {
            List<User> filtered = new ArrayList<>();
            for (User user : allUsersList) {
                if (user.getRole() != null && user.getRole().equalsIgnoreCase(role)) {
                    filtered.add(user);
                }
            }
            users.setValue(filtered);
        }
    }

    public void searchUsers(String query) {
        if (query == null || query.isEmpty()) {
            users.setValue(new ArrayList<>(allUsersList));
        } else {
            List<User> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (User user : allUsersList) {
                boolean matchesName = user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerQuery);
                boolean matchesEmail = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);
                if (matchesName || matchesEmail) {
                    filtered.add(user);
                }
            }
            users.setValue(filtered);
        }
    }

    public void toggleBanUser(User user) {
        String newStatus = "banned".equalsIgnoreCase(user.getStatus()) ? "active" : "banned";
        user.setStatus(newStatus);
        repository.update(user.getId(), user, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                fetchUsers(); // Refresh list
            }
            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }
}
