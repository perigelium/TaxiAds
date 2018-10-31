package com.edisoninteractive.inrideads.EventHandlers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.LocationInfo;
import com.edisoninteractive.inrideads.Interfaces.LocationUpdated;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;

import java.util.Iterator;
import java.util.Vector;

import static android.content.Context.LOCATION_SERVICE;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;


/**
 * Created by Creator on 6/13/2017.
 */

public class GeolocationManager
{
    private static volatile GeolocationManager instance;

    public static GeolocationManager getInstance()
    {
        if (instance == null)
        {
            synchronized (GeolocationManager.class)
            {
                if (instance == null)
                {
                    instance = new GeolocationManager();
                }
            }
        }
        return instance;
    }

    private boolean gpsProviderEnabled = false;
    private boolean networkProviderEnabled = false;

    //******************************** private vars *********************************//
    private LocationManager locationManager;

    private Location unitLocation;
    private long unitLocationTime;
    private Vector<LocationInfo> locationHistory;

    private int satellitesInView = 0;
    private int satellitesInUse = 0;
    private long lastGpsStatusTime = 0;

    private Context context;
    private LocationUpdated callback;
    private String className = GeolocationManager.class.getSimpleName();

    //******************************* initialization ***********************************//

    private GeolocationManager()
    {

    }

    @SuppressWarnings("all")
    public void init(Context context, final LocationUpdated callback)
    {
        this.context = context;
        this.callback = callback;

        Log.d(APP_LOG_TAG, className + ": GeolocationManager started");

/*        try
        {*/
            locationHistory = new Vector<LocationInfo>(100);

            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            
            if(locationManager == null)
            {
                Log.d(APP_LOG_TAG, className + ": GeolocationManager is not accessible");
                return;
            }

            try
            {
                gpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": GPS enabled");
            } catch (Exception exc)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": gps provider disabled");
            }

            try
            {
                networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Network location provider enabled");
            } catch (Exception exc2)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Network location provider disabled");
            }

            // Define a listener that responds to location updates
            LocationListener gpsLocationListener = new LocationListener()
            {
                public void onLocationChanged(Location location)
                {
                    try
                    {
                        Log.i(APP_LOG_TAG, className + ": New GPS location: " + location.getLatitude() + " " + location.getLongitude());

                        makeUseOfNewLocation(location);

                        callback.onLocationChanged();
                    } catch (Exception exc)
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": gpsLocationListener.onLocationChanged failed");
                        Crashlytics.logException(exc);
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras)
                {
                    //Log.d(APP_LOG_TAG, className + ": onStatusChanged:"+status);
                }

                public void onProviderEnabled(String provider)
                {
                    Log.d(APP_LOG_TAG, className + ": gps provider enabled");
                }

                public void onProviderDisabled(String provider)
                {
                    Log.d(APP_LOG_TAG, className + ": gps provider disabled");
                }
            };


            LocationListener networkLocationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location location)
                {
                    try
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": New network location: " + location.getLatitude() + " " + location.getLongitude());
                        makeUseOfNewLocation(location);

                        callback.onLocationChanged();
                    } catch (Exception exc)
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": networkLocationListener.onLocationChangeg threw an error");
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras)
                {
                    Log.d(APP_LOG_TAG, className + ": Network provider status changed");
                }

                @Override
                public void onProviderEnabled(String provider)
                {
                    Log.d(APP_LOG_TAG, className + ": Network location provider enabled");
                }

                @Override
                public void onProviderDisabled(String provider)
                {
                    Log.d(APP_LOG_TAG, className + ": Network location provider disabled");
                }
            };

            if (gpsProviderEnabled)
            {
                if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates
                            (LocationManager.GPS_PROVIDER, GlobalConstants.MIN_GPS_TIME_THRESHOLD_MILLIS, 
                                    GlobalConstants.MIN_GPS_DISTANCE_THRESHOLD_METERS, gpsLocationListener);

                    locationManager.addGpsStatusListener(new GpsStatus.Listener()
                    {
                        @Override
                        public void onGpsStatusChanged(int event)
                        {
                            @SuppressLint("MissingPermission") GpsStatus gpsStatus = locationManager.getGpsStatus(null);

                            if (gpsStatus != null)
                            {
                                Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
                                Iterator<GpsSatellite> sat = satellites.iterator();

                                satellitesInUse = 0;
                                satellitesInView = 0;
                                lastGpsStatusTime = System.currentTimeMillis();

                                while (sat.hasNext())
                                {
                                    GpsSatellite satellite = sat.next();
                                    if (satellite.usedInFix())
                                    {
                                        satellitesInUse++;
                                    } else
                                    {
                                        satellitesInView++;
                                    }
                                }

                                switch (event)
                                {
                                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                                        Log.d(APP_LOG_TAG, className + ": GPS status changed: GPS_EVENT_FIRST_FIX");
                                        break;

/*                                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                                        Log.d(APP_LOG_TAG, className + ": GPS status changed: GPS_EVENT_SATELLITE_STATUS. In use:" + satellitesInUse+" in view: " + satellitesInView);
                                        break;*/

                                    case GpsStatus.GPS_EVENT_STARTED:
                                        Log.d(APP_LOG_TAG, className + ": GPS status changed: GPS_EVENT_STARTED");
                                        break;

                                    case GpsStatus.GPS_EVENT_STOPPED:
                                        Log.d(APP_LOG_TAG, className + ": GPS status changed: GPS_EVENT_STOPPED");
                                        break;
                                }
                            }
                        }
                    });

                    Log.d(APP_LOG_TAG, className + ": GPS listener added...");
                }
            }

            if (networkProviderEnabled)
            {
                if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GlobalConstants.MIN_GPS_TIME_THRESHOLD_MILLIS, GlobalConstants.MIN_GPS_DISTANCE_THRESHOLD_METERS, networkLocationListener);

                    Log.d(APP_LOG_TAG, className + ": Network location listener added...");
                }
            }

    }


    private void makeUseOfNewLocation(Location location)
    {
        try{
            unitLocation = location;
            unitLocationTime = System.currentTimeMillis();

            LocationInfo locationInfo = new LocationInfo();
            locationInfo.setLocation(location);
            locationInfo.setDetectionTime(System.currentTimeMillis());

            locationHistory.insertElementAt(locationInfo, 0);
            locationHistory.setSize(10);

            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Location update: " + location.getLatitude() + "   " + location.getLongitude());
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "GeolocationManager.makeUseOfNewLocation threw an exception");
            Crashlytics.logException(exc);
        }

    }

    public Location getLastLocation()
    {
        return unitLocation;
    }

    public boolean isLastLocationActual()
    {

        if (unitLocation != null)
        {
            long diff = (System.currentTimeMillis() - unitLocationTime) / 1000;
            if (diff < 300)
            {
                return true;
            } else
            {
                return false;
            }
        }
        return false;
    }


    /**************************************************************
     * Provides a count of satellites in view, and satellites in use
     **************************************************************/
    public String getSatellitesDetailsString()
    {

        String result = "satellite data is not available";
        long timeNow = System.currentTimeMillis();

        try
        {
            if ((timeNow - lastGpsStatusTime) > 120 * 1000)
            {
                //reset satellites data if latest gps status was more than 3 minutes ago
                satellitesInUse = 0;
                satellitesInView = 0;
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Reset sat count to 0 as the latest update was >120 sec ago");
            }

            result = String.valueOf(satellitesInView + satellitesInUse);

        } catch (Exception exc)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": Failed to extract satellites data");
        }

        return result;
    }

}
