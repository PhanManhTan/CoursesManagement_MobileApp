package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.remote.ReviewApi;
import com.example.myapplication.models.Review;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewRepository {
    private final ReviewApi reviewApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public ReviewRepository(Context context) {
        // Initialize ReviewApi using RetrofitClient
        this.reviewApi = RetrofitClient.getClient(context).create(ReviewApi.class);
    }

    // Get all Reviews from remote
    public void getAll(RepositoryCallback<List<Review>> callback) {
        reviewApi.getAll().enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Review by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Review> callback) {
        reviewApi.getById(id).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Review not found");
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Review to remote
    public void insert(Review review, RepositoryCallback<Void> callback) {
        reviewApi.insert(review).enqueue(new Callback<Void>() {
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

    // Update Review by ID on remote
    public void update(String id, Review review, RepositoryCallback<Void> callback) {
        reviewApi.update(id, review).enqueue(new Callback<Void>() {
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

    // Delete Review by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        reviewApi.delete(id).enqueue(new Callback<Void>() {
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
