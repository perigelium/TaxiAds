package com.edisoninteractive.inrideads.EventHandlers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.edisoninteractive.inrideads.Interfaces.LocationUpdated;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan one fine day
 */


public class LocationRetriever implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener
{
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FASTEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 30; // 30 meters
    //private static int EXPIRATION_DURATION = 15000;
    //private static int MAXWAITTIME = 600000;

    private Activity activity;

    private LocationUpdated callback;

    public LocationRetriever(Activity activity, LocationUpdated callback)
    {
        this.activity = activity;
        this.callback = callback;

        buildLocationRequest();

        if (checkPlayServices())
        {
            buildGoogleApiClient();
        }

        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.connect();
        }
    }

    private synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);

        if (result != ConnectionResult.SUCCESS)
        {
            if (googleAPI.isUserResolvableError(result))
            {
                return false;
            }

            return false;
        }

        return true;
    }

    private void buildLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);// PRIORITY_BALANCED_POWER_ACCURACY
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        //mLocationRequest.setExpirationDuration(EXPIRATION_DURATION);
        //mLocationRequest.setMaxWaitTime(MAXWAITTIME);
        //mLocationRequest.setNumUpdates(1);
    }

    @Override
    public void onConnected(Bundle arg0)
    {
            startLocationUpdates();
    }

    private void startLocationUpdates()
    {
        if (activity.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || activity.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates()
    {
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        Log.d(APP_LOG_TAG, "Connection failed: ConnectionResult errorCode - " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int arg0)
    {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null)
        {
            mLastLocation = location;

            callback.onLocationChanged();
        }
    }

    public Location getLastLocation()
    {
        return mLastLocation;
    }
}
