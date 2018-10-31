package com.edisoninteractive.inrideads.EventHandlers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.Config_JS;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.Macro;
import com.edisoninteractive.inrideads.Entities.Macrocommands;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;

import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Receivers.PowerConnectionReceiver.ACC_OFF_TIME_STAMP;

/**
 * Created by mdumik on 02.01.2018.
 */

public class PowerConnectionManager
{
    private static PowerConnectionManager instance = new PowerConnectionManager();
    private static String className = PowerConnectionManager.class.getSimpleName();
    private static boolean externalPowerConnected = true;


    public static PowerConnectionManager getInstance()
    {
        if (instance == null)
        {
            instance = new PowerConnectionManager();
        }
        return instance;
    }


    public static void syncPowerState(Context context)
    {
        onPowerStateChanged(instance.isPowerConnected(context), context);
    }

    public static void onPowerStateChanged(boolean isConnected, Context context)
    {
        // Run config commands only if power state has been changed
        if (externalPowerConnected == isConnected)
        {
            //do not do anything
            return;
        }

        Log.d(GlobalConstants.APP_LOG_TAG, "PowerConnectionManager.onPowerStateChanged to " + isConnected);
        externalPowerConnected = isConnected;

        if (!isConnected)
        {
            try
            {
                SharedPreferences mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                mSettings.edit().putLong(ACC_OFF_TIME_STAMP, System.currentTimeMillis()).apply();
            } catch (Exception ex)
            {
                Log.d(GlobalConstants.APP_LOG_TAG, "PowerConnectionManager.onPowerStateChanged " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Send notify config event subscribers
        String configEventType = isConnected ? Macro.Event.Config.ON_POWER_ON : Macro.Event.Config.ON_POWER_OFF;
        List<Command> configCommands;

        Config_JS config_js = FileUtils.readMediaInterfaceConfigObject();
        if (null != config_js && null != config_js.params && config_js.params.onPowerOn != null  && config_js.params.onPowerOff != null)
        {
            try
            {
                configCommands = isConnected ? config_js.params.onPowerOn.commands : config_js.params.onPowerOff.commands;

                if (null != configCommands)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, "onPowerStateChanged -> EventManager-notify: " + configEventType);
                    EventManager.getInstance().notify(configEventType, configCommands);

                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, "onPowerStateChanged: Exception : " + ex.getMessage());
            }
        }

        // Notify internal event subscribers
        String internalEventType = isConnected ? Macro.Event.Internal.POWER_CONNECTED : Macro.Event.Internal.POWER_DISCONNECTED;
        SystemCommandManager.getInstance(context).handleInternalEvent(internalEventType);
    }

    public boolean isPowerConnected(Context context) throws NullPointerException
    {

        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;

    }

    public boolean isDeviceCharging(Context context) throws NullPointerException
    {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public void execPowerOffModeCommandsIfNotConnected(Context context)
    {


        PowerConnectionManager powerManager = PowerConnectionManager.getInstance();

        try
        {
            if (!powerManager.isPowerConnected(context))
            {
                Config_JS config_js = FileUtils.readMediaInterfaceConfigObject();
                if (null != config_js && null != config_js.params && null != config_js.params.onStartInPowerOffMode && null != config_js.params.onStartInPowerOffMode.commands)
                {
                    List<Command> commands = config_js.params.onStartInPowerOffMode.commands;
                    EventManager.getInstance().notify(Macrocommands.ON_START_IN_POWER_OFF_MODE, commands);
                }
            } else
            {
                Log.i(GlobalConstants.APP_LOG_TAG, className + ": execPowerOffModeCommandsIfNotConnected: Power is connected");
            }
        } catch (Exception ex)
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": execPowerOffModeCommandsIfNotConnected exception, " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
