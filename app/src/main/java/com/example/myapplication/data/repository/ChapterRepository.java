package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.ChapterApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.utils.ApiErrorFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        this.chapterApi = RetrofitClient.getClient(context).create(ChapterApi.class);
    }

    // Get all Chapters
    public void getAll(RepositoryCallback<List<Chapter>> callback) {
        chapterApi.getAll().enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }
            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Chapters by Course ID
    public void getByCourseId(String courseId, RepositoryCallback<List<Chapter>> callback) {
        chapterApi.getByCourseId("eq." + courseId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }
            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getById(String id, RepositoryCallback<Chapter> callback) {
        chapterApi.getById("eq." + id).enqueue(new Callback<List<Chapter>>() {
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

    public void insert(Chapter chapter, RepositoryCallback<Void> callback) {
        chapterApi.insert(chapter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(getErrorMessage(response));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void insertAndReturn(Chapter chapter, RepositoryCallback<Chapter> callback) {
        chapterApi.insertAndReturn("return=representation", createWritePayload(chapter)).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else if (response.isSuccessful()) {
                    callback.onError("Chapter was created but no chapter id was returned");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void update(String id, Chapter chapter, RepositoryCallback<Void> callback) {
        chapterApi.update("eq." + id, createWritePayload(chapter)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(getErrorMessage(response));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void delete(String id, RepositoryCallback<Void> callback) {
        chapterApi.delete("eq." + id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(getErrorMessage(response));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private Map<String, Object> createWritePayload(Chapter chapter) {
        Map<String, Object> payload = new HashMap<>();
        if (chapter == null) return payload;

        payload.put("course_id", chapter.getCourseId());
        payload.put("title", chapter.getTitle());
        payload.put("order_index", chapter.getOrderIndex());
        return payload;
    }

    private String getErrorMessage(Response<?> response) {
        return ApiErrorFormatter.fromResponse(response);
    }
}
