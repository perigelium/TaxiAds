package com.edisoninteractive.inrideads.Interfaces;

/**
 * Created by Alex Angan one fine day
 */

public interface FaceTrackingListener
{
    void onFaceDetected(int facesCount);

    void onFaceMissing(int facesCount);
}
