package com.example.myapplication.data.remote;

import com.example.myapplication.models.Comment;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CommentApi {
    // Get all Comments
    @GET("comments?select=*")
    Call<List<Comment>> getAll();

    // Get Comment by ID
    @GET("comments?select=*")
    Call<List<Comment>> getById(@Query("id") String idFilter);

    // Insert new Comment
    @POST("comments")
    Call<Void> insert(@Body Comment comment);

    // Update Comment by ID
    @PATCH("comments")
    Call<Void> update(@Query("id") String idFilter, @Body Comment comment);

    // Delete Comment by ID
    @DELETE("comments")
    Call<Void> delete(@Query("id") String idFilter);
}
