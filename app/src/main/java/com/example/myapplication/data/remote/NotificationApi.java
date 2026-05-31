package com.example.myapplication.data.remote;

import com.example.myapplication.models.Notification;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotificationApi {
    // Lấy thông báo của user, sắp xếp mới nhất lên đầu
    @GET("notifications?order=created_at.desc")
    Call<List<Notification>> getByUserId(@Query("user_id") String userIdFilter);

    // Lấy danh sách thông báo chưa đọc
    @GET("notifications")
    Call<List<Notification>> getUnreadByUserId(@Query("user_id") String userIdFilter, @Query("is_read") String isReadFilter);

    // Dùng Map để insert an toàn (tránh truyền id null)
    @POST("notifications")
    Call<Void> insert(@Body Map<String, Object> payload);

    @PATCH("notifications")
    Call<Void> update(@Query("id") String idFilter, @Body Map<String, Object> payload);

    @DELETE("notifications")
    Call<Void> delete(@Query("id") String idFilter);
}