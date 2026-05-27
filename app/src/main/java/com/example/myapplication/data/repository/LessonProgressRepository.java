package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.LessonProgressApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.LessonProgress;
import com.example.myapplication.utils.ApiErrorFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonProgressRepository {
    private final LessonProgressApi lessonProgressApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public LessonProgressRepository(Context context) {
        this.lessonProgressApi = RetrofitClient.getClient(context).create(LessonProgressApi.class);
    }

    public void getAll(RepositoryCallback<List<LessonProgress>> callback) {
        lessonProgressApi.getAll().enqueue(new Callback<List<LessonProgress>>() {
            @Override
            public void onResponse(Call<List<LessonProgress>> call, Response<List<LessonProgress>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError(getErrorMessage(response));
            }
            @Override
            public void onFailure(Call<List<LessonProgress>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getById(String id, RepositoryCallback<LessonProgress> callback) {
        lessonProgressApi.getById("eq." + id).enqueue(new Callback<List<LessonProgress>>() {
            @Override
            public void onResponse(Call<List<LessonProgress>> call, Response<List<LessonProgress>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("LessonProgress not found");
                }
            }
            @Override
            public void onFailure(Call<List<LessonProgress>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getByUserIdAndLessonId(String userId, String lessonId, RepositoryCallback<LessonProgress> callback) {
        lessonProgressApi.getByUserIdAndLessonId("eq." + userId, "eq." + lessonId).enqueue(new Callback<List<LessonProgress>>() {
            @Override
            public void onResponse(Call<List<LessonProgress>> call, Response<List<LessonProgress>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("LessonProgress not found");
                }
            }
            @Override
            public void onFailure(Call<List<LessonProgress>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void insert(LessonProgress lessonProgress, RepositoryCallback<Void> callback) {
        lessonProgressApi.insert(lessonProgress).enqueue(new Callback<Void>() {
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

    public void insertAndReturn(LessonProgress lessonProgress, RepositoryCallback<LessonProgress> callback) {
        lessonProgressApi.insertAndReturn("return=representation", createWritePayload(lessonProgress)).enqueue(new Callback<List<LessonProgress>>() {
            @Override
            public void onResponse(Call<List<LessonProgress>> call, Response<List<LessonProgress>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else if (response.isSuccessful()) {
                    callback.onError("LessonProgress was created but no data returned");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }
            @Override
            public void onFailure(Call<List<LessonProgress>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void update(String id, LessonProgress lessonProgress, RepositoryCallback<Void> callback) {
        lessonProgressApi.updateById("eq." + id, createWritePayload(lessonProgress)).enqueue(new Callback<Void>() {
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
        lessonProgressApi.deleteById("eq." + id).enqueue(new Callback<Void>() {
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

    private Map<String, Object> createWritePayload(LessonProgress lessonProgress) {
        Map<String, Object> payload = new HashMap<>();
        if (lessonProgress == null) return payload;

        payload.put("user_id", lessonProgress.getUserId());
        payload.put("lesson_id", lessonProgress.getLessonId());
        payload.put("is_completed", lessonProgress.isCompleted());
        payload.put("watch_time_seconds", lessonProgress.getWatchTimeSeconds());

        if (lessonProgress.getCompletedAt() != null) {
            payload.put("completed_at", lessonProgress.getCompletedAt());
        }
        return payload;
    }

    private String getErrorMessage(Response<?> response) {
        return ApiErrorFormatter.fromResponse(response);
    }
}