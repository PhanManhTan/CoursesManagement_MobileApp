package com.example.myapplication.data.remote;

import com.example.myapplication.models.Course;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CourseApi {
    // Get all Courses
    @GET("courses?select=*")
    Call<List<Course>> getAll();

    // Get Course by ID
    @GET("courses?select=*")
    Call<List<Course>> getById(@Query("id") String idFilter);

    // Get Courses for a specific Instructor
    @GET("courses?select=*")
    Call<List<Course>> getByInstructor(@Query("instructor_id") String instructorFilter);

    // Get Courses by Status
    @GET("courses?select=*")
    Call<List<Course>> getByStatus(@Query("status") String statusFilter);

    // Insert new Course
    @POST("courses")
    Call<Void> insert(@Body Course course);

    // Update Course by ID
    @PATCH("courses")
    Call<Void> update(@Query("id") String idFilter, @Body Course course);

    // Delete Course by ID
    @DELETE("courses")
    Call<Void> delete(@Query("id") String idFilter);
}
