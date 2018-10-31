package com.edisoninteractive.inrideads.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.edisoninteractive.inrideads.Database.DataStats;
import com.edisoninteractive.inrideads.Database.DatabaseManager;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

/**
 * Created by mdumik on 19.12.2017.
 */

public class ServerStatsSendService extends Service {

    private static final String className = ServerStatsSendService.class.getSimpleName();
    private static boolean isRunning;
    private static OkHttpClient okHttpClient;
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!isRunning) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isRunning = true;
                    if (NetworkUtils.isNetworkAvailable(ServerStatsSendService.this)) {
                        sendStats();
                    }
                }
            }, 10000, 1000 * 60 * 2);
        }

        return START_STICKY;
    }

    private void sendStats() {

        List<DataStats> unsentStats = null;
        try
        {
            unsentStats = DatabaseManager.get(this).getUnsentStats();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (unsentStats == null || unsentStats.size() == 0) {
            return;
        }

        changeSentStatus(unsentStats, 2);


        String uuid = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String wifiMac = SystemUtils.getWifiMAC(getApplicationContext());

        if (unsentStats.size() < 200) {
            send(unsentStats, UNIT_ID, uuid, wifiMac);
        } else {
            List<List<DataStats>> groupedStatsList = sortBy200(unsentStats);
            for (List<DataStats> groupedList : groupedStatsList) {
                send(groupedList, UNIT_ID, uuid, wifiMac);
            }
        }
    }

    private void changeSentStatus(List<DataStats> stats, int status) {

        for (DataStats stat : stats) {
            if (null != stat) {
                stat.setSent(status);
                DatabaseManager.get(this).updateStats(stat);
            }
        }
    }

    private void deleteSentStats() {
        DatabaseManager.get(this).deleteSentStats();
    }

    private String getStatsJSONArray(List<DataStats> stats) {

        try {
            JSONArray jsonArray = new JSONArray();

            for (DataStats stat : stats) {

                if (stat == null) {
                    continue;
                }

                JSONObject jsonObject = new JSONObject();

                // handle details as JSONObject and not a string
                boolean curlyBracesFound = false;
                JSONObject details = null;
                if(stat.getDetails()!=null && !stat.getDetails().equals("null")){
                    curlyBracesFound = stat.getDetails().contains("{");
                    if(curlyBracesFound){
                        details = new JSONObject(stat.getDetails());
                    }
                }

                jsonObject.put("details", details);
                jsonObject.put("time", stat.getTime());
                jsonObject.put("lon", stat.getLon());
                jsonObject.put("id", stat.getId());
                jsonObject.put("stats_id", stat.getStatsId());
                jsonObject.put("stats_type", stat.getStatsType());
                jsonObject.put("count", stat.getCount());
                jsonObject.put("unit_id", stat.getUnitId());
                jsonObject.put("date", stat.getDate());
                jsonObject.put("time_full", stat.getTimeFull());
                jsonObject.put("sent", stat.getSent());
                jsonObject.put("timestamp", stat.getTimeStamp());
                jsonObject.put("campaign_id", stat.getCampaignId());
                jsonObject.put("lat", stat.getLat());

                jsonArray.put(jsonObject);
            }

            String finalVal = jsonArray.toString();

            return jsonArray.toString();

        } catch (JSONException ex) {
            return null;
        }
    }

    private void send(List<DataStats> statsList, String unitId, String uuid, String wifiMac) {

        String stats = getStatsJSONArray(statsList);
        Log.d(GlobalConstants.APP_LOG_TAG, className + ": ServerStatsSendService: send() trying to send stats to server");

        if (stats == null || TextUtils.isEmpty(unitId) || wifiMac == null) {
            return;
        }

        if(okHttpClient == null)
        {
            okHttpClient = new OkHttpClient();
        }

        long rnd = (long) Math.floor(Long.MAX_VALUE * Math.random());

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("unit_id", unitId)
                .add("stats", stats)
                .add("rnd", String.valueOf(rnd))
                .add("uuid", uuid)
                .add("wifi_mac", wifiMac);

        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(GlobalConstants.API_UPLOAD_STATS_URL)
                .post(formBody)
                .build();

        OkHttpClient eagerClient = okHttpClient.newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                // increasing writeTimeout value can solve SSL connection timeout exception
                // if not, the SSL exception can be caused by sending data greater than 1Mib via OkHttp
                .build();

        try {
            Response response = eagerClient.newCall(request).execute(); // NB !

            if (!response.isSuccessful()) {
                Log.d(GlobalConstants.APP_LOG_TAG, className + ": ServerStatsSendService: OkHttp: Failure sending stats " +
                        response.message());
                changeSentStatus(statsList, 0);
            } else {
                Log.d(GlobalConstants.APP_LOG_TAG, className + ": ServerStatsSendService: OkHttp: Stats Successful sent!" +
                        response.message());
                deleteSentStats();
            }

            response.body().close();

        } catch (IOException e) {
            Log.d(GlobalConstants.APP_LOG_TAG, className + ": ServerStatsSendService: IOException sending stats = "
                    + e.getMessage());
            changeSentStatus(statsList, 0);
            e.printStackTrace();
        }
    }

    private List<List<DataStats>> sortBy200(List<DataStats> stats) {

        List<List<DataStats>> groupedList = new ArrayList<>();

        int x = 200;  // chunk size
        int len = stats.size();

        for (int i = 0; i < len; i += x) {

            if (i + x < len) {
                List<DataStats> tempList = stats.subList(i, (i + x) - 1);
                groupedList.add(tempList);
            } else {
                List<DataStats> lastList = stats.subList(i, len - 1);
                groupedList.add(lastList);
                break;
            }
        }

        return groupedList;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        restartService();
    }

    private void restartService() {
        Intent restartIntent = new Intent(this, getClass());
        PendingIntent pi = PendingIntent.getService(this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        long time = System.currentTimeMillis() + (1000 * 10); // 10 seconds from now
        try {
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, time, pi);
            }
        } catch (NullPointerException ex) {
        }
    }
}
