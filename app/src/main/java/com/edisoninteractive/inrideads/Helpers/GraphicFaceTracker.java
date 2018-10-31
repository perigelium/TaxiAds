package com.edisoninteractive.inrideads.Helpers;

import android.app.Activity;
import android.util.Log;

import com.edisoninteractive.inrideads.Interfaces.FaceTrackingListener;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan one fine day
 */

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class GraphicFaceTracker extends Tracker<Face>
{
    private GraphicOverlay mOverlay;
    private Activity activity;

    private FaceTrackingListener faceTrackingListener;

    public void setFaceTrackingListener(FaceTrackingListener faceTrackingListener) {
        this.faceTrackingListener = faceTrackingListener;
    }

    public GraphicFaceTracker(GraphicOverlay overlay, Activity activity)
    {
        mOverlay = overlay;
        this.activity = activity;
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item)
    {
        Log.d(APP_LOG_TAG, "face detected");
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face)
    {
        int facesFound = detectionResults.getDetectedItems().size();

        faceTrackingListener.onFaceDetected(facesFound);
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults)
    {
        int facesFound = detectionResults.getDetectedItems().size();

        Log.d(APP_LOG_TAG, "face missing");
        faceTrackingListener.onFaceMissing(facesFound);
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone()
    {
        Log.d(APP_LOG_TAG, "face gone");
    }
}