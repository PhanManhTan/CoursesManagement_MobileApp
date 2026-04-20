package com.example.myapplication.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("apikey", Constants.SUPABASE_API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
