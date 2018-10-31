package com.edisoninteractive.inrideads.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.MAKE_SCREENSHOT;

/**
 * Created by Alex Angan on 02.08.2018.
 */

public class MakeScreenshotService extends IntentService
{
    private static final String className = MakeScreenshotService.class.getSimpleName();
    private Intent uploadIntent;
    private AtomicInteger waitingIntentCount;

    public MakeScreenshotService()
    {
        super(className);
    }

    @Override
    public void onCreate()
    {
        waitingIntentCount = new AtomicInteger(0);
        uploadIntent = new Intent(this, UploadService.class);

        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId)
    {
        waitingIntentCount.set(waitingIntentCount.incrementAndGet());

        Log.i(APP_LOG_TAG, className + " onStartCommand, queue size = " + String.valueOf(waitingIntentCount.intValue()));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        waitingIntentCount.set(waitingIntentCount.decrementAndGet());

        Log.i(APP_LOG_TAG, className + " onHandleIntent start, queue size =  " + String.valueOf(waitingIntentCount.intValue()));

        if (MAKE_SCREENSHOT.equals(intent.getAction()))
        {
            if (waitingIntentCount.intValue() == 0)
            {
                Log.i(APP_LOG_TAG, className + " " + MAKE_SCREENSHOT);

                Bundle b = new Bundle();

                try
                {
                    byte[] bytes = SystemUtils.getScreenshotBytesOnRootedDevice();

                    String length = bytes != null ? String.valueOf(bytes.length) : "";

                    Log.i(APP_LOG_TAG, className + " bytes: " + length);

                    sendImageBytesToServer(this, bytes);

                } catch (Exception e)
                {
                    b.putString(Intent.EXTRA_TEXT, e.toString());
                }
            } else
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " queue overflow,  waitingIntentCount: " + waitingIntentCount.intValue());
            }
        }
    }

    private void sendImageBytesToServer(Context context, byte[] imageBytes)
    {
        if (null != imageBytes && NetworkUtils.isNetworkAvailable(context))
        {
            uploadIntent.putExtra(UploadService.EXTRA_IMAGE_BYTES_KEY, imageBytes);
            String unitId = GlobalConstants.UNIT_ID;
            uploadIntent.putExtra(UploadService.EXTRA_UNIT_ID_KEY, unitId);
            context.startService(uploadIntent);
        }
    }
}
