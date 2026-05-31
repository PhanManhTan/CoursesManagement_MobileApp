package com.example.myapplication.data.remote;

import com.example.myapplication.models.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotificationApi {

    @GET("notifications?select=*")
    Call<List<Notification>> getByUserId(@Query("user_id") String userIdFilter);

    @GET("notifications?select=*")
    Call<List<Notification>> getAll();

    @POST("notifications")
    Call<Void> insert(@Body Notification notification);

    @PATCH("notifications")
    Call<Void> update(@Query("id") String idFilter, @Body Notification notification);

    @DELETE("notifications")
    Call<Void> delete(@Query("id") String idFilter);
}

