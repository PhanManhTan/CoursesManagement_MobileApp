package com.example.myapplication.data.remote;

import android.content.Context;
import android.util.Log;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.utils.SessionManager;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit = null;
    private static volatile AuthErrorListener authErrorListener;
    private static final AtomicBoolean handlingAuthError = new AtomicBoolean(false);

    public interface AuthErrorListener {
        void onAuthError();
    }

    public static void setAuthErrorListener(AuthErrorListener listener) {
        authErrorListener = listener;
        if (listener != null) {
            handlingAuthError.set(false);
        }
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Luôn bật BODY để debug

            SessionManager sessionManager = new SessionManager(context.getApplicationContext());
            String apiKey = BuildConfig.SUPABASE_API_KEY;

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = sessionManager.getToken();

                        Request.Builder builder = original.newBuilder()
                                .header("Content-Type", "application/json");

                        if (apiKey != null && !apiKey.trim().isEmpty()) {
                            builder.header("apikey", apiKey);
                        }

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        okhttp3.Response response = chain.proceed(builder.build());
                        if (response.code() == 401 && handlingAuthError.compareAndSet(false, true)) {
                            Log.w(TAG, "Received 401 from " + original.url().encodedPath());
                            AuthErrorListener listener = authErrorListener;
                            if (listener != null) {
                                listener.onAuthError();
                            }
                        }
                        return response;
                    })
                    .addInterceptor(logging)
                    .build();

            // SỬA LẠI BASE URL: Phải có /rest/v1/ ở cuối
            String baseUrl = BuildConfig.SUPABASE_URL;
            if (baseUrl != null) {
                if (!baseUrl.endsWith("/")) baseUrl += "/";
                if (!baseUrl.contains("/rest/v1/")) baseUrl += "rest/v1/";
            } else {
                baseUrl = "https://placeholder.supabase.co/rest/v1/";
            }

            Log.d(TAG, "Retrofit Base URL: " + baseUrl);

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
