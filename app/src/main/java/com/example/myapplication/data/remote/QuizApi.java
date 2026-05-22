package com.example.myapplication.data.remote;

import com.example.myapplication.models.Quiz;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface QuizApi {
    // Get all Quizzes
    @GET("quizzes?select=*")
    Call<List<Quiz>> getAll();

    // Get Quiz by ID
    @GET("quizzes?select=*")
    Call<List<Quiz>> getById(@Query("id") String idFilter);

    @GET("quizzes?select=*")
    Call<List<Quiz>> getByLessonId(@Query("lesson_id") String lessonIdFilter);

    // Insert new Quiz
    @POST("quizzes")
    Call<Void> insert(@Body Quiz quiz);

    // Update Quiz by ID
    @PATCH("quizzes")
    Call<Void> update(@Query("id") String idFilter, @Body Quiz quiz);

    // Delete Quiz by ID
    @DELETE("quizzes")
    Call<Void> delete(@Query("id") String idFilter);

    @DELETE("quizzes")
    Call<Void> deleteByLessonId(@Query("lesson_id") String lessonIdFilter);
}
