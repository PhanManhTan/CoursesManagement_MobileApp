package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.CartApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Cart;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private final CartApi cartApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public CartRepository(Context context) {
        // Initialize CartApi using RetrofitClient
        this.cartApi = RetrofitClient.getClient(context).create(CartApi.class);
    }

    // Get all Carts from remote
    public void getAll(RepositoryCallback<List<Cart>> callback) {
        cartApi.getAll().enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Cart by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Cart> callback) {
        cartApi.getById(id).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Cart item not found");
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Cart to remote
    public void insert(Cart cart, RepositoryCallback<Void> callback) {
        cartApi.insert(cart).enqueue(new Callback<Void>() {
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

    // Update Cart by ID on remote
    public void update(String id, Cart cart, RepositoryCallback<Void> callback) {
        cartApi.update(id, cart).enqueue(new Callback<Void>() {
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

    // Delete Cart by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        cartApi.delete(id).enqueue(new Callback<Void>() {
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
