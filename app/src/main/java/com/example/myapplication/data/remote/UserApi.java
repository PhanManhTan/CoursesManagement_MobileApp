package com.example.myapplication.data.remote;

import com.example.myapplication.models.User;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    // Get all Users
    @GET("/rest/v1/users?select=*")
    Call<List<User>> getAll();

    // Get User by ID
    @GET("/rest/v1/users?select=*")
    Call<List<User>> getById(@Query("id") String idFilter);

    // Insert new User
    @POST("/rest/v1/users")
    Call<Void> insert(@Body User user);

    // Update User by ID
    @PATCH("/rest/v1/users")
    Call<Void> update(@Query("id") String idFilter, @Body User user);

    @PATCH("/rest/v1/users")
    Call<Void> updateFields(@Query("id") String idFilter, @Body Map<String, Object> fields);

    // Delete User by ID
    @DELETE("/rest/v1/users")
    Call<Void> delete(@Query("id") String idFilter);
}
