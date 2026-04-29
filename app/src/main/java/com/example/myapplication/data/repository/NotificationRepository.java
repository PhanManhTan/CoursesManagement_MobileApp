package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.NotificationApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {
    private final NotificationApi notificationApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public NotificationRepository(Context context) {
        // Initialize NotificationApi using RetrofitClient
        this.notificationApi = RetrofitClient.getClient(context).create(NotificationApi.class);
    }

    // Get all Notifications from remote
    public void getAll(RepositoryCallback<List<Notification>> callback) {
        notificationApi.getAll().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Notification by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Notification> callback) {
        notificationApi.getById(id).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Notification not found");
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Notification to remote
    public void insert(Notification notification, RepositoryCallback<Void> callback) {
        notificationApi.insert(notification).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Update Notification by ID on remote
    public void update(String id, Notification notification, RepositoryCallback<Void> callback) {
        notificationApi.update(id, notification).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Delete Notification by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        notificationApi.delete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
