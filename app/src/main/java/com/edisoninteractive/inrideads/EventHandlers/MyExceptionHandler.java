package com.edisoninteractive.inrideads.EventHandlers;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by mdumik on 1/26/2018.
 */

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private Application application;

    public MyExceptionHandler(Application app)
    {
        this.application = app;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        logException(ex);
        sendCrashBroadcast();
        SystemUtils.restartActivity(application, true);
    }

    private void logException(Throwable ex)
    {
        Log.e(APP_LOG_TAG, "Uncaught exception: " + ex.getMessage());
        Crashlytics.log("Unit ID = " + GlobalConstants.UNIT_ID);
        Crashlytics.log("UUID = " + SystemUtils.getDeviceUUID(application));
        Crashlytics.logException(ex);
    }

    private void sendCrashBroadcast()
    {
        final Intent intent = new Intent();
        intent.setAction("com.edisoninteractive.inrideads.CRASH");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.edisoninteractive.inrideads", "com.edisoninteractive.inrideads.CRASH"));
        application.sendBroadcast(intent);
    }
}
