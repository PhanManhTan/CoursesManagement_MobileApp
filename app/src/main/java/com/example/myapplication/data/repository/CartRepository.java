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
        this.cartApi = RetrofitClient.getClient(context).create(CartApi.class);
    }

    public void getAll(RepositoryCallback<List<Cart>> callback) {
        cartApi.getAll().enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<List<Cart>> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void getById(String id, RepositoryCallback<Cart> callback) {
        cartApi.getById(id).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Cart> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void insert(Cart cart, RepositoryCallback<Void> callback) {
        cartApi.insert(cart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void update(String id, Cart cart, RepositoryCallback<Void> callback) {
        cartApi.update(id, cart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void delete(String id, RepositoryCallback<Void> callback) {
        cartApi.delete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error: " + response.code());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }
}
