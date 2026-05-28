package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.remote.UserApi;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.ApiErrorFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public UserRepository(Context context) {
        // Initialize UserApi using RetrofitClient
        this.userApi = RetrofitClient.getClient(context).create(UserApi.class);
    }

    // Get all Users from remote
    public void getAll(RepositoryCallback<List<User>> callback) {
        userApi.getAll().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get User by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<User> callback) {
        userApi.getById("eq." + id).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else if (response.isSuccessful()) {
                    callback.onError("User not found");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new User to remote
    public void insert(User user, RepositoryCallback<Void> callback) {
        userApi.insert(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Update User by ID on remote
    public void update(String id, User user, RepositoryCallback<Void> callback) {
        userApi.update("eq." + id, user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateProfile(String id, String fullName, String bio, RepositoryCallback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("full_name", fullName);
        fields.put("bio", bio);
        userApi.updateFields("eq." + id, fields).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateAvatar(String id, String avatarUrl, RepositoryCallback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("avatar_url", avatarUrl);
        userApi.updateFields("eq." + id, fields).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Delete User by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        userApi.delete("eq." + id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private String getErrorMessage(Response<?> response) {
        return ApiErrorFormatter.fromResponse(response);
    }
}
