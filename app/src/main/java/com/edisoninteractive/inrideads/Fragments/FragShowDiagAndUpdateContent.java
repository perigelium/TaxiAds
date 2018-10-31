package com.edisoninteractive.inrideads.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.Counters;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;
import com.edisoninteractive.inrideads.EventHandlers.CameraFaceDetector;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.NetworkAvailability;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.Interfaces.BoundServiceListener;
import com.edisoninteractive.inrideads.Interfaces.CustomEventListener;
import com.edisoninteractive.inrideads.Interfaces.FaceTrackingListener;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Services.SyncFiles;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.edisoninteractive.inrideads.databinding.FragShowDiagAndUpdateContentBinding;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ACTION_MAKE_SYNC_CONTACT;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.LONG_PRESS_TIMEOUT;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.LOCATION_UPDATED;

/**
 * Created by Alex Angan one fine day
 */

public class FragShowDiagAndUpdateContent extends Fragment implements CustomEventListener, FaceTrackingListener
{
    Activity activity;

    private Counters counters;
    private Button btnRetrySyncData;
    private TextView tvLocation;
    private FrameLayout flCameraView;
    private CameraFaceDetector cameraFaceDetector;
    private TextView tvFacesCount;
    private ImageView ivMapPicture;
    private BroadcastReceiver connectivityReceiver;
    private NetworkAvailability networkAvailability;
    Intent intentSyncFiles;
    boolean boundToSyncFiles = false;
    private SyncFiles syncFiles;
    private ServiceConnection syncFilesSrvConnection;
    private String className = getClass().getSimpleName();
    private TextView tvOnlineStatus;
    private TextView tvUpdateContentStatusIdle;
    private FrameLayout flUpdateContentMinView;
    FragShowDiagAndUpdateContentBinding dataBinding;

    public FragShowDiagAndUpdateContent()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activity = getActivity();

        EventManager eventManager = EventManager.getInstance();
        eventManager.subscribe(this);

        intentSyncFiles = new Intent(activity.getApplicationContext(), SyncFiles.class);

        syncFilesSrvConnection = new ServiceConnection()
        {

            public void onServiceConnected(ComponentName name, final IBinder binder)
            {
                Log.i(APP_LOG_TAG, className + ": SyncFiles - Service Connected");

                boundToSyncFiles = true;
                syncFiles = ((SyncFiles.SyncFilesBinder) binder).getService();

                ((SyncFiles.SyncFilesBinder) binder).setListener(new BoundServiceListener()
                {
                    @Override
                    public void sendProgress(double progress)
                    {
                        // Use this method to update download progress
                    }

                    @Override
                    public void finishedDownloading()
                    {
/*                        if (successfully)
                        {
                            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": openDisplayView called");

                            if (!activity.isDestroyed() && !activity.isFinishing() && FragShowDiagAndUpdateContent.this.isVisible())
                            {
                                SystemUtils.restartActivity(activity, true);
                            }
                        }
                        else
                        {*/
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                btnRetrySyncData.setEnabled(true);
                            }
                        });

                    }
                });

                counters = syncFiles.counters;
                dataBinding.setCounters(counters);

                counters.setStateRequestingContent(false);
                counters.setStateDownloadingContent(false);
                counters.setSyncRequestSucceeded(true);

                Intent intent = new Intent(getActivity(), SyncFiles.class);
                activity.stopService(intent);

                intent.setAction(ACTION_MAKE_SYNC_CONTACT);
                intent.putExtra("forced", true);
                activity.startService(intent);
            }

            public void onServiceDisconnected(ComponentName name)
            {
                Log.d(APP_LOG_TAG, className + ": SyncFiles - Service Disconnected");
                boundToSyncFiles = false;
            }
        };

        connectivityReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (context == null)
                {
                    return;
                }

                if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
                {
                    Log.i(APP_LOG_TAG, className + ": Internet status offline");

                    tvOnlineStatus.setText("Waiting for being online...");

                    flUpdateContentMinView.setVisibility(View.GONE);
                    tvUpdateContentStatusIdle.setVisibility(View.VISIBLE);
                } else
                {
                    Log.i(APP_LOG_TAG, className + ": Internet status Online");
                    tvOnlineStatus.setText("Online");

                    flUpdateContentMinView.setVisibility(View.VISIBLE);
                    tvUpdateContentStatusIdle.setVisibility(View.GONE);

                    bindServiceConnection();
                }
            }
        };
    }

    private void bindServiceConnection()
    {
        if (!boundToSyncFiles)
        {
            if(SystemUtils.isServiceRunning(activity, SyncFiles.class))
            {
                Intent intent = new Intent(getActivity(), SyncFiles.class);

                for (int i = 0; i < 5; i++)
                {
                    if (!activity.stopService(intent))
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": Unable to stop service SyncFiles");

                        try
                        {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    } else
                    {
                        Log.i(APP_LOG_TAG, className + ": SyncFiles service has stopped successfully");
                        break;
                    }
                }
            }

            activity.bindService(intentSyncFiles, syncFilesSrvConnection, Context.BIND_AUTO_CREATE);
            boundToSyncFiles = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.frag_show_diag_and_update_content, container, false);
        View rootView = dataBinding.getRoot();

        flUpdateContentMinView = (FrameLayout) rootView.findViewById(R.id.flUpdateContentMinView);
        final LinearLayout llUpdateContentFullView = (LinearLayout) rootView.findViewById(R.id.llUpdateContentFullView);
        tvUpdateContentStatusIdle = (TextView) rootView.findViewById(R.id.tvUpdateContentStatusIdle);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                llUpdateContentFullView.setVisibility(View.VISIBLE);
                flUpdateContentMinView.setVisibility(View.GONE);
                tvUpdateContentStatusIdle.setVisibility(View.GONE);
            }
        };

/*        SharedPreferences mSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        int lastSyncFilesResult = mSettings.getInt("lastSyncFilesResult", 0);

        if (lastSyncFilesResult != 1)
        {
            handler.post(runnable);
        } else
        {*/
            flUpdateContentMinView.setOnTouchListener(new OnTouchDebouncedListener()
            {
                @Override
                public boolean onTouched(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        handler.postDelayed(runnable, LONG_PRESS_TIMEOUT);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    {
                        long eventDuration = motionEvent.getEventTime() - motionEvent.getDownTime();

                        if (eventDuration < LONG_PRESS_TIMEOUT)
                        {
                            handler.removeCallbacks(runnable);
                        }
                    }
                    return true;
                }
            });
        //}

        flCameraView = (FrameLayout) rootView.findViewById(R.id.flCameraView);

        btnRetrySyncData = (Button) rootView.findViewById(R.id.btnRetrySyncData);

        btnRetrySyncData.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnRetrySyncData.setAlpha(0.4f);
                btnRetrySyncData.setEnabled(false);

                resetSyncSession();
            }
        });

        Button btnCancelAndRestartApp = (Button) rootView.findViewById(R.id.btnRestartApp);
        btnCancelAndRestartApp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SystemUtils.restartActivity(activity, true);
            }
        });

        tvOnlineStatus = (TextView) rootView.findViewById(R.id.tvOnlineStatus);

        TextView tvUnitId = (TextView) rootView.findViewById(R.id.tvUnitId);
        tvUnitId.setText(UNIT_ID);

        tvLocation = (TextView) rootView.findViewById(R.id.tvLocation);
        tvFacesCount = (TextView) rootView.findViewById(R.id.tvFacesCount);
        ivMapPicture = (ImageView) rootView.findViewById(R.id.ivMapPicture);

        return rootView;
    }

    private void resetSyncSession()
    {
        if (syncFiles != null)
        {
            unbindServiceConnection();

            activity.stopService(new Intent(getActivity(), SyncFiles.class));

            try
            {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            bindServiceConnection();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        btnRetrySyncData.setEnabled(false);

        if (cameraFaceDetector == null)
        {
            cameraFaceDetector = new CameraFaceDetector(FragShowDiagAndUpdateContent.this, activity, flCameraView);
        }
        showMapPicture();
    }

    private void showMapPicture()
    {
        Location mLastLocation = LocationTrackPoint.mlastLocation;

        if (mLastLocation != null)
        {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            String strLat = String.valueOf(latitude);
            String strLng = String.valueOf(longitude);

            String strLocation = strLat + " , " + strLng;
            tvLocation.setText(strLocation);

            String url = "http://maps.google.com/maps/api/staticmap?center=" + strLat + "," + strLng + "&zoom=16&size=640x220&sensor=false&markers=color:red|" + strLat + "," + strLng;

            Picasso.with(activity).load(url)
                    //.placeholder(R.drawable.map_placeholder)
                    //.error(R.drawable.map_placeholder_error)
                    //.centerCrop().resize(640, 180)
                    .fit().into(ivMapPicture);
        }
    }

/*    private void performSync()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                syncFiles.startUpdate(true);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }*/

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (cameraFaceDetector != null)
        {
            cameraFaceDetector.stopCamera();
            cameraFaceDetector = null;
        }

/*        if (syncFiles != null)
        {
            syncFiles.setSyncMode(false);
        }*/

        try
        {
            // May throw Exception: receiver not registered
            if (connectivityReceiver != null)
            {
                networkAvailability = NetworkAvailability.getInstance();
                networkAvailability.unregisterNetworkAvailability(getActivity(), connectivityReceiver);
            }
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

        unbindServiceConnection();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        networkAvailability = NetworkAvailability.getInstance();
        networkAvailability.registerNetworkAvailability(activity, connectivityReceiver);
    }

    private void unbindServiceConnection()
    {
        if (boundToSyncFiles)
        {
            activity.unbindService(syncFilesSrvConnection);
            boundToSyncFiles = false;
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        unbindServiceConnection();
    }

    @Override
    public void onFaceDetected(final int facesCount)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String strFacesCount = "Faces: " + facesCount;
                tvFacesCount.setText(strFacesCount);
            }
        });
    }

    @Override
    public void onFaceMissing(final int facesCount)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String strFacesCount = "Faces: " + facesCount;
                tvFacesCount.setText(strFacesCount);
            }
        });
    }

    @Override
    public void processEvent(String eventType, List<Command> command)
    {
        if (this.isVisible() && eventType.equals(LOCATION_UPDATED))
        {
            showMapPicture();
        }
    }
}
