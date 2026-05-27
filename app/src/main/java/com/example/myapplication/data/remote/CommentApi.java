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
    @GET("comments?select=*")
    Call<List<Comment>> getAll();

    @GET("comments?select=*")
    Call<List<Comment>> getById(@Query("id") String idFilter);

    @GET("comments?select=*,users(id,full_name,avatar_url)&order=created_at.asc")
    Call<List<Comment>> getByLessonId(@Query("lesson_id") String lessonIdFilter);

    @POST("comments")
    Call<Void> insert(@Body Comment comment);

    @PATCH("comments")
    Call<Void> update(@Query("id") String idFilter, @Body Comment comment);

    @DELETE("comments")
    Call<Void> delete(@Query("id") String idFilter);
}