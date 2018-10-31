package com.edisoninteractive.inrideads.Interfaces;

import com.edisoninteractive.inrideads.Entities.ConfigData;
import com.edisoninteractive.inrideads.Entities.UpdaterCheckResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Alex Angan one fine day
 */

public interface RetrofitAPI
{
    @FormUrlEncoded
    @POST(".")
    Call<ResponseBody> uploadEvent(@Field("id") String id,
                                  @Field("status") String status,
                                  @Field("unit_id") String unit_id,
                                  @Field("uuid") String uuid,
                                  @Field("wifi_mac") String wifi_mac
    );

    @FormUrlEncoded
    @POST(".")
    Call<ResponseBody> uploadEvents(@Field("ids[]") String id,
                                   @Field("status") String status,
                                   @Field("unit_id") String unit_id,
                                   @Field("uuid") String uuid,
                                   @Field("wifi_mac") String wifi_mac
    );

    @GET(".")
    Call<UpdaterCheckResponse> getUniId(
                                    @Query("unit_id") String unit_id,
                                    @Query("uuid") String uuid
    );

    @FormUrlEncoded
    @POST(".")
    Call<ConfigData> getGlobalConfig(
                                                    @Field("force") String force,
                                                    @Field("unit_id") String unit_id,
                                                    @Field("uuid") String uuid,
                                                    @Field("rnd") String rnd,
                                                    @Field("wifi_mac") String wifi_mac,
                                                    @Field("sw_version") String sw_version,
                                                    @Field("latitude") String latitude,
                                                    @Field("longitude") String longitude,
                                                    @Field("location_accuracy") String location_accuracy,
                                                    @Field("location_provider_name") String location_provider_name
    );

    @FormUrlEncoded
    @POST(".")
    Call<ResponseBody> uploadLogEvent(
            @Field("unit_id") String unit_id,
            @Field("uuid") String uuid,
            @Field("unix_time_stamp") String unix_time_stamp,
            @Field("local_date_time") String local_date_time,
            @Field("priority") String priority,
            @Field("message_body") String message_body
    );

/*    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_SEND_EVENTS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();*/
}
