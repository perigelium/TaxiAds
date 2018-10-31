package com.edisoninteractive.inrideads.Utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;

/**
 * Created by mdumik on 03.01.2018.
 */

public class BrightnessUtils
{

    private static BrightnessUtils instance;

    public static BrightnessUtils getInstance()
    {
        if (null == instance)
        {
            instance = new BrightnessUtils();
        }
        return instance;
    }


    private BrightnessUtils()
    {

    }

    public void increaseBrightness(Context context)
    {
        float newBrightness;

        try
        {
            if(!SystemUtils.isDeviceRooted()){
                Activity mainActivityRef = (Activity) context;
                Window window = mainActivityRef.getWindow();
                WindowManager.LayoutParams layoutParams = window.getAttributes();

                newBrightness = layoutParams.screenBrightness + 0.1f;
                setBrightness(newBrightness, context);

            } else {
                // Change the screen brightness change mode to manual.
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                int brightnessByte =
                        Settings.System.getInt(
                                context.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS);

                newBrightness = brightnessByte / 255f;

                newBrightness += 0.1f;

                setBrightness(newBrightness, context);
            }
        }
        catch (Exception ex)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, "Utils/BrightnessUtils: unable to increase brightness " + ex.getMessage());
        }
    }

    public void decreaseBrightness(Context context)
    {
        float newBrightness;

        try {

            if(!SystemUtils.isDeviceRooted()){
                Activity mainActivityRef = (Activity) context;
                Window window = mainActivityRef.getWindow();
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                newBrightness = layoutParams.screenBrightness - 0.1f;
                setBrightness(newBrightness, context);
            } else {
                // Change the screen brightness change mode to manual.
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                int brightnessByte =
                        Settings.System.getInt(
                                context.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS);

                newBrightness = brightnessByte / 255f;

                newBrightness -= 0.1f;

                setBrightness(newBrightness, context);
            }

        } catch (Exception ex)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, "Utils/BrightnessUtils: unable to decrease brightness " + ex.getMessage());
        }

    }

    public void setBrightness(float brightnessLevelFloat, Context context)
    {

        // use Settings.System for rooted devices

        float newBrightness = brightnessLevelFloat;

        if(newBrightness > 1f){
            newBrightness = 1f;
        }else if(newBrightness<0){
            newBrightness = 0f;
        }

        if(SystemUtils.isDeviceRooted()){

            try{
                int byteBrightness =  (int) (newBrightness * 255f);
                Log.i(GlobalConstants.APP_LOG_TAG, "Setting brightness to " + newBrightness + "  " + byteBrightness);

                // Change the screen brightness change mode to manual.
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                // Apply the screen brightness value to the system, this will change the value in Settings ---> Display ---> Brightness level.
                // It will also change the screen brightness for the device.
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, byteBrightness);
            }catch (Exception putIntException){
                Log.e(GlobalConstants.APP_LOG_TAG, "BrightnessUtils failed to set brightness via System.putInt");
            }

        }else{
            // use window layout params for non-rooted devices

            try
            {
                Activity mainActivityRef = (Activity) context;
                Window window = mainActivityRef.getWindow();

                if(window!=null){
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.screenBrightness = newBrightness;
                    window.setAttributes(layoutParams);
                }

            } catch (Exception ex)
            {
                Log.w(GlobalConstants.APP_LOG_TAG, "BrightnessUtils failed to set brightness via window attributes, " + ex.getMessage());
            }

        }




    }
}
