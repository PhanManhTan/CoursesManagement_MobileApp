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
    // Get all Enrollments
    @GET("enrollments?select=*")
    Call<List<Enrollment>> getAll();

    // Get Enrollment by ID
    @GET("enrollments?select=*")
    Call<List<Enrollment>> getById(@Query("id") String idFilter);

    // Insert new Enrollment
    @POST("enrollments")
    Call<Void> insert(@Body Enrollment enrollment);

    // Update Enrollment by ID
    @PATCH("enrollments")
    Call<Void> update(@Query("id") String idFilter, @Body Enrollment enrollment);

    // Delete Enrollment by ID
    @DELETE("enrollments")
    Call<Void> delete(@Query("id") String idFilter);
}
