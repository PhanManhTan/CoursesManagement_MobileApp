package com.example.myapplication.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.data.remote.CourseApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.ApiErrorFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseRepository {
    private static final String TAG = "CourseRepository";
    private final CourseApi courseApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public CourseRepository(Context context) {
        courseApi = RetrofitClient.getClient(context).create(CourseApi.class);
    }

    // MODIFIED: Fetch only approved courses for general display (e.g., Home Activity)
    public void getAll(RepositoryCallback<List<Course>> callback) {
        Log.d(TAG, "Fetching all approved courses");
        courseApi.getByStatus("eq.approved").enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully fetched approved courses");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to fetch approved courses: " + getErrorMessage(response));
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e(TAG, "Error fetching approved courses: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // MODIFIED: Search only approved courses by title
    public void search(String query, RepositoryCallback<List<Course>> callback) {
        Log.d(TAG, "Searching approved courses with query: " + query);
        courseApi.search("ilike.*" + query + "*", "eq.approved").enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully searched approved courses");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to search approved courses: " + getErrorMessage(response));
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e(TAG, "Error searching approved courses: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // MODIFIED: Get only approved courses by category ID
    public void getByCategoryId(String categoryId, RepositoryCallback<List<Course>> callback) {
        Log.d(TAG, "Fetching approved courses for category ID: " + categoryId);
        courseApi.getByCategoryId("eq." + categoryId, "eq.approved").enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully fetched approved courses by category");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to fetch approved courses by category: " + getErrorMessage(response));
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e(TAG, "Error fetching approved courses by category: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // MODIFIED: Search only approved courses by category and title
    public void searchByCategoryAndTitle(String categoryId, String query, RepositoryCallback<List<Course>> callback) {
        Log.d(TAG, "Searching approved courses by category ID: " + categoryId + " and query: " + query);
        courseApi.searchByCategoryAndTitle("eq." + categoryId, "ilike.*" + query + "*", "eq.approved").enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully searched approved courses by category and title");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to search approved courses by category and title: " + getErrorMessage(response));
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e(TAG, "Error searching approved courses by category and title: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // UNMODIFIED: Instructor methods remain unchanged to allow them to see their unapproved courses
    public void getByInstructor(String instructorId, RepositoryCallback<List<Course>> callback) {
        courseApi.getByInstructor("eq." + instructorId).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getByStatus(String status, RepositoryCallback<List<Course>> callback) {
        courseApi.getByStatus("eq." + status).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getById(String id, RepositoryCallback<Course> callback) {
        courseApi.getById("eq." + id).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                handleSingleCourseResponse(response, callback, "Course not found");
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getByIdForInstructor(String id, String instructorId, RepositoryCallback<Course> callback) {
        courseApi.getByIdAndInstructor("eq." + id, "eq." + instructorId).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                handleSingleCourseResponse(response, callback, "Course not found or you do not have permission");
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void insert(Course course, RepositoryCallback<Void> callback) {
        courseApi.insert(createWritePayload(course)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void insertAndReturn(Course course, RepositoryCallback<Course> callback) {
        courseApi.insertAndReturn("return=representation", createWritePayload(course)).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else if (response.isSuccessful()) {
                    callback.onError("Course was created but no course id was returned");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void update(String id, Course course, RepositoryCallback<Void> callback) {
        courseApi.update("eq." + id, createWritePayload(course)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateForInstructor(String id, String instructorId, Course course, RepositoryCallback<Void> callback) {
        courseApi.updateByInstructor(
                "return=representation",
                "eq." + id,
                "eq." + instructorId,
                createWritePayload(course)
        ).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(null);
                } else if (response.isSuccessful()) {
                    callback.onError("Course not found or you do not have permission");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void delete(String id, RepositoryCallback<Void> callback) {
        courseApi.delete("eq." + id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteForInstructor(String id, String instructorId, RepositoryCallback<Void> callback) {
        courseApi.deleteByInstructor(
                "return=representation",
                "eq." + id,
                "eq." + instructorId
        ).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(null);
                } else if (response.isSuccessful()) {
                    callback.onError("Course not found or you do not have permission");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private void handleSingleCourseResponse(Response<List<Course>> response, RepositoryCallback<Course> callback, String emptyMessage) {
        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
            callback.onSuccess(response.body().get(0));
        } else if (response.isSuccessful()) {
            callback.onError(emptyMessage);
        } else {
            callback.onError(getErrorMessage(response));
        }
    }

    private String getErrorMessage(Response<?> response) {
        return ApiErrorFormatter.fromResponse(response);
    }

    private Map<String, Object> createWritePayload(Course course) {
        Map<String, Object> payload = new HashMap<>();
        if (course == null) return payload;

        putIfNotEmpty(payload, "instructor_id", course.getInstructorId());
        putIfNotEmpty(payload, "title", course.getTitle());
        putIfNotEmpty(payload, "description", course.getDescription());
        putIfNotEmpty(payload, "thumbnail_url", course.getThumbnailUrl());
        payload.put("price", course.getPrice());
        if (course.getDiscountPrice() > 0) {
            payload.put("discount_price", course.getDiscountPrice());
        }
        putIfNotEmpty(payload, "status", course.getStatus());
        putIfNotEmpty(payload, "category_id", course.getCategoryId());
        putIfNotEmpty(payload, "created_at", course.getCreatedAt());
        return payload;
    }

    private void putIfNotEmpty(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isEmpty()) {
            payload.put(key, value);
        }
    }
}