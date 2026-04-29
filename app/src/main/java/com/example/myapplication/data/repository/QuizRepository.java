package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.QuizApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Quiz;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizRepository {
    private final QuizApi quizApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public QuizRepository(Context context) {
        // Initialize QuizApi using RetrofitClient
        this.quizApi = RetrofitClient.getClient(context).create(QuizApi.class);
    }

    // Get all Quizzes from remote
    public void getAll(RepositoryCallback<List<Quiz>> callback) {
        quizApi.getAll().enqueue(new Callback<List<Quiz>>() {
            @Override
            public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Quiz>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Quiz by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Quiz> callback) {
        quizApi.getById(id).enqueue(new Callback<List<Quiz>>() {
            @Override
            public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Quiz not found");
                }
            }

            @Override
            public void onFailure(Call<List<Quiz>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Quiz to remote
    public void insert(Quiz quiz, RepositoryCallback<Void> callback) {
        quizApi.insert(quiz).enqueue(new Callback<Void>() {
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

    // Update Quiz by ID on remote
    public void update(String id, Quiz quiz, RepositoryCallback<Void> callback) {
        quizApi.update(id, quiz).enqueue(new Callback<Void>() {
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

    // Delete Quiz by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        quizApi.delete(id).enqueue(new Callback<Void>() {
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
