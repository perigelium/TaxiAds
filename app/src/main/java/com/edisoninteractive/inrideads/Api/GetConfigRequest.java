package com.edisoninteractive.inrideads.Api;

import android.util.Log;

import com.edisoninteractive.inrideads.Entities.ConfigDataArguments;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_GET_CONFIG_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan one fine day
 */

public class GetConfigRequest extends MyApiRequest
{

    private ConfigDataArguments configDataArguments;
    private final String className = GetConfigRequest.class.getSimpleName();

    public GetConfigRequest(ConfigDataArguments configDataArguments)
    {
        this.configDataArguments = configDataArguments;
    }

    @Override
    public Request buildRequest()
    {
        try
        {
            String url = API_GET_CONFIG_URL;

            MultipartBody.Builder builder = new MultipartBody.Builder();
            Log.i(APP_LOG_TAG, className + ": Build request: MultipartBody.Builder - success");

            builder.setType(MultipartBody.FORM);
            Log.i(APP_LOG_TAG, className + ": Build request: setType - success");
            builder.addFormDataPart("force", configDataArguments.force);
            builder.addFormDataPart("unit_id", configDataArguments.unit_id);
            builder.addFormDataPart("uuid", configDataArguments.uuid);
            builder.addFormDataPart("rnd", configDataArguments.rnd);
            builder.addFormDataPart("wifi_mac", configDataArguments.wifi_mac);
            builder.addFormDataPart("sw_version", configDataArguments.sw_version);

            if ( configDataArguments.latitude != null && !configDataArguments.latitude.isEmpty())
            {
                builder.addFormDataPart("lat", configDataArguments.latitude);
            }

            if ( configDataArguments.longitude != null && !configDataArguments.longitude.isEmpty())
            {
                builder.addFormDataPart("long", configDataArguments.longitude);
            }

            if(configDataArguments.location_accuracy != null && !configDataArguments.location_accuracy.isEmpty())
            {
                builder.addFormDataPart("location_accuracy", configDataArguments.location_accuracy);
            }

            if(configDataArguments.location_provider_name != null && !configDataArguments.location_provider_name.isEmpty())
            {
                builder.addFormDataPart("location_provider_name", configDataArguments.location_provider_name);
            }

            Log.i(APP_LOG_TAG, className + ": Build request: addFormDataPart - success");

            RequestBody requestBody = builder.build();
            Log.i(APP_LOG_TAG, className + ": Build request: requestBody built successfully");


            Request.Builder requestBuilder = new Request.Builder();

            requestBuilder.url(url);
            requestBuilder.post(requestBody);
            Log.i(APP_LOG_TAG, className + ": Build request: requestBuilder posted successfully");

            return requestBuilder.build();
        } catch (Exception e)
        {
            Log.i(APP_LOG_TAG, className + ": Build request failed, " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
