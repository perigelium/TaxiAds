package com.edisoninteractive.inrideads;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Bluetooth.BluetoothConnection;
import com.edisoninteractive.inrideads.Entities.Config_JS;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.GeolocationManager;
import com.edisoninteractive.inrideads.EventHandlers.LocationRetriever;
import com.edisoninteractive.inrideads.EventHandlers.MyExceptionHandler;
import com.edisoninteractive.inrideads.EventHandlers.SystemCommandManager;
import com.edisoninteractive.inrideads.Fragments.FragDisplayBlocks;
import com.edisoninteractive.inrideads.Fragments.FragRegistrationMenu;
import com.edisoninteractive.inrideads.Fragments.FragShowDiagAndUpdateContent;
import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.Interfaces.LocationUpdated;
import com.edisoninteractive.inrideads.Presenters.AolAdPlayer;
import com.edisoninteractive.inrideads.Services.ACCMonitoringService;
import com.edisoninteractive.inrideads.Services.MakeScreenshotService;
import com.edisoninteractive.inrideads.Services.RegularSyncContacts;
import com.edisoninteractive.inrideads.Services.ServerStatsSendService;
import com.edisoninteractive.inrideads.Services.UploadService;
import com.edisoninteractive.inrideads.Services.UptimeStatsService;
import com.edisoninteractive.inrideads.Utils.DirectoryUtils;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.ImageUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ACTION_START_REGULAR_SYNC_CONTACTS;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIGS_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_ROOT_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.MAKE_SCREENSHOT;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.MAKE_SCREENSHOTS_SCHEDULE_PERIOD;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.PERMISSION_CONSTANTS;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.RECREATE_ACTIVITY;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.REGULAR_RESTART_APP_PERIOD;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.RESTART_APP;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.STORAGE_DIR;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.LOCATION_UPDATED;
import static com.edisoninteractive.inrideads.Utils.SystemUtils.getMBytesInExternalStorageAvailable;

/**
 * Created by Alex Angan one fine day
 */

public class MainActivity extends Activity implements Communicator, LocationUpdated
{
    private FragmentManager mFragmentManager;
    private int PERMISSION_REQUEST_CODE = 11;
    LocationRetriever locationRetriever;
    Location mLastLocation;
    public EventManager eventsManager;
    DialogInterface.OnClickListener listener;
    private boolean allPermissionsGained;
    public static ArrayMap<String, AolAdPlayer> aolAdPlayers;

    private static final int ENABLE_GPS_REQUEST_CODE = 12;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 13;
    private Timer screenshotTimer;

    final int flagsSystemUI = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    //View.SYSTEM_UI_FLAG_LOW_PROFILE
    private PowerManager.WakeLock wakeLock;
    private BluetoothConnection bluetoothConnection;
    private boolean onePermissionNotGranted;
    private GeolocationManager geolocationManager;
    private final String className = getClass().getSimpleName();
    private Config_JS config_js;
    private View rootView;


    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (!allPermissionsGained)
        {
            return;
        }

        mFragmentManager = getFragmentManager();

        makeScreenshots(rootView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
/*        if (BuildConfig.DEBUG)
        {
            SystemUtils.setVmPolicyInDebugMode();
        }*/

        if ( !BuildConfig.DEBUG)
        {
            Fabric.with(inRideAdsApp.get(), new Crashlytics());

            final MyExceptionHandler myExceptionHandler = new MyExceptionHandler(inRideAdsApp.get());
            Thread.setDefaultUncaughtExceptionHandler(myExceptionHandler);

            new ANRWatchDog(15000) // ANR interval in milliseconds
                    .setANRListener(new ANRWatchDog.ANRListener()
                    {
                        @Override
                        public void onAppNotResponding(ANRError error)
                        {
                            Log.e(APP_LOG_TAG, "onAppNotResponding error: " + error.getMessage());
                            myExceptionHandler.uncaughtException(Thread.currentThread(), error);
                        }
                    }).setIgnoreDebugger(true) // set true in case ANR detects needed in debug mode
                    .start();
        }

        super.onCreate(savedInstanceState);

        //Uninstall prev(AIR) version if its present
        SystemUtils.uninstallAirVersionIfPresent();

        if (!RECREATE_ACTIVITY && savedInstanceState != null)
        {
            this.finish();
        }

        if (RECREATE_ACTIVITY)
        {
            RECREATE_ACTIVITY = false;
        }

        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.rootLayout);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(flagsSystemUI);

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {
            @Override
            public void onSystemUiVisibilityChange(int visibility)
            {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    try
                    {
                        decorView.setSystemUiVisibility(flagsSystemUI);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.d(APP_LOG_TAG, className + ": onCreate - decorView: Underflow in restore - more restores than saves");
                    }
                }
            }
        });

        allPermissionsGained = isAllRequestedPermissionsGranted(PERMISSION_CONSTANTS);

        if (!allPermissionsGained)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestMultiplePermissions(PERMISSION_CONSTANTS);
            }
        }

        if (allPermissionsGained)
        {
            aolAdPlayers = new ArrayMap<>();
            SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            RESTART_APP = false;
            this.eventsManager = EventManager.getInstance();

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyWakeLock");

            if ((wakeLock != null) && !wakeLock.isHeld())  // we have a WakeLock but we don't hold it
            {
                wakeLock.acquire();
            }

            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            getWindow().setAttributes(params);

            mFragmentManager = getFragmentManager();

            config_js = FileUtils.readMediaInterfaceConfigObject();

            DirectoryUtils directoryUtils = new DirectoryUtils();
            boolean allRequiredFilesExist = directoryUtils.doesNecessaryDirectoriesExists();

            if (!allRequiredFilesExist || config_js == null)
            {
                extractAndCopyDefaultDataStructure();
                mSettings.edit().putBoolean("DownloadDataCompleted", false).apply();
                config_js = FileUtils.readMediaInterfaceConfigObject();
            }

            directoryUtils.checkAndAutoCreateAdditionalDirectories();

            FileUtils fileUtils = FileUtils.getInstance();
            fileUtils.updateUnitId(this);

            GlobalConstants.UUID = SystemUtils.getDeviceUUID(getApplicationContext());
            Log.i(APP_LOG_TAG, className + ": UUID = " + GlobalConstants.UUID);
            GlobalConstants.WIFI_MAC = SystemUtils.getWifiMAC(getApplicationContext());
            Log.i(APP_LOG_TAG, className + ": WIFI_MAC = " + GlobalConstants.WIFI_MAC);

            NetworkUtils.getInstance().showAndUploadLogEvent
                    (className, 0, "External storage directory MBytes available = " + String.valueOf(getMBytesInExternalStorageAvailable()));

            if (UNIT_ID != null && !UNIT_ID.equals("0000"))
            {
                //Log.i(APP_LOG_TAG, className + ": started, app version - " + BuildConfig.VERSION_NAME);
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 0,
                        "started, app version - " + BuildConfig.VERSION_NAME);

                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                boolean isLowRamMemory = activityManager != null && activityManager.isLowRamDevice();

                if(isLowRamMemory)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": Low Ram Memory !");
                }

                startService(new Intent(MainActivity.this, ServerStatsSendService.class));
            }

            startService(new Intent(MainActivity.this, UptimeStatsService.class));

            Timer timerRestart = new Timer();

            timerRestart.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    SystemUtils.restartActivity(MainActivity.this, true);
                }
            }, REGULAR_RESTART_APP_PERIOD, REGULAR_RESTART_APP_PERIOD);

            Intent intent = new Intent(this, RegularSyncContacts.class);
            intent.setAction(ACTION_START_REGULAR_SYNC_CONTACTS);
            startService(intent);

            if (isGPS_Enabled())
            {
                startLocationTracking();
            }

            SystemCommandManager systemCommandManager = SystemCommandManager.getInstance(this);
            eventsManager.subscribe(systemCommandManager);

            startService(new Intent(this, ACCMonitoringService.class));

            //bluetoothConnection = new BluetoothConnection(MainActivity.this);

            File fileLastSyncTimestamp = new File(DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/lastSyncTimestamp.evt");
            //int lastSyncFilesResult = mSettings.getInt("lastSyncFilesResult", 0);

            if (!allRequiredFilesExist) // At least one config file in Configs folder missing
            {
                replaceFragment(FragRegistrationMenu.class.getSimpleName(), new FragRegistrationMenu());
            } else if (!fileLastSyncTimestamp.exists())
            {
                replaceFragment(FragShowDiagAndUpdateContent.class.getSimpleName(), new FragShowDiagAndUpdateContent());
            } else if (config_js != null)
            {
                removeFragment(FragDisplayBlocks.class.getSimpleName());
                replaceFragment(FragDisplayBlocks.class.getSimpleName(), new FragDisplayBlocks());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        Log.i(APP_LOG_TAG, className + ": On new intent");

        RECREATE_ACTIVITY = true;
        this.recreate();
    }

    private void extractAndCopyDefaultDataStructure()
    {
        try
        {
            InputStream inputStream = getAssets().open(DATA_ROOT_FOLDER_NAME + ".zip");
            FileUtils.unzipMyZipToFolderStructure(inputStream, STORAGE_DIR.getAbsolutePath());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void startLocationTracking()
    {
        Runnable runnable = null;

        if (SystemUtils.isGooglePlayServicesUpToDateAndAvailable(inRideAdsApp.get())) //  && !Build.MODEL.equals(GlobalConstants.IEI_TX_MODEL_NAME)
        {
            runnable = new Runnable()
            {
                public void run()
                {
                    locationRetriever = new LocationRetriever(MainActivity.this, MainActivity.this);
                }
            };
        } else
        {
            geolocationManager = GeolocationManager.getInstance();
            geolocationManager.init(MainActivity.this, MainActivity.this);
        }

        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onLocationChanged()
    {
        if (locationRetriever != null)
        {
            mLastLocation = locationRetriever.getLastLocation();
        } else if (geolocationManager != null)
        {
            mLastLocation = geolocationManager.getLastLocation();
        }

        if (mLastLocation != null && mLastLocation.getLatitude() != 0 && mLastLocation.getLongitude() != 0)
        {
            LocationTrackPoint.mlastLocation = mLastLocation;

            LocationTrackPoint.unixTimeStamp = System.currentTimeMillis();

            eventsManager.notify(LOCATION_UPDATED, null);

            Log.i(APP_LOG_TAG, className + ": New location received: " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        }
    }

    @Override
    public void onBackPressed()
    {
        closeApp();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestMultiplePermissions(String[] permissions)
    {
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == PERMISSION_REQUEST_CODE)
        {
            onePermissionNotGranted = false;

            for (int i = 0; i < grantResults.length; i++)
            {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    onePermissionNotGranted = true;
                }
            }

            if (onePermissionNotGranted)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": One or more of requested storage permission(s) not granted !");
            }
            RECREATE_ACTIVITY = true;
            this.recreate();
        }
    }

    public boolean isAllRequestedPermissionsGranted(String[] permissions)
    {
        for (String permission : permissions)
        {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        return true;
    }

    public void reloadFragment(String fragmentTag)
    {
        mFragmentManager = getFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentByTag(fragmentTag);

        if (fragment != null)
        {
            Log.i(APP_LOG_TAG, className + ": - reloading fragment " + fragment.getTag());
            FragmentTransaction pmFragmentTransaction = mFragmentManager.beginTransaction();
            pmFragmentTransaction.detach(fragment);
            pmFragmentTransaction.attach(fragment);
            pmFragmentTransaction.commit();
        }
    }

    private void removeFragment(String fragmentTag)
    {
        mFragmentManager = getFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentByTag(fragmentTag);

        if (fragment != null)
        {
            Log.i(APP_LOG_TAG, className + ": - removing fragment " + fragment.getTag());
            FragmentTransaction pmFragmentTransaction = mFragmentManager.beginTransaction();
            pmFragmentTransaction.detach(fragment);
            pmFragmentTransaction.remove(fragment);

            try
            {
                pmFragmentTransaction.commitAllowingStateLoss();
                mFragmentManager.executePendingTransactions();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean isGPS_Enabled()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null)
        {
            return false;
        }

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    @Override
    public void replaceFragment(String fragmentTag, Fragment newFragment)
    {
        mFragmentManager = getFragmentManager();
        Fragment fragReplacement = mFragmentManager.findFragmentByTag(fragmentTag);

        if (fragReplacement == null)
        {
            fragReplacement = newFragment;
        }

        if (!fragReplacement.isAdded())
        {
            FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();

            mFragmentTransaction.replace(R.id.fragContainer, fragReplacement, fragmentTag);

            try
            {
                mFragmentTransaction.commitAllowingStateLoss();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
        {
            reloadFragment(fragmentTag);
        }
    }

    @Override
    public void closeApp()
    {
        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": closeApp called, exiting...");
        this.finish();
        System.exit(0);
    }

    private void makeScreenshots(final View rootView)
    {
        if (SystemUtils.isDeviceRooted())
        {
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    Intent intent = new Intent(MainActivity.this, MakeScreenshotService.class);
                    intent.setAction(MAKE_SCREENSHOT);
                    startService(intent);
                }
//            }, 5000, 1000 * 60 * 15);
            }, 5000, MAKE_SCREENSHOTS_SCHEDULE_PERIOD);

        } else
        {
            screenshotTimer = new Timer();
            screenshotTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            byte[] bytes = ImageUtils.makeScreenshotFromView(rootView);

                            if (null != bytes && NetworkUtils.isNetworkAvailable(MainActivity.this))
                            {
                                Intent intent = new Intent(MainActivity.this, UploadService.class);
                                intent.putExtra(UploadService.EXTRA_IMAGE_BYTES_KEY, bytes);
                                intent.putExtra(UploadService.EXTRA_UNIT_ID_KEY, GlobalConstants.UNIT_ID);
                                MainActivity.this.startService(intent);
                            }
                        }
                    });
                }
            }, 5000, MAKE_SCREENSHOTS_SCHEDULE_PERIOD);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (null != screenshotTimer)
        {
            try
            {
                screenshotTimer.cancel();
            } catch (Exception ex)
            {
                Log.d(APP_LOG_TAG, className + ": Exception while trying to cancel screenshotTimer => " + ex.getMessage());
                ex.printStackTrace();
            } finally
            {
                screenshotTimer = null;
            }
        }
    }
}
