package com.edisoninteractive.inrideads.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;
import com.edisoninteractive.inrideads.Entities.Macrocommands;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;

/**
 * Created by Creator on 3/31/2018.
 *
 * Usage: adb shell am broadcast -a com.edisoninteractive.intent.action.NEW_LOCATION --es sms_body "latitude:longitude"
 */

public class FakeLocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.w(GlobalConstants.APP_LOG_TAG, "NewLocationReceiver got new external location");

        try{

            String locationString = intent.getStringExtra("location");
            String[] locationParts = locationString.split(":");
            double lat = Double.valueOf(locationParts[0]);
            double lon = Double.valueOf(locationParts[1]);

            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            newLocation.setLatitude(lat);
            newLocation.setLongitude(lon);

            LocationTrackPoint.unixTimeStamp = System.currentTimeMillis();
            LocationTrackPoint.mlastLocation = newLocation;

            EventManager.getInstance().notify(Macrocommands.LOCATION_UPDATED, null);
            Log.w(GlobalConstants.APP_LOG_TAG, "New external location  " + lat + " : " + lon);

            try {
                Toast.makeText(context,"New external location:" + locationString, Toast.LENGTH_LONG).show();
            }catch (Exception exc){
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to show toast");
            }

            /*if(lat!=-1 && lon!=-1){
                Location newLocation = new Location(LocationManager.GPS_PROVIDER);
                newLocation.setLatitude(lat);
                newLocation.setLongitude(lon);

                Date date = new Date();
                LocationTrackPoint.unixTimeStamp = date.getTime();
                LocationTrackPoint.mlastLocation = newLocation;

                EventManager.getInstance().notify(Macrocommands.LOCATION_UPDATED, null);
            }else{
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to fetch lat and lon values at NewLocationReceiver");
            }*/

        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "NewLocationReceiver: Failed to fetch lat and lon values");
        }

    }
}
