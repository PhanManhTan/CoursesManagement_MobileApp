package com.example.myapplication.data.remote;

import com.example.myapplication.models.Report;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReportApi {
    @GET("reports?select=*")
    Call<List<Report>> getAll();

    @GET("reports?select=*")
    Call<List<Report>> getById(@Query("id") String idFilter);

    @POST("reports")
    Call<Void> insert(@Body Report report);

    @PATCH("reports")
    Call<Void> update(@Query("id") String idFilter, @Body Report report);

    @DELETE("reports")
    Call<Void> delete(@Query("id") String idFilter);
}
