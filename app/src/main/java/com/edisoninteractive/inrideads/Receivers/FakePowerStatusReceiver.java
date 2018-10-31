package com.edisoninteractive.inrideads.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.PowerConnectionManager;

public class FakePowerStatusReceiver extends BroadcastReceiver {

    private final String FAKE_STATUS_CHARGING = "1";
    private final String FAKE_STATUS_DISCHARGING = "0";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String status = intent.getStringExtra("status");

            Toast.makeText(context, "FAKE POWER STATUS: " + status,Toast.LENGTH_LONG).show();

            if(status.equals(FAKE_STATUS_CHARGING)){
                PowerConnectionManager.onPowerStateChanged(true, context);
            }else if(status.equals(FAKE_STATUS_DISCHARGING)){
                try {
                    SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
                    preferences.edit().putLong(PowerConnectionReceiver.ACC_OFF_TIME_STAMP, System.currentTimeMillis()).apply();
                }catch (Exception exc){
                    Log.e(GlobalConstants.APP_LOG_TAG, "FakePowerStatusReceiver: Failed to write ACC_OFF_TIME_STAMP");
                    Crashlytics.logException(exc);
                }

                PowerConnectionManager.onPowerStateChanged(false, context);
            }else{
                Log.d(GlobalConstants.APP_LOG_TAG, "FakePowerStatusReceiver.onReceive() unknown status");
            }
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "FakePowerStatusReceiver.onReceive() threw an error");
            exc.printStackTrace();
            Crashlytics.logException(exc);
        }
    }

}
