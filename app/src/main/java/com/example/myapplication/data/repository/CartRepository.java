package com.example.myapplication.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.myapplication.data.remote.CartApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Cart;
import com.example.myapplication.utils.ApiErrorFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private final CartApi cartApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public CartRepository(Context context) {
        this.cartApi = RetrofitClient.getClient(context).create(CartApi.class);
    }

    public void getByUserId(String userId, RepositoryCallback<List<Cart>> callback) {
        cartApi.getByUserId("eq." + userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorFormatter.fromResponse(response));
                }
            }
            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void addToCart(Cart cart, RepositoryCallback<Void> callback) {
        // CHỈ GỬI CÁC TRƯỜNG CƠ BẢN LÊN SUPABASE
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", cart.getUserId());
        payload.put("course_id", cart.getCourseId());

        Log.d(TAG, "Adding to cart: " + payload.toString());

        cartApi.insert(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String error = ApiErrorFormatter.fromResponse(response);
                    Log.e(TAG, "Add to cart failed: " + error);
                    callback.onError(error);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void removeFromCart(String cartItemId, RepositoryCallback<Void> callback) {
        cartApi.deleteById("eq." + cartItemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(ApiErrorFormatter.fromResponse(response));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
