package com.example.myapplication.data.remote;

import com.example.myapplication.models.Lesson;
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

public interface LessonApi {
    // Get all Lessons
    @GET("lessons?select=*")
    Call<List<Lesson>> getAll();

    // Get Lesson by ID
    @GET("lessons?select=*")
    Call<List<Lesson>> getById(@Query("id") String idFilter);

    // Get Lessons by Chapter ID
    @GET("lessons?select=*")
    Call<List<Lesson>> getByChapterId(@Query("chapter_id") String chapterIdFilter);

    // Insert new Lesson
    @POST("lessons")
    Call<Void> insert(@Body Lesson lesson);

    @POST("lessons?select=*")
    Call<List<Lesson>> insertAndReturn(@Header("Prefer") String prefer, @Body Map<String, Object> lesson);

    // Update Lesson by ID
    @PATCH("lessons")
    Call<Void> update(@Query("id") String idFilter, @Body Lesson lesson);

    @PATCH("lessons")
    Call<Void> updateById(@Query("id") String idFilter, @Body Map<String, Object> lesson);

    // Delete Lesson by ID
    @DELETE("lessons")
    Call<Void> delete(@Query("id") String idFilter);

    @DELETE("lessons")
    Call<Void> deleteById(@Query("id") String idFilter);
}
