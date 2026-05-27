package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.LessonApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.utils.ApiErrorFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        this.lessonApi = RetrofitClient.getClient(context).create(LessonApi.class);
    }

    // Get all Lessons
    public void getAll(RepositoryCallback<List<Lesson>> callback) {
        lessonApi.getAll().enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError(getErrorMessage(response));
            }
            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Lessons by Chapter ID
    public void getByChapterId(String chapterId, RepositoryCallback<List<Lesson>> callback) {
        lessonApi.getByChapterId("eq." + chapterId).enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError(getErrorMessage(response));
            }
            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getById(String id, RepositoryCallback<Lesson> callback) {
        lessonApi.getById("eq." + id).enqueue(new Callback<List<Lesson>>() {
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

    public void insert(Lesson lesson, RepositoryCallback<Void> callback) {
        lessonApi.insert(lesson).enqueue(new Callback<Void>() {
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

    public void insertAndReturn(Lesson lesson, RepositoryCallback<Lesson> callback) {
        lessonApi.insertAndReturn("return=representation", createWritePayload(lesson)).enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else if (response.isSuccessful()) {
                    callback.onError("Lesson was created but no lesson id was returned");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void update(String id, Lesson lesson, RepositoryCallback<Void> callback) {
        lessonApi.updateById("eq." + id, createWritePayload(lesson)).enqueue(new Callback<Void>() {
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
        lessonApi.deleteById("eq." + id).enqueue(new Callback<Void>() {
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

    private Map<String, Object> createWritePayload(Lesson lesson) {
        Map<String, Object> payload = new HashMap<>();
        if (lesson == null) return payload;

        payload.put("chapter_id", lesson.getChapterId());
        payload.put("title", hasValue(lesson.getTitle()) ? lesson.getTitle() : "Untitled lesson");
        payload.put("content_type", resolveWriteContentType(lesson));
        payload.put("video_url", hasValue(lesson.getVideoUrl()) ? lesson.getVideoUrl() : null);
        payload.put("document_url", hasValue(lesson.getDocumentUrl()) ? lesson.getDocumentUrl() : null);
        payload.put("order_index", lesson.getOrderIndex() > 0 ? lesson.getOrderIndex() : 1);
        payload.put("duration_seconds", lesson.getDurationSeconds());
        payload.put("content", lesson.getContent());
        return payload;
    }

    private String resolveWriteContentType(Lesson lesson) {
        if (hasValue(lesson.getVideoUrl())) {
            return "video";
        }
        if (hasValue(lesson.getDocumentUrl())) {
            return "document";
        }
        String contentType = lesson.getContentType();
        if ("document".equalsIgnoreCase(contentType)) {
            return "document";
        }
        return "video";
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String getErrorMessage(Response<?> response) {
        return ApiErrorFormatter.fromResponse(response);
    }
}
