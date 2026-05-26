package com.example.myapplication.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.myapplication.data.remote.FcmApi;
import com.example.myapplication.data.remote.RetrofitClient;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles communication for storing and updating client registration tokens.
 */
public class FcmRepository {
    private static final String TAG = "FcmRepository";
    private final FcmApi fcmApi;

    public FcmRepository(Context context) {
        this.fcmApi = RetrofitClient.getClient(context).create(FcmApi.class);
    }

    public void uploadToken(String userId, String token) {
        if (userId == null || userId.isEmpty() || token == null || token.isEmpty()) {
            Log.w(TAG, "Cannot upload null or empty credentials.");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("fcm_token", token);

        fcmApi.upsertToken("resolution=merge-duplicates", body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token uploaded/synced to Supabase successfully.");
                } else {
                    Log.e(TAG, "Failed to upload FCM token. Status code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error uploading FCM token: " + t.getMessage());
            }
        });
    }
}
