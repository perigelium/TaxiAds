package com.edisoninteractive.inrideads.Utils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;

import static android.media.AudioManager.ADJUST_LOWER;
import static android.media.AudioManager.ADJUST_RAISE;

/**
 * Created by mdumik on 04.01.2018.
 */

public class VolumeUtils
{
    public static void increaseVolume(Context context)
    {
        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if(audioManager == null)
        {
            return;
        }

        try
        {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, ADJUST_RAISE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, ADJUST_RAISE, 0);
        } catch (Exception ex)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, "Failed to increase volume");
            Crashlytics.logException(ex);
        }
    }

    public static void decreaseVolume(Context context)
    {
        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if(audioManager == null)
        {
            return;
        }

        try
        {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, ADJUST_LOWER, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, ADJUST_LOWER, 0);
        } catch (Exception ex)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, "Failed to decrease volume");
            Crashlytics.logException(ex);
        }
    }

    public static void setVolume(Context context, Double value)
    {
        Log.i(GlobalConstants.APP_LOG_TAG, "Setting volume to " + value);

        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if(audioManager == null)
        {
            return;
        }

        try
        {
            int volume = (int) (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * value);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume , 0);
        } catch (Exception ex)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, "Failed to set up volume");
        }
    }
}
