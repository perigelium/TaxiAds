package com.edisoninteractive.inrideads.EventHandlers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.edisoninteractive.inrideads.Helpers.CameraSourcePreview;
import com.edisoninteractive.inrideads.Helpers.GraphicFaceTracker;
import com.edisoninteractive.inrideads.Helpers.GraphicOverlay;
import com.edisoninteractive.inrideads.Interfaces.FaceTrackingListener;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.edisoninteractive.inrideads.inRideAdsApp;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.NoSuchElementException;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan one fine day
 */

public class CameraFaceDetector
{
    private SurfaceHolder mSurfaceHolder;
    private GraphicOverlay mGraphicOverlay;
    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private boolean faceDetectionRunning;
    private Activity activity;
    private FaceTrackingListener faceTrackingListener;
    private DialogInterface.OnClickListener listener;
    private int facesCount;
    private final String className = CameraFaceDetector.class.getSimpleName();

    public CameraFaceDetector(final FaceTrackingListener faceTrackingListener, final Activity activity, final View rootView)
    {
        this.activity = activity;
        this.faceTrackingListener = faceTrackingListener;

        mPreview = (CameraSourcePreview) rootView.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) rootView.findViewById(R.id.faceOverlay);

        SurfaceView mSurfaceView = (SurfaceView) rootView.findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();

        listener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        };

        PackageManager packageManager = activity.getPackageManager();

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " Frontal camera not found !");
        } else
        {
            if (SystemUtils.isGooglePlayServicesUpToDateAndAvailable(inRideAdsApp.get()))
            {
                if (!createCameraSource())
                {
                    Log.i(APP_LOG_TAG, className + " starting old face detection api...");
                    startOldCameraFaceDetection(activity);
                } else
                {
                    startCameraSource();
                }
            } else
            {
                Log.i(APP_LOG_TAG, className + " starting old face detection api...");
                startOldCameraFaceDetection(activity);
            }
        }
    }

    private void startOldCameraFaceDetection(final Activity activity)
    {
        faceDetectionRunning = false;

        try
        {
            final Camera mCamera = getFrontFacingCamera();

            if (mCamera != null)
            {
                Log.i(APP_LOG_TAG, className + " Front facing camera opened successfully");

                final Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener()
                {
                    @Override
                    public void onFaceDetection(Camera.Face[] faces, Camera camera)
                    {
                        if (faces.length > facesCount)
                        {
                            Log.d(APP_LOG_TAG, className + " face detected");

                            faceTrackingListener.onFaceDetected(faces.length);
                        } else if (faces.length < facesCount)
                        {
                            faceTrackingListener.onFaceMissing(faces.length);
                            Log.d(APP_LOG_TAG, className + " face missing");
                        }
                        facesCount = faces.length;
                    }
                };

                mSurfaceHolder.addCallback(new SurfaceHolder.Callback()
                {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder)
                    {
                        mCamera.setFaceDetectionListener(faceDetectionListener);
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
                    {
                        try
                        {
                            mCamera.setPreviewDisplay(mSurfaceHolder);

                            mCamera.startPreview();

                            //mCamera.autoFocus(autoFocusCallback);
                            if (mCamera.getParameters().getMaxNumDetectedFaces() == 0)
                            {
                                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " Faces detection not supported");
                            } else
                            {
                                if (!faceDetectionRunning)
                                {
                                    mCamera.startFaceDetection();
                                    faceDetectionRunning = false;
                                }

                                Log.i(APP_LOG_TAG, className + " Faces detection supported");
                            }
                        } catch (Exception e)
                        {
                            Log.w(APP_LOG_TAG, className + " on Surface changed exception, " + e.getMessage());
                        }
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder)
                    {
                        try
                        {
                            if (faceDetectionRunning)
                            {
                                mCamera.stopFaceDetection();
                                mCamera.stopPreview();
                            }

                            Log.i(APP_LOG_TAG, className + " Front facing camera stopped");
                        } catch (Exception e)
                        {
                            Log.w(APP_LOG_TAG, className + " on Surface destroyed exception, " + e.getMessage());
                        }
                    }
                });
            } else
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " Front facing camera open failed");
            }
        } catch (Exception e)
        {
            Log.w(APP_LOG_TAG, className + " Front facing camera opening exception, " + e.getMessage());
        }
    }

    private Camera getFrontFacingCamera() throws NoSuchElementException
    {
        try
        {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++)
            {
                Camera.getCameraInfo(cameraIndex, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                {
                    try
                    {
                        return Camera.open(cameraIndex);
                    } catch (RuntimeException e)
                    {
                        Log.w(APP_LOG_TAG, className + " get Front facing camera exception, " + e.getMessage());
                    }
                }
            }
        } catch (Exception e)
        {
            Log.w(APP_LOG_TAG, className + " Front facing camera opening exception, " + e.getMessage());
        }
        return null;
    }

    public void stopCamera()
    {
        try
        {
            if (mPreview != null)
            {
                mPreview.stop();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean createCameraSource()
    {
        Context context = activity;
        FaceDetector detector = new FaceDetector.Builder(context).setClassificationType(FaceDetector.FAST_MODE).build();

        detector.setProcessor(new MultiProcessor.Builder<>(new CameraFaceDetector.GraphicFaceTrackerFactory()).build());

        if (!detector.isOperational())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " Face detector dependencies are not yet available.");

            return false;
        } else
        {
            Log.i(APP_LOG_TAG, className + " Face detector is operational.");
        }

        mCameraSource = new CameraSource.Builder(context, detector).setRequestedPreviewSize(1, 1).setFacing(CameraSource.CAMERA_FACING_FRONT).setRequestedFps(15.0f).build();

        return true;
    }

    private void startCameraSource()
    {
        if (mCameraSource != null)
        {
            try
            {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " Unable to start camera source, " + e.getMessage());

                mCameraSource.release();
                mCameraSource = null;
            }

            if (mCameraSource != null)
            {
                Log.i(APP_LOG_TAG, className + " Camera source started successfully");
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face>
    {
        @Override
        public Tracker<Face> create(Face face)
        {
            GraphicFaceTracker graphicFaceTracker = new GraphicFaceTracker(mGraphicOverlay, activity);
            graphicFaceTracker.setFaceTrackingListener(faceTrackingListener);
            return graphicFaceTracker;
        }
    }
}
