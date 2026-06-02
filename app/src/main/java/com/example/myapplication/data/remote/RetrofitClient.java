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

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit = null;
    private static volatile AuthErrorListener authErrorListener;
    private static final AtomicBoolean handlingAuthError = new AtomicBoolean(false);
    private static final Object refreshLock = new Object();
    private static final OkHttpClient refreshClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
                            handleAuthError(appContext, "Missing session before " + original.url().encodedPath());
                        }

                        String token = sessionManager.getToken();
                        if (!authEndpoint && (!hasValue(token) || sessionManager.isAccessTokenExpired(token))) {
                            token = refreshAccessTokenIfPossible(sessionManager, apiKey, null);
                        }

                        okhttp3.Response response = chain.proceed(buildRequest(original, apiKey, token));
                        if (!authEndpoint && response.code() == 401) {
                            response.close();
                            String refreshedToken = refreshAccessTokenIfPossible(sessionManager, apiKey, token);
                            if (hasValue(refreshedToken) && !refreshedToken.equals(token)) {
                                return chain.proceed(buildRequest(original, apiKey, refreshedToken));
                            }
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

    private static Request buildRequest(Request original, String apiKey, String token) {
        Request.Builder builder = original.newBuilder()
                .header("Content-Type", "application/json");

        if (hasValue(apiKey)) {
            builder.header("apikey", apiKey);
        }

        if (hasValue(token)) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder.build();
    }

    private static String refreshAccessTokenIfPossible(SessionManager sessionManager, String apiKey, String failedToken) {
        synchronized (refreshLock) {
            String currentToken = sessionManager.getToken();
            if (hasValue(failedToken)) {
                if (hasValue(currentToken) && !currentToken.equals(failedToken)) {
                    return currentToken;
                }
            } else if (hasValue(currentToken) && !sessionManager.isAccessTokenExpired(currentToken)) {
                return currentToken;
            }

            String refreshToken = sessionManager.getRefreshToken();
            if (!hasValue(refreshToken)) {
                return null;
            }

            String projectUrl = getProjectBaseUrl();
            if (!hasValue(projectUrl) || !hasValue(apiKey)) {
                return null;
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("refresh_token", refreshToken);

                Request request = new Request.Builder()
                        .url(projectUrl + "/auth/v1/token?grant_type=refresh_token")
                        .post(RequestBody.create(JSON, payload.toString()))
                        .header("Content-Type", "application/json")
                        .header("apikey", apiKey)
                        .build();

                try (okhttp3.Response response = refreshClient.newCall(request).execute()) {
                    String body = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        Log.w(TAG, "Refresh token failed with HTTP " + response.code());
                        return null;
                    }

                    JSONObject json = new JSONObject(body);
                    String newAccessToken = json.optString("access_token", "");
                    String newRefreshToken = json.optString("refresh_token", refreshToken);
                    if (!hasValue(newAccessToken)) {
                        return null;
                    }

                    sessionManager.saveSession(
                            newAccessToken,
                            hasValue(newRefreshToken) ? newRefreshToken : refreshToken,
                            sessionManager.getUserId(),
                            sessionManager.getRole()
                    );
                    return newAccessToken;
                }
            } catch (Exception e) {
                Log.w(TAG, "Refresh token failed", e);
                return null;
            }
        }
    }

    private static String getProjectBaseUrl() {
        String baseUrl = BuildConfig.SUPABASE_URL;
        if (!hasValue(baseUrl)) {
            return "";
        }

        baseUrl = baseUrl.trim();
        int restIndex = baseUrl.indexOf("/rest/v1");
        if (restIndex >= 0) {
            baseUrl = baseUrl.substring(0, restIndex);
        }
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private static void handleAuthError(Context context, String reason) {
        if (!handlingAuthError.compareAndSet(false, true)) {
            return;
        }

        Log.w(TAG, reason);
        AuthErrorListener listener = authErrorListener;
        new Handler(Looper.getMainLooper()).post(() -> {
            new SessionManager(context).clear();
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

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
