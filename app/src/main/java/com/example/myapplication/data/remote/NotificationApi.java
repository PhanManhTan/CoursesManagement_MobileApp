package com.example.myapplication.data.remote;

import com.example.myapplication.models.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotificationApi {
    @GET("notifications")
    Call<List<Notification>> getAll();

    @GET("notifications/{id}")
    Call<Notification> getById(@Path("id") String id);

    @POST("notifications")
    Call<Void> insert(@Body Notification notification);

    @PATCH("notifications/{id}")
    Call<Void> update(@Path("id") String id, @Body Notification notification);

    @DELETE("notifications/{id}")
    Call<Void> delete(@Path("id") String id);
}
