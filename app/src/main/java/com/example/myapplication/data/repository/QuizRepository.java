package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.QuizApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Quiz;
import com.example.myapplication.utils.ApiErrorFormatter;
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
                    callback.onError(getErrorMessage(response));
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
        quizApi.getById("eq." + id).enqueue(new Callback<List<Quiz>>() {
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

    public void getByLessonId(String lessonId, RepositoryCallback<List<Quiz>> callback) {
        quizApi.getByLessonId("eq." + lessonId).enqueue(new Callback<List<Quiz>>() {
            @Override
            public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
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
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void replaceForLesson(String lessonId, List<Quiz> quizzes, RepositoryCallback<Void> callback) {
        deleteByLessonId(lessonId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                insertQuizAt(lessonId, quizzes, 0, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void insertQuizAt(String lessonId, List<Quiz> quizzes, int index, RepositoryCallback<Void> callback) {
        if (quizzes == null || index >= quizzes.size()) {
            callback.onSuccess(null);
            return;
        }

        Quiz source = quizzes.get(index);
        if (source == null || source.getQuestion() == null || source.getQuestion().trim().isEmpty()) {
            insertQuizAt(lessonId, quizzes, index + 1, callback);
            return;
        }

        Quiz quiz = new Quiz(
                null,
                lessonId,
                source.getQuestion(),
                source.getOptions(),
                source.getCorrectAnswer()
        );
        insert(quiz, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                insertQuizAt(lessonId, quizzes, index + 1, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // Update Quiz by ID on remote
    public void update(String id, Quiz quiz, RepositoryCallback<Void> callback) {
        quizApi.update("eq." + id, quiz).enqueue(new Callback<Void>() {
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

    // Delete Quiz by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        quizApi.delete("eq." + id).enqueue(new Callback<Void>() {
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

    public void deleteByLessonId(String lessonId, RepositoryCallback<Void> callback) {
        quizApi.deleteByLessonId("eq." + lessonId).enqueue(new Callback<Void>() {
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

    private String getErrorMessage(Response<?> response) {
        return ApiErrorFormatter.fromResponse(response);
    }
}
