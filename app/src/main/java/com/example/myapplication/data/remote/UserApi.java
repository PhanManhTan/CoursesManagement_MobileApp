package com.example.myapplication.data.remote;

import com.example.myapplication.models.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    // Get all Users
    @GET("users?select=*")
    Call<List<User>> getAll();

    // Get User by ID
    @GET("users?select=*")
    Call<List<User>> getById(@Query("id") String idFilter);

    // Insert new User
    @POST("users")
    Call<Void> insert(@Body User user);

    // Update User by ID
    @PATCH("users")
    Call<Void> update(@Query("id") String idFilter, @Body User user);

    // Delete User by ID
    @DELETE("users")
    Call<Void> delete(@Query("id") String idFilter);
}
