package com.example.myapplication.data.repository;

import android.content.Context;

import com.example.myapplication.data.remote.ChapterApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Chapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterRepository {

    private final ChapterApi chapterApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public ChapterRepository(Context context) {
        this.chapterApi = RetrofitClient
                .getClient(context)
                .create(ChapterApi.class);
    }

    // Get all Chapters
    public void getAll(RepositoryCallback<List<Chapter>> callback) {
        chapterApi.getAll().enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get by ID
    public void getById(String id, RepositoryCallback<Chapter> callback) {
        chapterApi.getById(id).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Chapter not found");
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert
    public void insert(Chapter chapter, RepositoryCallback<Void> callback) {
        chapterApi.insert(chapter).enqueue(new Callback<Void>() {
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

    // Update
    public void update(String id, Chapter chapter, RepositoryCallback<Void> callback) {
        chapterApi.update(id, chapter).enqueue(new Callback<Void>() {
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

    // Delete
    public void delete(String id, RepositoryCallback<Void> callback) {
        chapterApi.delete(id).enqueue(new Callback<Void>() {
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