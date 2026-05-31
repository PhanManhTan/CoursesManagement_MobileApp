package com.example.myapplication.data.remote;

import com.example.myapplication.models.Review;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReviewApi {
    @GET("reviews?select=*")
    Call<List<Review>> getAll();

    @GET("reviews?select=*")
    Call<List<Review>> getById(@Query("id") String idFilter);

    @POST("reviews")
    Call<Void> insert(@Body Review review);

    @PATCH("reviews")
    Call<Void> update(@Query("id") String idFilter, @Body Review review);

    @DELETE("reviews")
    Call<Void> delete(@Query("id") String idFilter);

    @GET("reviews?select=*")
    Call<List<Review>> getReviewByUserAndCourse(
            @Query("user_id") String userIdFilter,
            @Query("course_id") String courseIdFilter
    );

    @GET("reviews?select=*")
    Call<List<Review>> getByCourseId(@Query("course_id") String courseIdFilter);
}
