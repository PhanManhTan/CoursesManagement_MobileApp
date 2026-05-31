package com.example.myapplication.data.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.activities.auth.LoginActivity;
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
        handlingAuthError.set(false);
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            }
            logging.redactHeader("apikey");
            logging.redactHeader("Authorization");

            Context appContext = context.getApplicationContext();
            SessionManager sessionManager = new SessionManager(appContext);
            String apiKey = BuildConfig.SUPABASE_API_KEY;

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        boolean authEndpoint = isAuthEndpoint(original);
                        if (!authEndpoint && !sessionManager.isLoggedIn()) {
                            handleAuthError(appContext, "Missing or expired session before " + original.url().encodedPath());
                        }

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
                        if (!authEndpoint && response.code() == 401) {
                            handleAuthError(appContext, "Received 401 from " + original.url().encodedPath());
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

    private static boolean isAuthEndpoint(Request request) {
        String path = request.url().encodedPath();
        return path != null && path.startsWith("/auth/v1/");
    }

    private static void handleAuthError(Context context, String reason) {
        if (!handlingAuthError.compareAndSet(false, true)) {
            return;
        }

        Log.w(TAG, reason);
        new SessionManager(context).clear();
        AuthErrorListener listener = authErrorListener;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (listener != null) {
                listener.onAuthError();
                return;
            }

            Toast.makeText(context, R.string.session_expired_login_again, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
