package com.example.myapplication.data.remote;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit interface for managing Firebase Cloud Messaging (FCM) tokens.
 */
public interface FcmApi {
    @POST("user_fcm_tokens")
    Call<Void> upsertToken(
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );
}
