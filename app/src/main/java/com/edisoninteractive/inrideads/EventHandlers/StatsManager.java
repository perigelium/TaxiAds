package com.edisoninteractive.inrideads.EventHandlers;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.edisoninteractive.inrideads.Database.DataStats;
import com.edisoninteractive.inrideads.Database.DatabaseManager;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

/**
 * Created by Creator on 12/18/2017.
 */

public class StatsManager {

    public static final String AD_ROTATOR = "1";
    public static final String INTERACTIVE = "2";
    public static final String BUTTON = "3";
    public static final String TOGGLE_BUTTON = "4";
    public static final String TAB = "5";
    public static final String RSS_THUMB = "6";
    public static final String LISTING_THUMB = "7";
    public static final String AD_ROTATOR_PRESS = "8";
    public static final String FORM_INPUT = "9";
    public static final String CHANNEL = "10";
    public static final String TAB_ELEMENT = "11";
    public static final String INTERACTIVE_ELEMENT = "12";
    public static final String FACE_DETECTED = "13";
    public static final String UPTIME_TICK = "14";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat TIME_FULL = new SimpleDateFormat("HH:mm:ss:SSS");

    private static StatsManager ourInstance;
    private DatabaseManager dbManager;
    private Context context;

    private static HashMap<String, Long> statsFreqDict = new HashMap<>();

    private StatsManager(Context context) {
        this.context = context;
        dbManager = DatabaseManager.get(context);
        initializeStatsFreq();
    }

    private void initializeStatsFreq() {
        for (int i = 1; i < 15; i++) {
            statsFreqDict.put(String.valueOf(i), 0L);
        }
    }

    //******************************* public static methods **************************************//
    public static StatsManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new StatsManager(context);
        }
        return ourInstance;
    }

    //****************************** public instance methods **************************************//
    public void writeStats(String statsType, @Nullable String statsId, @Nullable String campaignId, @Nullable String details) {

        //Log.d(GlobalConstants.APP_LOG_TAG, "Services/StatsManager: writeStats() called");

        long lastStatTime;
        Date currentDate = new Date();

        if (null != statsFreqDict.get(statsType)) {
            lastStatTime = statsFreqDict.get(statsType);
        } else {
            lastStatTime = System.currentTimeMillis();
        }

        long currentTime = System.currentTimeMillis();
        statsFreqDict.put(statsType, currentTime);

        if (TextUtils.equals(statsType, CHANNEL) && (currentTime - lastStatTime) < 3000) {
            return;

        } else {
            String lat = "null";
            String lon = "null";

            Location location = LocationTrackPoint.mlastLocation;
            if (location != null && location.getLatitude()!=0 && location.getLongitude()!=0) {
                lat = String.valueOf(location.getLatitude());
                lon = String.valueOf(location.getLongitude());
            }

            String unitId;
            try {
                unitId = UNIT_ID;
            } catch (Exception ex) {
                return;
            }
            String date = DATE_FORMAT.format(currentDate);
            String time = TIME_FORMAT.format(currentDate);
            long time_stamp = System.currentTimeMillis();
            String timeStamp = String.valueOf(time_stamp);
            String timeFull = TIME_FULL.format(currentDate);

            if (null == statsId || statsId.isEmpty()) {
                statsId = "null";
            }

            if (null == campaignId || campaignId.isEmpty()) {
                campaignId = "null";
            }

            if (null == details || details.isEmpty()) {
                details = "null";
            }

            DataStats stats = new DataStats(statsType, statsId, 1, date, 0, unitId, campaignId,
                    details, time, lat, lon, timeStamp, timeFull);

            try {
                Log.d(GlobalConstants.APP_LOG_TAG, "Services/StatsManager: writeStats() trying to save to DB");
                dbManager.writeStats(stats);
            } catch (Exception ex) {
                Log.w(GlobalConstants.APP_LOG_TAG, "Services/StatsManager: writeStats() EXCEPTION: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // statsId can be empty string or null
        // campaign id can be empty string or null
        // details can be null

        // What data you have to inject into DB
        // statsType -> statsType
        // statsId -> stats_id
        // campaignId -> campaign_id
        // details -> details

        // time in format of HH:MM:SS -> time
        // date in format of YYYY:MM:DD -> date
        // unit id -> unit_id
        // 1 -> count
        // 0 -> sent
        // unit location latitude -> lat
        // unit location longitude -> lon
        // unix timestamp -> timestamp
        // time in format of HH:MM:SS;MILLIS -> time_full

    }
}
