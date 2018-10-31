package com.edisoninteractive.inrideads.Entities;

import android.Manifest;
import android.os.Environment;

import com.edisoninteractive.inrideads.BuildConfig;

import java.io.File;

/**
 * Created by Alex Angan one fine day
 */

public class GlobalConstants
{
    public static String API_GET_CONFIG_URL = BuildConfig.API_HOSTNAME + "units/contact/";
    public static String API_GET_FILES_URL = BuildConfig.API_HOSTNAME +  "files/getfile";
    public static String API_SEND_EVENTS_URL = BuildConfig.API_HOSTNAME + "units/events/";
    public static String API_UPDATER_CHECK_URL = BuildConfig.API_HOSTNAME + "updater/check/";
    //public static String API_COMPANY_REGISTER_URL = BuildConfig.API_HOSTNAME + "units/company-register";
    public static String API_AOL_CACHE_URL = BuildConfig.API_HOSTNAME + "units/upload-urls/";
    public static final String API_UPLOAD_STATS_URL = BuildConfig.API_HOSTNAME + "statistics/upload";
    public static final String API_UPLOAD_LOGS_URL = "http://askrc.edisoninteractive.com/units/insert-message/";

    public static final  String SCREEN_BASE_URL = BuildConfig.API_HOSTNAME + "units/upload-screenshot?unit_id=";

    public static final String GOOGLE_MAP_LOCATION_API_URL = "https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyB2KitXPhkCx8R1fio-Z_nJH0kD4TiIvPM";

    public static final String HEALTH_API_AUTHORITY_ADDRESS = "api.taxiinteractive.com";

    public static final String DATA_ROOT_FOLDER_NAME = "TaxiInteractive";
    public static final String CONFIGS_FOLDER_NAME = "configs";
    public static final String TEMP_FOLDER_NAME = "tmp";

    public static final String CONFIG_JS_NAME = "config.js";
    public static final String PARAMS_JS_NAME = "params.js";
    public static final String ADSCHEDULE_JS_NAME = "adschedule.js";

    public static final File STORAGE_DIR = Environment.getExternalStorageDirectory();
    public static final String DATA_PATH = STORAGE_DIR.getAbsolutePath() + File.separator + DATA_ROOT_FOLDER_NAME; // Path to EdisonInteractive
    public static final String AD_MOB_TIMESTAMP_PATH = DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/ad_mob_timestamp.evt";

    public static final String APP_PREFERENCES = "mysettings";
    public static final String MAKE_SCREENSHOT = "make_screenshot";

    public static final String APP_NAME = "inrideads";
    public static String UNIT_ID = "0000";
    public static String UUID = "";
    public static String WIFI_MAC = "";
    public static final String APP_LOG_TAG = "edison_inrideads";
    public static boolean RESTART_APP = false;
    public static boolean RECREATE_ACTIVITY = false;
    public static final String SHUTDOWN_INTENT_ACTION = "shut_down_device_edison";
    public static final String REBOOT_DEVICE_INTENT_ACTION = "reboot_device_edison";

    public static final long MIN_GPS_TIME_THRESHOLD_MILLIS = 5000;
    public static final float MIN_GPS_DISTANCE_THRESHOLD_METERS = 70;
    public static final String AIR_VERSION_ID = "air.com.taxiinteractive.android";

    public static final String GOOGLE_GEOLOCATION_API_PROVIDER_NAME = "geolocation_google";
    public static final long LONG_PRESS_TIMEOUT = 2000; // msec
    public static final long HALF_EQUATOR_LENGTH = 20 * 1000 * 1000;  // meters, max real distance between two points on Earth
    public static final long MIN_IMAGE_PLAY_DURATION = 2; // sec
    public static final long LOW_FREE_SPACE_ON_DEVICE = 3000; // MBytes
    public static final long CRITICAL_FREE_SPACE_ON_DEVICE = 500; // MBytes
    public static final long SYNC_FILES_SCHEDULE_PERIOD = 15 * 60 * 1000; // msec period between two successive sync contacts
    public static final long SYNC_FILES_SCHEDULE_DELAY = 1 * 40 * 1000; // msec initial delay
    public static final long REGULAR_RESTART_APP_PERIOD = 180 * 60 * 1000; // msec
    public static final long GET_GOOGLE_LOCATION_PERIOD = 15 * 60 * 1000; // msec
    public static final long MAKE_SCREENSHOTS_SCHEDULE_PERIOD = 15 * 60 * 1000; // msec
    public static final long DEBOUNCED_TOUCH_THRESHOLD = 200; // msec
    public static final Integer BITMAP_QUALITY_PERCENT = 60; // percents

    public static final String EDISON_MONITOR_PACKAGE = "com.taxiinteractive.monitor";
    public static final String EDISON_UPDATER_PACKAGE = "com.taxiinteractive.updater";
    public static final String INRIDEADS_APP_PACKAGE  = "com.edisoninteractive.inrideads";
    public static final String LAUNCHER_APP_PACKAGE   = "com.taxiinteractive.launcher";

    public static boolean DEBOUNCED_TOUCH_EVENTS_ENABLED = true;

    public static final long SHOW_STD_CHANNEL_CUT_OFF_INTERVAL = 1700;

    public static final String AD_MOB_KEY = "ca-app-pub-2756860740531206~5805206669";//"ca-app-pub-3940256099942544~3347511713";//"ca-app-pub-8325909879166680~9269466078";
    public static final String AD_MOB_LIVE_INTERSTITIAL_AD_ID = "ca-app-pub-2756860740531206/2467337705";//"ca-app-pub-3940256099942544/8691691433";//"ca-app-pub-3940256099942544/1033173712";//"ca-app-pub-8325909879166680/2129342657"; //
    public static final String AD_MOB_TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/8691691433";//"ca-app-pub-3940256099942544/8691691433";//"ca-app-pub-3940256099942544/1033173712";//"ca-app-pub-8325909879166680/2129342657"; //

    public static final int INTERSTITIAL_AD_PERIOD_BASE = 5 * 60 * 1000; // msec
    public static final int INTERSTITIAL_AD_PERIOD_DEVIATION = 1 * 60 * 1000; // msec
    public static final long INTERSTITIAL_AD_DURATION = 60 * 1000; // msec

    public static final String[] PERMISSION_CONSTANTS =
            { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA};

    public static final String ACTION_MAKE_SYNC_CONTACT = "com.edisoninteractive.inrideads.ACTION_MAKE_SYNC_CONTACT";
    public static final String ACTION_START_REGULAR_SYNC_CONTACTS = "com.edisoninteractive.inrideads.ACTION_START_REGULAR_SYNC_CONTACTS";
    public static final String ACTION_STOP_REGULAR_SYNC_CONTACTS = "com.edisoninteractive.inrideads.ACTION_STOP_REGULAR_SYNC_CONTACTS";
}


