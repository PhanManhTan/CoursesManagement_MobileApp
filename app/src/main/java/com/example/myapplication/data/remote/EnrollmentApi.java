package com.example.myapplication.data.remote;

import com.example.myapplication.models.Enrollment;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EnrollmentApi {
    @GET("enrollment_progress_view?select=*,courses(*)")
    Call<List<Enrollment>> getAll();

    @GET("enrollment_progress_view?select=*,courses(*)")
    Call<List<Enrollment>> getByUserId(@Query("user_id") String userIdFilter);

    @GET("enrollment_progress_view?select=*")
    Call<List<Enrollment>> getById(@Query("id") String idFilter);

    @POST("enrollments")
    Call<Void> insert(@Body Enrollment enrollment);

    @PATCH("enrollments")
    Call<Void> update(@Query("id") String idFilter, @Body Enrollment enrollment);

    @DELETE("enrollments")
    Call<Void> delete(@Query("id") String idFilter);

    @GET("enrollments?select=id")
    Call<List<Object>> checkEnrollment(@Query("user_id") String userIdFilter, @Query("course_id") String courseIdFilter);
}
