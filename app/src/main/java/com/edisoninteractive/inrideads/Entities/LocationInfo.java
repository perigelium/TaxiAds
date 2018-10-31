package com.edisoninteractive.inrideads.Entities;

import android.location.Location;

/**
 * Created by Creator on 7/22/2017.
 */

public class LocationInfo {

    private Location location;
    private long detectionTime;


    public void setDetectionTime(long detectionTime){
        this.detectionTime = detectionTime;
    }

    public long getDetectionTime(){
        return detectionTime;
    }

    public Location getLocation(){
        return location;
    }

    public void setLocation(Location newLocation){
        location = newLocation;
    }


}
