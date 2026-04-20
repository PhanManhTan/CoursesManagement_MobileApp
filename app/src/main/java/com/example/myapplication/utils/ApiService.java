package com.example.myapplication.utils;

import com.example.myapplication.models.AuthResponse;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.OtpRequest;
import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.models.ResendOtpRequest;
import com.example.myapplication.models.UpdatePasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @POST("auth/v1/token")
    Call<AuthResponse> login(@Query("grant_type") String grantType, @Body LoginRequest body);

    @POST("auth/v1/verify")
    Call<AuthResponse> verifyOtp(@Body OtpRequest body);

    @POST("auth/v1/resend")
    Call<Void> resendOtp(@Body ResendOtpRequest body);

    @POST("auth/v1/recover")
    Call<Void> forgotPassword(@Body LoginRequest body);

    @PUT("auth/v1/user")
    Call<Void> updatePassword(@Header("Authorization") String bearer, @Body UpdatePasswordRequest body);
}
