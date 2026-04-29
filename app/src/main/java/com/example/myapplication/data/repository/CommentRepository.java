package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.CommentApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Comment;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentRepository {
    private final CommentApi commentApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public CommentRepository(Context context) {
        // Initialize CommentApi using RetrofitClient
        this.commentApi = RetrofitClient.getClient(context).create(CommentApi.class);
    }

    // Get all Comments from remote
    public void getAll(RepositoryCallback<List<Comment>> callback) {
        commentApi.getAll().enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Comment by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Comment> callback) {
        commentApi.getById(id).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Comment not found");
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Comment to remote
    public void insert(Comment comment, RepositoryCallback<Void> callback) {
        commentApi.insert(comment).enqueue(new Callback<Void>() {
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

    // Update Comment by ID on remote
    public void update(String id, Comment comment, RepositoryCallback<Void> callback) {
        commentApi.update(id, comment).enqueue(new Callback<Void>() {
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

    // Delete Comment by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        commentApi.delete(id).enqueue(new Callback<Void>() {
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
