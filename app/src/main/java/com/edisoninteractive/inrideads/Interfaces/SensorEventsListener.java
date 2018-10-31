package com.edisoninteractive.inrideads.Interfaces;

/**
 * Created by Alex Angan one fine day
 */

public interface SensorEventsListener
{
    void onFaceDetected(int facesCount);

    void onFaceMissing(int facesCount);
}
