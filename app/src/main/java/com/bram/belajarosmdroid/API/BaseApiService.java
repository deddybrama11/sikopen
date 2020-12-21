package com.bram.belajarosmdroid.API;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface BaseApiService {

    @FormUrlEncoded
    @POST("auth")
    Call<ResponseBody> loginRequest(
            @Field("username") String nip,
            @Field("password") String pass);

    @GET("shifts/get")
    Call<ResponseBody> getAllShift();

    @FormUrlEncoded
    @POST("presence/check-clock")
    Call<ResponseBody> absenRequest(
            @Field("cc_type") Integer cc,
            @Field("employee_id") Integer id,
            @Field("shift") Integer shift,
            @Field("jmasuk") String jmasuk,
            @Field("jpulang") String jpulang,
            @Field("jefektif") String jefektif,
            @Field("mesin_id") Integer mesin_id,
            @Field("lat") String lat,
            @Field("lng") String lng);

    @GET("points/get")
    Call<ResponseBody> getMesin();

}
