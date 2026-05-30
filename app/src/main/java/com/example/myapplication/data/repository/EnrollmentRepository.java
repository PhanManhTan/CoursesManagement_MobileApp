package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.EnrollmentApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Enrollment;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnrollmentRepository {
    private final EnrollmentApi enrollmentApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public EnrollmentRepository(Context context) {
        this.enrollmentApi = RetrofitClient.getClient(context).create(EnrollmentApi.class);
    }

    // Get all enrollments with nested course data
    public void getAll(RepositoryCallback<List<Enrollment>> callback) {
        enrollmentApi.getAll().enqueue(new Callback<List<Enrollment>>() {
            @Override
            public void onResponse(Call<List<Enrollment>> call, Response<List<Enrollment>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override
            public void onFailure(Call<List<Enrollment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get enrollments for the current student
    public void getByUserId(String userId, RepositoryCallback<List<Enrollment>> callback) {
        enrollmentApi.getByUserId("eq." + userId).enqueue(new Callback<List<Enrollment>>() {
            @Override
            public void onResponse(Call<List<Enrollment>> call, Response<List<Enrollment>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override
            public void onFailure(Call<List<Enrollment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void insert(Enrollment enrollment, RepositoryCallback<Void> callback) {
        // CHỈ GỬI CÁC TRƯỜNG CƠ BẢN LÊN SUPABASE (Tránh lỗi dư thừa cột)
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("user_id", enrollment.getUserId());
        payload.put("course_id", enrollment.getCourseId());

        // Gửi số tiền thanh toán nếu bảng của bạn có cột này
        // payload.put("paid_amount", enrollment.getPaidAmount());

        enrollmentApi.insert(payload).enqueue(new Callback<Void>() {
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

    public void update(String id, Enrollment enrollment, RepositoryCallback<Void> callback) {
        enrollmentApi.update("eq." + id, enrollment).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void delete(String id, RepositoryCallback<Void> callback) {
        enrollmentApi.delete("eq." + id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void checkEnrollment(String userId, String courseId, RepositoryCallback<Boolean> callback) {
        enrollmentApi.checkEnrollment("eq." + userId, "eq." + courseId).enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(!response.body().isEmpty());
                } else {
                    callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
