package com.edisoninteractive.inrideads.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.HEALTH_API_AUTHORITY_ADDRESS;

/**
 * Gathers and reports vital diagnostic data to server
 * every 5 minutes
 */

public class HealthMonitoringService extends Service {

    /********************************** constants ************************************/

    private final long TIMER_INTERVAL_MILLIS = 300000;
    private final String UNITS_API_PATH = "units";
    private final String MONITOR_CONTACT_API_PATH = "monitor-contact";


    //********************************* class members ********************************/
    private int numConnectionErrors = 0;
    private String unit_id = null;
    private PackageInfo monitorInfo;
    private PackageInfo updaterInfo;
    private PackageInfo mediaAppInfo;
    private PackageInfo launcherInfo;
    private String monitorVersionString;
    private String updaterVersionString;
    private String mediaAppVersionString;
    private String launcherAppVersionString;
    private Handler delayer;
    private boolean isRunning;
    private OkHttpClient okHttpClient;

    //***************************** overridden public methods ****************************//

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(GlobalConstants.APP_LOG_TAG, "HealthStateMonitorService onStartCommand");


        okHttpClient = new OkHttpClient();

        if(!isRunning){
            delayer = new Handler();
            resetDelayer();
            doContact();
        }else {
            Log.w(GlobalConstants.APP_LOG_TAG, "HealthStateMonitorService is running already");
        }

        return START_STICKY;
    }



    //****************************** private methods ****************************************//

    private void resetDelayer(){

        delayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                doContact();
                resetDelayer();
            }
        }, TIMER_INTERVAL_MILLIS);
    }

    private void updateUnitId(){
        try{
            SharedPreferences appPreferences = getApplicationContext().getSharedPreferences(GlobalConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
            unit_id = appPreferences.getString("unit_id", "0000");
        }catch (Exception exc){
            unit_id = null;
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to get unit id");
            Crashlytics.logException(exc);
        }
    }

    private String getDateTimeString(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        String dateTimeString = sdf.format(new Date()) + " " + getCurrentTimezoneOffset();
        return dateTimeString;
    }

    public String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = "GMT "+(offsetInMillis >= 0 ? "+" : "-") + offset;

        return offset;
    }

    private String getFreeSpaceExternal(){
        long freeBytesExternal = 0;
        String result = "N/A";

        try{
            freeBytesExternal  = new File(getExternalFilesDir(null).toString()).getFreeSpace();
            DecimalFormat df = new DecimalFormat("#.00");
            result = df.format(((double) freeBytesExternal)/1000000000) + " GB";
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to get free space on external drive");
        }

        return result;
    }

    @SuppressWarnings("all")
    private String getICCID(){
        String result = "N/A";
        TelephonyManager telephonyManager;

        try{
            telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            result = telephonyManager.getSimSerialNumber();

        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to get sim serial number");
        }

        return result;
    }

    @SuppressWarnings("all")
    private String getIMEI(){

        String result = "N/A";

        TelephonyManager telephonyManager;

        try{
            telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            result = telephonyManager.getDeviceId();
        }catch(Throwable error){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to extract IMEI");
        }

        return result;
    }

    private boolean getWifiStatus(){
        try{
            WifiManager wifiManager;
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiEnabled();
        }catch (Exception e){
            Log.e(GlobalConstants.APP_NAME, "Failed to get wifi status");
        }
        return false;
    }

    public String getConnectionType(){

        try{
            final android.net.ConnectivityManager mManager =
                    (android.net.ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo =  mManager.getActiveNetworkInfo();

            if(networkInfo!=null || networkInfo.isConnected()){

                switch (networkInfo.getType()){

                    case ConnectivityManager.TYPE_WIFI:
                        return "Wi-Fi";

                    case ConnectivityManager.TYPE_MOBILE:
                        return "mobile";


                    default:
                        return "other";

                }
            }
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to fetch connection type");
        }


        return "N/A";
    }


    private String getSerial(){

        try{
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
                return Build.getSerial();
            }else{
                return Build.SERIAL;
            }
        }catch (Throwable throwable){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to extract device serial");
            return "null";
        }

    }

    private void doContact(){

        updateAppsInfo();

        if(monitorVersionString!=null){
            // Do not send health data if monitor app is installed
            return;
        }


        int rnd = (int) (Math.random()*Integer.MAX_VALUE);
        updateUnitId();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(HEALTH_API_AUTHORITY_ADDRESS)
                .appendPath(UNITS_API_PATH)
                .appendPath(MONITOR_CONTACT_API_PATH)
                .appendQueryParameter("rnd", Integer.toString(rnd))
                .appendQueryParameter("unit_id", unit_id)
                .appendQueryParameter("uuid", SystemUtils.getDeviceUUID(getApplicationContext()))
                .appendQueryParameter("dateTime", getDateTimeString())
                .appendQueryParameter("monitor_version", monitorVersionString)
                .appendQueryParameter("media_app_version", mediaAppVersionString)
                .appendQueryParameter("updater_version", updaterVersionString)
                .appendQueryParameter("launcher_version", launcherAppVersionString)
/*                .appendQueryParameter("gps_satellites", getGpsSatellites())
                .appendQueryParameter("gps_location_fix", getGpsLocationFix())*/
                .appendQueryParameter("free_space", getFreeSpaceExternal())
                .appendQueryParameter("iccid", getICCID())
                .appendQueryParameter("getIMEI", getIMEI())
                .appendQueryParameter("timezone_local", getCurrentTimezoneOffset())
                .appendQueryParameter("android_version", Build.VERSION.RELEASE)
                .appendQueryParameter("build_number", Build.DISPLAY)
                .appendQueryParameter("build_device", Build.DEVICE)
                .appendQueryParameter("build_board", Build.BOARD)
                .appendQueryParameter("build_serial", getSerial())
                .appendQueryParameter("model_details", Build.MODEL)
                .appendQueryParameter("build_fingerprint", Build.FINGERPRINT)
                .appendQueryParameter("build_hardware", Build.HARDWARE)
                .appendQueryParameter("wifi_status", Boolean.toString(getWifiStatus()))
                .appendQueryParameter("generated_by", GlobalConstants.INRIDEADS_APP_PACKAGE)
                .appendQueryParameter("connection_type", getConnectionType());


        Request request = new Request.Builder()
                .url(builder.build().toString())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }

    private void updateAppsInfo(){

            PackageManager pm = getApplicationContext().getPackageManager();

            monitorVersionString = null;
            updaterVersionString = null;
            mediaAppVersionString = null;
            launcherAppVersionString = null;

            try{
                monitorInfo = pm.getPackageInfo(GlobalConstants.EDISON_MONITOR_PACKAGE , 0);
                monitorVersionString = monitorInfo.versionName;
            }catch (Exception monitorExc){
                Log.w(GlobalConstants.APP_LOG_TAG, "Package info missing for " +  GlobalConstants.EDISON_MONITOR_PACKAGE);
            }

            try{
                updaterInfo = pm.getPackageInfo(GlobalConstants.EDISON_UPDATER_PACKAGE, 0);
                updaterVersionString = updaterInfo.versionName;
            }catch (Exception updaterExc){
                Log.w(GlobalConstants.APP_LOG_TAG, "Package info missing for " + GlobalConstants.EDISON_UPDATER_PACKAGE);
            }

            try{
                mediaAppInfo = pm.getPackageInfo(GlobalConstants.INRIDEADS_APP_PACKAGE, 0);
                mediaAppVersionString = mediaAppInfo.versionName;
            }catch (Exception getMediaAppKxExc){
                Log.w(GlobalConstants.APP_LOG_TAG, "Package info missing for inrideads");
            }

            try{
                launcherInfo = pm.getPackageInfo(GlobalConstants.LAUNCHER_APP_PACKAGE, 0);
                launcherAppVersionString = launcherInfo.versionName;
            }catch (Exception launchAppExc){
                launcherAppVersionString = "null";
                Log.w(GlobalConstants.APP_LOG_TAG, "Package info missing for " + GlobalConstants.LAUNCHER_APP_PACKAGE);
            }

    }






}
