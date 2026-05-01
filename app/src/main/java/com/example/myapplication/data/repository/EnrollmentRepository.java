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
        // Initialize EnrollmentApi using RetrofitClient
        this.enrollmentApi = RetrofitClient.getClient(context).create(EnrollmentApi.class);
    }

    // Get all Enrollments from remote
    public void getAll(RepositoryCallback<List<Enrollment>> callback) {
        enrollmentApi.getAll().enqueue(new Callback<List<Enrollment>>() {
            @Override
            public void onResponse(Call<List<Enrollment>> call, Response<List<Enrollment>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Enrollment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Enrollment by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Enrollment> callback) {
        enrollmentApi.getById("eq." + id).enqueue(new Callback<List<Enrollment>>() {
            @Override
            public void onResponse(Call<List<Enrollment>> call, Response<List<Enrollment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Enrollment not found");
                }
            }

            @Override
            public void onFailure(Call<List<Enrollment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Enrollment to remote
    public void insert(Enrollment enrollment, RepositoryCallback<Void> callback) {
        enrollmentApi.insert(enrollment).enqueue(new Callback<Void>() {
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

    // Update Enrollment by ID on remote
    public void update(String id, Enrollment enrollment, RepositoryCallback<Void> callback) {
        enrollmentApi.update("eq." + id, enrollment).enqueue(new Callback<Void>() {
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

    // Delete Enrollment by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        enrollmentApi.delete("eq." + id).enqueue(new Callback<Void>() {
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
