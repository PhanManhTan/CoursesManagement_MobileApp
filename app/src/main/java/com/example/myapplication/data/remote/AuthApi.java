package com.example.myapplication.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface AuthApi {
    @POST("/auth/v1/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest request);

    @POST("/auth/v1/token?grant_type=password")
    Call<LoginResponse> signIn(@Body LoginRequest request);

    @POST("/auth/v1/resend")
    Call<Void> resend(@Body ResendRequest request);

    @POST("/auth/v1/recover")
    Call<Void> recover(@Body Map<String, String> body);

    @POST("/auth/v1/verify")
    Call<LoginResponse> verifyOtp(@Body VerifyOtpRequest request);

    @PUT("/auth/v1/user")
    Call<AuthResponse> updatePassword(@Body UpdatePasswordRequest request);

    // 🔥 Thêm: Lấy thông tin role từ bảng public.users (hoặc profiles)
    // Giả sử bảng tên là 'users' trong schema public
    @GET("/rest/v1/users")
    Call<List<UserProfile>> getUserProfile(@Query("id") String userId, @Query("select") String select);

    class UserProfile {
        @SerializedName("role")
        private String role;
        @SerializedName("full_name")
        private String fullName;

        public String getRole() { return role; }
        public String getFullName() { return fullName; }
    }

    class UpdatePasswordRequest {
        @SerializedName("password")
        private String password;
        public UpdatePasswordRequest(String password) { this.password = password; }
    }

    class VerifyOtpRequest {
        @SerializedName("email") private String email;
        @SerializedName("token") private String token;
        @SerializedName("type") private String type;
        public VerifyOtpRequest(String email, String token, String type) {
            this.email = email; this.token = token; this.type = type;
        }
    }

    class ResendRequest {
        @SerializedName("email") private String email;
        @SerializedName("type") private String type;
        public ResendRequest(String email, String type) { this.email = email; this.type = type; }
    }

    class SignUpRequest {
        @SerializedName("email") private String email;
        @SerializedName("password") private String password;
        @SerializedName("data") private Map<String, Object> data;
        public SignUpRequest(String email, String password, Map<String, Object> data) {
            this.email = email; this.password = password; this.data = data;
        }
    }

    class LoginRequest {
        @SerializedName("email") private String email;
        @SerializedName("password") private String password;
        public LoginRequest(String email, String password) { this.email = email; this.password = password; }
    }

    class LoginResponse {
        @SerializedName("access_token") private String accessToken;
        @SerializedName("user") private AuthResponse user;
        public AuthResponse getUser() { return user; }
        public String getAccessToken() { return accessToken; }
    }

    class AuthResponse {
        @SerializedName("id") private String id;
        @SerializedName("email") private String email;
        @SerializedName("user_metadata") private Map<String, Object> userMetadata;
        public String getId() { return id; }
        public String getEmail() { return email; }
        public Map<String, Object> getUserMetadata() { return userMetadata; }
    }
}
