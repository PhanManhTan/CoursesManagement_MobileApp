package com.example.myapplication.data.remote;

import com.example.myapplication.models.Chapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChapterApi {
    // Get all Chapters
    @GET("chapters?select=*")
    Call<List<Chapter>> getAll();

    // Get Chapter by ID
    @GET("chapters?id=eq.{id}&select=*")
    Call<List<Chapter>> getById(@Path("id") String id);

    // Get Chapters by Course ID
    @GET("chapters?select=*")
    Call<List<Chapter>> getByCourseId(@Query("course_id") String courseIdFilter);

    // Insert new Chapter
    @POST("chapters")
    Call<Void> insert(@Body Chapter chapter);

    // Update Chapter by ID
    @PATCH("chapters?id=eq.{id}")
    Call<Void> update(@Path("id") String id, @Body Chapter chapter);

    // Delete Chapter by ID
    @DELETE("chapters?id=eq.{id}")
    Call<Void> delete(@Path("id") String id);
}
