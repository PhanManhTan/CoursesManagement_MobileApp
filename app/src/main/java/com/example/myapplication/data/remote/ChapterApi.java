package com.example.myapplication.data.remote;

import com.example.myapplication.models.Chapter;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChapterApi {
    // Get all Chapters
    @GET("chapters?select=*")
    Call<List<Chapter>> getAll();

    // Get Chapter by ID
    @GET("chapters?select=*")
    Call<List<Chapter>> getById(@Query("id") String idFilter);

    // Get Chapters by Course ID
    @GET("chapters?select=*")
    Call<List<Chapter>> getByCourseId(@Query("course_id") String courseIdFilter);

    // Insert new Chapter
    @POST("chapters")
    Call<Void> insert(@Body Chapter chapter);

    @POST("chapters?select=*")
    Call<List<Chapter>> insertAndReturn(@Header("Prefer") String prefer, @Body Map<String, Object> chapter);

    // Update Chapter by ID
    @PATCH("chapters")
    Call<Void> update(@Query("id") String idFilter, @Body Map<String, Object> chapter);

    // Delete Chapter by ID
    @DELETE("chapters")
    Call<Void> delete(@Query("id") String idFilter);
}
