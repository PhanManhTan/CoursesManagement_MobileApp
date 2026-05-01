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
        this.notificationApi = RetrofitClient.getClient(context).create(NotificationApi.class);
    }

    public void getAll(RepositoryCallback<List<Notification>> callback) {
        notificationApi.getAll().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<List<Notification>> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void getById(String id, RepositoryCallback<Notification> callback) {
        notificationApi.getById(id).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Notification> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void insert(Notification notification, RepositoryCallback<Void> callback) {
        notificationApi.insert(notification).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void update(String id, Notification notification, RepositoryCallback<Void> callback) {
        notificationApi.update(id, notification).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void delete(String id, RepositoryCallback<Void> callback) {
        notificationApi.delete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }
}
