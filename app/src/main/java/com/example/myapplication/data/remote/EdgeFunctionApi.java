package com.example.myapplication.data.remote;

import com.example.myapplication.models.PaymentResponse;
import com.google.gson.JsonObject;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface EdgeFunctionApi {
    @POST
    Call<PaymentResponse> createPaymentUrl(
            @Url String url,
            @Header("Authorization") String authHeader,
            @Body Map<String, Object> payload
    );

    // THÊM HÀM NÀY ĐỂ GỌI API XÁC THỰC
    @GET
    Call<JsonObject> verifyPayment(@Url String url);
}