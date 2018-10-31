package com.edisoninteractive.inrideads.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.edisoninteractive.inrideads.EventHandlers.PowerConnectionManager;
import com.edisoninteractive.inrideads.Services.ACCMonitoringService;


/**
 * Created by mdumik on 02.01.2018.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String ACC_OFF_TIME_STAMP = "ACC_LAST_TIME_STAMP";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Handle connection changes only when ACCMonitoring service is running
        if(ACCMonitoringService.running){
            PowerConnectionManager.syncPowerState(context);
            //Toast.makeText(context, "PowerConnectionReceiver:"+PowerConnectionManager.getInstance().isPowerConnected(context), Toast.LENGTH_LONG).show();
        }






       /* int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;*/

        //todo: FOR TEST: Replace isConnected with isCharging
        //boolean isConnected = PowerConnectionManager.getInstance().isPowerConnected(context);

        /*if (!isConnected) {
            long currentTime = System.currentTimeMillis();
            try {
                SharedPreferences mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                mSettings.edit().putLong(ACC_OFF_TIME_STAMP, currentTime).apply();
            } catch (Exception ex) {
                Log.d(GlobalConstants.APP_LOG_TAG, "GlobalConstants.mSettings Exception " + ex.getMessage());
                ex.printStackTrace();
            }
        }*/
        
        //PowerConnectionManager.onPowerStateChanged(isConnected, context);

        // Code to define what kind of connection is detected: (USB, or AC)
//        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//        boolean usbCharge = chargePlug == BATTERY_PLUGGED_USB;
//        boolean acCharge = chargePlug == BATTERY_PLUGGED_AC;
    }
}
