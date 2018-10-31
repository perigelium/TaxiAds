package com.edisoninteractive.inrideads.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.WifiConnectionPoint;
import com.edisoninteractive.inrideads.Services.GlGeoLocationApiService;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Creator on 4/6/2018.
 */

public class WifiAPdataReceiver extends BroadcastReceiver
{
    private String className = getClass().getSimpleName();
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null)
        {
            List<ScanResult> wifiList = wifiManager.getScanResults();

            ArrayList<WifiConnectionPoint> al_WifiConnectionPoints = new ArrayList<>();

            for (int i = 0; i < wifiList.size(); i++)
            {
                ScanResult scanResult = wifiList.get(i);
                WifiConnectionPoint bean = new WifiConnectionPoint();
                bean.macAddress = (scanResult.BSSID);
                bean.signalStrength = (scanResult.level);
                al_WifiConnectionPoints.add(bean);
            }

            Gson gson = new Gson();
            String jsonArray_WifiConnectionPoints = gson.toJson(al_WifiConnectionPoints);

            Log.i(APP_LOG_TAG, className + ": wifi points quantity: " + al_WifiConnectionPoints.size());

            Intent intentServiceStart = new Intent(context, GlGeoLocationApiService.class);
            intentServiceStart.setAction("com.edisoninteractive.inrideads.Services.action.GetGlGeoLocation");
            intentServiceStart.putExtra("wifiAccessPoints", jsonArray_WifiConnectionPoints);
            context.startService(intentServiceStart);
        }

        try
        {
            context.unregisterReceiver(this);
        } catch (Exception e)
        {
            e.printStackTrace();
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": unregisterReceiver threw an exception, " + e.getMessage());
        }
    }
}
