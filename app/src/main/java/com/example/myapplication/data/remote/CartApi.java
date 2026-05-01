package com.example.myapplication.data.remote;

import com.example.myapplication.models.Cart;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CartApi {
    @GET("cart")
    Call<List<Cart>> getAll();

    @GET("cart/{id}")
    Call<Cart> getById(@Path("id") String id);

    @POST("cart")
    Call<Void> insert(@Body Cart cart);

    @PATCH("cart/{id}")
    Call<Void> update(@Path("id") String id, @Body Cart cart);

    @DELETE("cart/{id}")
    Call<Void> delete(@Path("id") String id);
}
