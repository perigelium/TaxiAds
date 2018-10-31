package com.edisoninteractive.inrideads.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Utils.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.SCREEN_BASE_URL;

/**
 * Created by mdumik on 15.12.2017.
 */

public class UploadService extends IntentService
{
    private static final String className = UploadService.class.getSimpleName();
    public static final String EXTRA_IMAGE_BYTES_KEY = "extraImageBytesKey";
    public static final String EXTRA_UNIT_ID_KEY = "extraImageUnitIdKey";

    private byte[] bytes = null;
    private String url = "";
    private OkHttpClient.Builder defaultHttpClient;

    public UploadService()
    {
        super(className);
    }

    @Override
    public void onCreate()
    {
        defaultHttpClient = new OkHttpClient.Builder();
        defaultHttpClient.connectTimeout(1, TimeUnit.MINUTES);
        defaultHttpClient.readTimeout(1, TimeUnit.MINUTES);
        defaultHttpClient.writeTimeout(1, TimeUnit.MINUTES);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        bytes = intent.getByteArrayExtra(EXTRA_IMAGE_BYTES_KEY);

        String unitId = intent.getStringExtra(EXTRA_UNIT_ID_KEY);
        url = SCREEN_BASE_URL + unitId;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.i(APP_LOG_TAG, className + " onHandleIntent");

        OkHttpClient okHttpClient = defaultHttpClient.build();

        Request request = new Request.Builder().url(url).method("POST",
                RequestBody.create(MediaType.parse("application/octet-stream"), bytes)).build();

        Response response = null;

        try
        {
            response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful())
            {
                Log.i(APP_LOG_TAG, className + " upload - success");
            }
        } catch (IOException e)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " upload - failed, " + e.getMessage());
        }

        if (response != null && response.body() != null)
        {
            response.body().close();
        }
    }
}
