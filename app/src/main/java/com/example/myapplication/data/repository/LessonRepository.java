package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.LessonApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Lesson;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonRepository {
    private final LessonApi lessonApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public LessonRepository(Context context) {
        // Initialize LessonApi using RetrofitClient
        this.lessonApi = RetrofitClient.getClient(context).create(LessonApi.class);
    }

    // Get all Lessons from remote
    public void getAll(RepositoryCallback<List<Lesson>> callback) {
        lessonApi.getAll().enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Lesson by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Lesson> callback) {
        lessonApi.getById(id).enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Lesson not found");
                }
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Lesson to remote
    public void insert(Lesson lesson, RepositoryCallback<Void> callback) {
        lessonApi.insert(lesson).enqueue(new Callback<Void>() {
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

    // Update Lesson by ID on remote
    public void update(String id, Lesson lesson, RepositoryCallback<Void> callback) {
        lessonApi.update(id, lesson).enqueue(new Callback<Void>() {
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

    // Delete Lesson by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        lessonApi.delete(id).enqueue(new Callback<Void>() {
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
