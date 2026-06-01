package com.example.myapplication.data.remote;

import com.example.myapplication.models.Course;
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

public interface CourseApi {
    // Get all Courses
    @GET("courses?select=*")
    Call<List<Course>> getAll();

    // Get Courses by Title Search
    @GET("courses?select=*")
    Call<List<Course>> search(
            @Query("title") String titleFilter
    );

    // Get Course by ID
    @GET("courses?select=*")
    Call<List<Course>> getById(@Query("id") String idFilter);

    @GET("courses?select=*")
    Call<List<Course>> getByIdAndInstructor(
            @Query("id") String idFilter,
            @Query("instructor_id") String instructorFilter
    );

    // Get Courses for a specific Instructor
    @GET("courses?select=*")
    Call<List<Course>> getByInstructor(@Query("instructor_id") String instructorFilter);

    // Get Courses by Status
    @GET("courses?select=*")
    Call<List<Course>> getByStatus(@Query("status") String statusFilter);

    // Get Courses by Category ID
    @GET("courses?select=*")
    Call<List<Course>> getByCategoryId(
            @Query("category_id") String categoryIdFilter
    );

    // Insert new Course
    @POST("courses")
    Call<Void> insert(@Body Map<String, Object> course);

    @POST("courses?select=*")
    Call<List<Course>> insertAndReturn(@Header("Prefer") String prefer, @Body Map<String, Object> course);

    // Update Course by ID
    @PATCH("courses")
    Call<Void> update(@Query("id") String idFilter, @Body Map<String, Object> course);

    @PATCH("courses?select=*")
    Call<List<Course>> updateByInstructor(
            @Header("Prefer") String prefer,
            @Query("id") String idFilter,
            @Query("instructor_id") String instructorFilter,
            @Body Map<String, Object> course
    );

    // Delete Course by ID
    @DELETE("courses")
    Call<Void> delete(@Query("id") String idFilter);

    @DELETE("courses?select=*")
    Call<List<Course>> deleteByInstructor(
            @Header("Prefer") String prefer,
            @Query("id") String idFilter,
            @Query("instructor_id") String instructorFilter
    );

    // Search courses by category and title
    @GET("courses?select=*")
    Call<List<Course>> searchByCategoryAndTitle(
            @Query("category_id") String categoryId,
            @Query("title") String title
    );
}