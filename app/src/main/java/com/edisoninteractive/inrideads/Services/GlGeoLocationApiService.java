package com.edisoninteractive.inrideads.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GLocation;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.GOOGLE_MAP_LOCATION_API_URL;

/**
 * Created by Alex Angan 2018.04.03
 */

public class GlGeoLocationApiService extends IntentService
{
    private static final String GET_LOCATION = "com.edisoninteractive.inrideads.Services.action.GetGlGeoLocation";

    public GlGeoLocationApiService()
    {
        super("GlGeoLocationApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();

            if (GET_LOCATION.equals(action))
            {
                String mJSONArray_WifiAPs = intent.getStringExtra("wifiAccessPoints");
                NetworkUtils networkUtils = NetworkUtils.getInstance();
                GLocation gLocation = networkUtils.requestGeolocation( getApplicationContext(), GOOGLE_MAP_LOCATION_API_URL, mJSONArray_WifiAPs);

                if(gLocation != null && gLocation.location != null
                        && (gLocation.accuracy >= 2000 || LocationTrackPoint.mlastLocation == null))
                {

                    long unixTime = System.currentTimeMillis();

                    LocationTrackPoint.mlastLocation = new android.location.Location(GlobalConstants.GOOGLE_GEOLOCATION_API_PROVIDER_NAME);
                    LocationTrackPoint.mlastLocation.setLatitude(gLocation.location.lat);
                    LocationTrackPoint.mlastLocation.setLongitude(gLocation.location.lng);
                    LocationTrackPoint.mlastLocation.setAccuracy(gLocation.accuracy);
                    LocationTrackPoint.unixTimeStamp = unixTime;

                    Log.w(APP_LOG_TAG, "Google Location: lat= " + gLocation.location.lat + ", lng: " + gLocation.location.lng + ", accuracy: " + gLocation.accuracy);
                }
            }
        }
    }
}
