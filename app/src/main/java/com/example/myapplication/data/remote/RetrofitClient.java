package com.example.myapplication.data.remote;

import android.content.Context;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = sessionManager.getToken();

                        Request.Builder builder = original.newBuilder()
                                .header("apikey", Constants.SUPABASE_API_KEY)
                                .header("Content-Type", "application/json");

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(builder.build());
                    })
                    .build();

            // Kiểm tra URL hợp lệ trước khi build để tránh crash khó hiểu
            String baseUrl = Constants.SUPABASE_URL;
            if (baseUrl == null || baseUrl.isEmpty() || !baseUrl.startsWith("http")) {
                baseUrl = "https://placeholder.supabase.co"; // Tránh crash, nhưng API sẽ lỗi 404
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
