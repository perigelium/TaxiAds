package com.edisoninteractive.inrideads.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.PowerConnectionManager;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

/**
 * This service constantly monitors power connection status
 * and detects connection status changes. It also has an emergency
 * 120 sec power off timer that has to switch off the device in case when
 * there is no external power more than 2 min.
 */

public class ACCMonitoringService extends Service
{
    private Handler shutdownHandler;
    private Handler checkACCHandler = new Handler();
    private boolean accConnected = true;  // We presume that device is powered on when it starts even if it has no extenal power on start
    private String className = getClass().getSimpleName();

    public static boolean running = false;

    private final int NO_POWER_SHUTDOWN_DELAY = 120 * 1000;
    private final int ACC_CHECK_INTERVAL = 1 * 1000;
    private final int CHECK_START_DELAY = 5 * 1000;


    //************************************** Runnables *******************************************//
    private Runnable readACCStatus = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                //1. sync power state for PowerConnectionManager
                PowerConnectionManager.syncPowerState(getApplicationContext());

                //2. update internal flags
                boolean prevAccValue = accConnected;
                accConnected = PowerConnectionManager.getInstance().isPowerConnected(getApplicationContext());

                //3. status change detected
                if (accConnected != prevAccValue)
                {
                    Log.d(GlobalConstants.APP_LOG_TAG, className + ": Read ACC status:" + PowerConnectionManager.getInstance().isPowerConnected(getApplicationContext()));
                    // stop and destroy emergency shutdown timer
                    stopEmergencyShutDownTimer();

                    if (!accConnected)
                    {
                        // Restart emergency shutdown timer if there is no power
                        restartEmergencyShutDownTimer();
                    }
                }

                checkACCHandler.postDelayed(this, ACC_CHECK_INTERVAL);
            } catch (Throwable throwable)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": Failed to execute ACCMonitoringService.readACCStatus, " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    };

    //*********************************** private methods ****************************************//

    private void stopEmergencyShutDownTimer()
    {
        if (shutdownHandler != null)
        {
            try
            {
                shutdownHandler.removeCallbacksAndMessages(null);
                shutdownHandler = null;
            } catch (Exception exc)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": Failed to stop shutdown timer");
                exc.printStackTrace();
            }
        }
    }

    private void restartEmergencyShutDownTimer()
    {
        if(shutdownHandler!=null){
            stopEmergencyShutDownTimer();
        }

        shutdownHandler = new Handler();
        shutdownHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": ACC Emergency timer ended. Shutting the screen down...");
                SystemUtils.shutdownDevice(getApplicationContext());
            }
        }, NO_POWER_SHUTDOWN_DELAY);
    }

    //********************************************************************************************//

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        running = true;

        // Start reading acc status 5 sec after app start
        Handler startupDelayer = new Handler();
        startupDelayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                readACCStatus.run();
            }
        }, CHECK_START_DELAY);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        SystemUtils.scheduleStartService(this, 15);
    }

}
