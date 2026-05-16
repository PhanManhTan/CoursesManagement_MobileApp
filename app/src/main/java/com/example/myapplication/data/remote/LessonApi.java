package com.example.myapplication.data.remote;

import com.example.myapplication.models.Lesson;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LessonApi {
    // Get all Lessons
    @GET("lessons?select=*")
    Call<List<Lesson>> getAll();

    // Get Lesson by ID
    @GET("lessons?id=eq.{id}&select=*")
    Call<List<Lesson>> getById(@Path("id") String id);

    // Get Lessons by Chapter ID
    @GET("lessons?select=*")
    Call<List<Lesson>> getByChapterId(@Query("chapter_id") String chapterIdFilter);

    // Insert new Lesson
    @POST("lessons")
    Call<Void> insert(@Body Lesson lesson);

    // Update Lesson by ID
    @PATCH("lessons?id=eq.{id}")
    Call<Void> update(@Path("id") String id, @Body Lesson lesson);

    // Delete Lesson by ID
    @DELETE("lessons?id=eq.{id}")
    Call<Void> delete(@Path("id") String id);
}
