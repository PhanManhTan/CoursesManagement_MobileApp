//package com.example.myapplication.data.remote;
//
//import com.example.myapplication.utils.Constants;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class RetrofitClient {
//    private static Retrofit retrofit = null;
//
//    public static Retrofit getClient(this) {
//        if (retrofit == null) {
//            // Thêm Logging Interceptor để xem chi tiết Request/Response
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .addInterceptor(logging) // Add logging
//                    .addInterceptor(chain -> {
//                        Request original = chain.request();
//                        Request request = original.newBuilder()
//                                .header("apikey", Constants.SUPABASE_API_KEY)
//                                .header("Authorization", "Bearer " + Constants.SUPABASE_API_KEY)
//                                .method(original.method(), original.body())
//                                .build();
//                        return chain.proceed(request);
//                    })
//                    .build();
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(Constants.SUPABASE_URL)
//                    .client(okHttpClient)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//        }
//        return retrofit;
//    }
//}
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

        // Logging để debug
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        SessionManager sessionManager = new SessionManager(context);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    String token = sessionManager.getToken(); // 🔥 lấy access_token

                    Request.Builder builder = original.newBuilder()
                            .header("apikey", Constants.SUPABASE_API_KEY)
                            .header("Content-Type", "application/json");

                    // 🔥 CHỈ thêm Authorization nếu có token
                    if (token != null) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(builder.build());
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SUPABASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}