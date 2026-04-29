package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.CourseApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Course;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseRepository {
    private final CourseApi courseApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public CourseRepository(Context context) {
        // Initialize CourseApi using RetrofitClient
        this.courseApi = RetrofitClient.getClient(context).create(CourseApi.class);
    }

    // Get all Courses from remote
    public void getAll(RepositoryCallback<List<Course>> callback) {
        courseApi.getAll().enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Course by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Course> callback) {
        courseApi.getById(id).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Course not found");
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Course to remote
    public void insert(Course course, RepositoryCallback<Void> callback) {
        courseApi.insert(course).enqueue(new Callback<Void>() {
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

    // Update Course by ID on remote
    public void update(String id, Course course, RepositoryCallback<Void> callback) {
        courseApi.update(id, course).enqueue(new Callback<Void>() {
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

    // Delete Course by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        courseApi.delete(id).enqueue(new Callback<Void>() {
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
