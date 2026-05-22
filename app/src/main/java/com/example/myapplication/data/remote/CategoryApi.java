package com.example.myapplication.data.remote;

import com.example.myapplication.models.Category;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CategoryApi {
    // Get all Categories
    @GET("categories?select=*")
    Call<List<Category>> getAll();

    // Get Category by ID
    @GET("categories?select=*")
    Call<List<Category>> getById(@Query("id") String idFilter);

    // Insert new Category
    @POST("categories")
    Call<Void> insert(@Body Category category);

    // Update Category by ID
    @PATCH("categories")
    Call<Void> update(@Query("id") String idFilter, @Body Category category);

    // Delete Category by ID
    @DELETE("categories")
    Call<Void> delete(@Query("id") String idFilter);
}
