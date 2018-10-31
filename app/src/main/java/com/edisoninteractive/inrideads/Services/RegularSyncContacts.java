package com.edisoninteractive.inrideads.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import java.util.Timer;
import java.util.TimerTask;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ACTION_MAKE_SYNC_CONTACT;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ACTION_START_REGULAR_SYNC_CONTACTS;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ACTION_STOP_REGULAR_SYNC_CONTACTS;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.SYNC_FILES_SCHEDULE_DELAY;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.SYNC_FILES_SCHEDULE_PERIOD;

/**
 * Created by Alex Angan on 23.08.2018.
 */

public class RegularSyncContacts extends Service
{
    private static final String className = RegularSyncContacts.class.getSimpleName();
    private Timer tScheduledSyncUpdate;

    public RegularSyncContacts()
    {
        super();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId)
    {
        Log.i(APP_LOG_TAG, className + " onStartCommand");

        String action = intent != null ? intent.getAction() : null;

        if (action != null && action.equals(ACTION_START_REGULAR_SYNC_CONTACTS))
        {
            if (tScheduledSyncUpdate != null)
            {
                tScheduledSyncUpdate.cancel();
            }

            tScheduledSyncUpdate = new Timer();

            TimerTask ttScheduledSyncUpdate = new TimerTask()
            {
                @Override
                public void run()
                {
                    if (NetworkUtils.isNetworkAvailable(RegularSyncContacts.this))
                    {
                        if (!SystemUtils.isServiceRunning(RegularSyncContacts.this, SyncFiles.class))
                        {
                            Intent intent = new Intent(RegularSyncContacts.this, SyncFiles.class);
                            intent.setAction(ACTION_MAKE_SYNC_CONTACT);
                            startService(intent);
                        } else
                        {
                            NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, " Skipping regular sync files contact, previous one is still running");
                        }
                    } else
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, " Skipping regular sync files contact, network connection is not accessible");
                    }
                }
            };

            tScheduledSyncUpdate.schedule(ttScheduledSyncUpdate, SYNC_FILES_SCHEDULE_DELAY, SYNC_FILES_SCHEDULE_PERIOD);
        }

        if (action != null && action.equals(ACTION_STOP_REGULAR_SYNC_CONTACTS))
        {
            if (tScheduledSyncUpdate != null)
            {
                tScheduledSyncUpdate.cancel();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
