package com.example.myapplication.utils;

import android.content.Context;

import com.example.myapplication.models.AuthErrorResponse;
import com.example.myapplication.models.AuthResponse;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.OtpRequest;
import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.models.ResendOtpRequest;
import com.example.myapplication.models.UpdatePasswordRequest;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    private final ApiService api;
    private final SessionManager session;

    public AuthRepository(Context context) {
        api     = RetrofitClient.getApiService();
        session = new SessionManager(context);
    }

    public void login(String email, String password, AuthCallback callback) {
        api.login("password", new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            saveSessionFromResponse(response.body(), email);
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseError(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        callback.onError("Connection error: " + t.getMessage());
                    }
                });
    }

    public void register(String email, String password, String fullName, String phone, SimpleCallback callback) {
        api.register(new RegisterRequest(email, password, fullName, phone))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(parseError(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        callback.onError("Connection error: " + t.getMessage());
                    }
                });
    }

    public void verifyOtp(String email, String otp, String type, AuthCallback callback) {
        OtpRequest request = "recovery".equals(type)
                ? OtpRequest.forRecovery(email, otp)
                : OtpRequest.forSignup(email, otp);

        api.verifyOtp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!"recovery".equals(type)) {
                        saveSessionFromResponse(response.body(), email);
                    }
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    public void resendSignupOtp(String email, SimpleCallback callback) {
        api.resendOtp(new ResendOtpRequest(email, "signup"))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) callback.onSuccess();
                        else callback.onError(parseError(response));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Connection error: " + t.getMessage());
                    }
                });
    }

    public void forgotPassword(String email, SimpleCallback callback) {
        api.forgotPassword(new LoginRequest(email, ""))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) callback.onSuccess();
                        else callback.onError(parseError(response));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Connection error: " + t.getMessage());
                    }
                });
    }

    public void updatePassword(String accessToken, String newPassword, SimpleCallback callback) {
        api.updatePassword("Bearer " + accessToken, new UpdatePasswordRequest(newPassword))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) callback.onSuccess();
                        else callback.onError(parseError(response));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Connection error: " + t.getMessage());
                    }
                });
    }

    public SessionManager getSession() {
        return session;
    }

    private void saveSessionFromResponse(AuthResponse body, String email) {
        String role     = (body.user != null && body.user.userMetadata != null) ? body.user.userMetadata.role     : "student";
        String fullName = (body.user != null && body.user.userMetadata != null) ? body.user.userMetadata.fullName : "";
        String userId   = body.user != null ? body.user.id : "";
        session.saveSession(body.accessToken, body.refreshToken, userId, email, role, fullName);
    }

    private String parseError(Response<?> response) {
        try {
            AuthErrorResponse err = new Gson().fromJson(response.errorBody().string(), AuthErrorResponse.class);
            return err.getReadableMessage();
        } catch (Exception e) {
            return "Error " + response.code();
        }
    }
}
