package com.example.myapplication.data.remote;

import com.example.myapplication.models.LessonProgress;
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

public interface LessonProgressApi {
    @GET("lesson_progress?select=*")
    Call<List<LessonProgress>> getAll();

    @GET("lesson_progress?select=*")
    Call<List<LessonProgress>> getById(@Query("id") String idFilter);

    @GET("lesson_progress?select=*")
    Call<List<LessonProgress>> getByUserIdAndLessonId(
            @Query("user_id") String userIdFilter,
            @Query("lesson_id") String lessonIdFilter
    );

    @POST("lesson_progress")
    Call<Void> insert(@Body LessonProgress lessonProgress);

    @POST("lesson_progress?select=*")
    Call<List<LessonProgress>> insertAndReturn(@Header("Prefer") String prefer, @Body Map<String, Object> lessonProgress);

    @PATCH("lesson_progress")
    Call<Void> updateById(@Query("id") String idFilter, @Body Map<String, Object> lessonProgress);

    @DELETE("lesson_progress")
    Call<Void> deleteById(@Query("id") String idFilter);
}