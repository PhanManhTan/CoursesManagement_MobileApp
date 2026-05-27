package com.example.myapplication.data.remote;

import com.example.myapplication.models.Cart;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CartApi {
    @GET("carts?select=*,courses(*)")
    Call<List<Cart>> getByUserId(@Query("user_id") String userIdFilter);

    @POST("carts")
    Call<Void> insert(@Body Map<String, Object> cartPayload);

    @DELETE("carts")
    Call<Void> deleteById(@Query("id") String idFilter);

    @DELETE("carts")
    Call<Void> deleteByUserId(@Query("user_id") String userIdFilter);
}
