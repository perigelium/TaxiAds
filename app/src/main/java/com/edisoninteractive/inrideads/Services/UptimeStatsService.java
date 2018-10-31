package com.edisoninteractive.inrideads.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.StatsManager;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by mdumik on 21.12.2017.
 */

public class UptimeStatsService extends Service
{
    private static final String className = UptimeStatsService.class.getSimpleName();
    private static final String DB_PATH = DATA_PATH + File.separator + "db" + File.separator + "ti_stats.sqlite";

    private static boolean isRunning;
    private static final long MINUTE = 60 * 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (!isRunning)
        {
            saveStats();
        }
        return START_STICKY;
    }

    private void saveStats()
    {
        isRunning = true;
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                File file = new File(DB_PATH);
                if (file.exists())
                {
                    try
                    {
                        Log.d(GlobalConstants.APP_LOG_TAG, className + ": UptimeStatsService: saveStats() called");
                        StatsManager.getInstance(UptimeStatsService.this).writeStats(StatsManager.UPTIME_TICK, "null", "null", "null");
                    } catch (Exception ex)
                    {
                        Log.d(GlobalConstants.APP_LOG_TAG, className + ": UptimeStatsService: saveStats() Exception : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }, 5000, MINUTE);

    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        isRunning = false;
        SystemUtils.scheduleStartService(this, 10000);
    }
}