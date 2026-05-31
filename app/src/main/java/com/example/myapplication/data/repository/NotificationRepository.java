package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.NotificationApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Lấy thông báo theo User ID
    public void getByUserId(String userId, RepositoryCallback<List<Notification>> callback) {
        notificationApi.getByUserId("eq." + userId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<List<Notification>> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    // Lấy thông báo chưa đọc
    public void getUnreadNotifications(String userId, RepositoryCallback<List<Notification>> callback) {
        notificationApi.getUnreadByUserId("eq." + userId, "eq.false").enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<List<Notification>> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    // Tạo thông báo mới (Chỉ gửi các trường cần thiết)
    public void insert(Notification notification, RepositoryCallback<Void> callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", notification.getUserId());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("is_read", false);

        notificationApi.insert(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    // Cập nhật trạng thái đã đọc bằng Map
    public void markAsRead(String notificationId, RepositoryCallback<Void> callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("is_read", true);
        notificationApi.updatePartial("eq." + notificationId, payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    // Cập nhật nguyên object (từ branch cũ)
    public void update(String id, Notification notification, RepositoryCallback<Void> callback) {
        notificationApi.update("eq." + id, notification).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void delete(String id, RepositoryCallback<Void> callback) {
        notificationApi.delete("eq." + id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }
}