package com.edisoninteractive.inrideads.Utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.Entities.GLocation;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.ObjectDataArguments;
import com.edisoninteractive.inrideads.Entities.UpdaterCheckResponse;
import com.edisoninteractive.inrideads.Interfaces.RetrofitAPI;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_UPDATER_CHECK_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_UPLOAD_LOGS_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UUID;

/**
 * Created by Alex Angan one fine day
 */

public class NetworkUtils implements retrofit2.Callback<ResponseBody>
{
    private static volatile NetworkUtils instance;
    private final OkHttpClient.Builder defaultHttpClient;
    private static final String className = NetworkUtils.class.getSimpleName();
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private RetrofitAPI api;

    private NetworkUtils()
    {
        // The singleton HTTP client.
        defaultHttpClient = new OkHttpClient.Builder();
    }

    public static NetworkUtils getInstance()
    {
        if (instance == null)
        {
            synchronized (NetworkUtils.class)
            {
                if (instance == null)
                {
                    instance = new NetworkUtils();
                }
            }
        }
        return instance;
    }

    public static boolean isNetworkAvailable(Context context)
    {
        boolean available = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            available = activeNetworkInfo != null && activeNetworkInfo.isConnected() && isHostAvailable("google.com", 1000);

            if (!available)
            {
                Log.d(APP_LOG_TAG, className + ": isNetworkAvailable: not available");
            }
        }

        return available;
    }

    private static boolean isHostAvailable(final String hostName, int timeOut)
    {
        InetAddress inetAddress = null;
        try
        {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>()
            {
                @Override
                public InetAddress call()
                {
                    try
                    {
                        return InetAddress.getByName(hostName);
                    } catch (UnknownHostException e)
                    {
                        return null;
                    }
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (Exception e)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": isHostAvailable exception, " + e.getMessage());
        }
        return inetAddress != null && !inetAddress.toString().equals("");
    }

    public String requestUnitId(String uuid)
    {
        String unitId = null;
        Log.i(APP_LOG_TAG, className + " Api Updater check, Requesting unit id...");

        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS).writeTimeout(45, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_UPDATER_CHECK_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();

        RetrofitAPI api = retrofit.create(RetrofitAPI.class);

        if (api != null)
        {
            retrofit2.Call<UpdaterCheckResponse> getIdCall = api.getUniId("0000", uuid);

            try
            {
                Response<UpdaterCheckResponse> response = getIdCall.execute();
                UpdaterCheckResponse updaterCheckResponse = response.body();

                if (updaterCheckResponse != null)
                {
                    unitId = updaterCheckResponse.unitId;
                    Log.i(APP_LOG_TAG, className + " Api Updater check, Received unit id:" + unitId);
                } else
                {
                    Log.i(APP_LOG_TAG, className + " Api Updater check, unable to receive unit id:");
                }
            } catch (Exception e)
            {
                Log.i(APP_LOG_TAG, className + " Api Updater check, unable to receive unit id:");
                e.printStackTrace();
            }
        }

        return unitId != null ? unitId : "0000";
    }

    public Call downloadObjectData(Callback callback, String apiUrl, ObjectDataArguments objectDataArguments)
    {
        defaultHttpClient.connectTimeout(10, TimeUnit.SECONDS);
        defaultHttpClient.readTimeout(1, TimeUnit.MINUTES);
        defaultHttpClient.writeTimeout(1, TimeUnit.MINUTES);

        OkHttpClient okHttpClient = defaultHttpClient.build();

/*        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("unit_id", objectDataArguments.unit_id)
                .addFormDataPart("uuid", objectDataArguments.uuid)
                .addFormDataPart("wifi_mac", objectDataArguments.wifi_mac)
                .addFormDataPart("file", objectDataArguments.file)
                .addFormDataPart("event_id", objectDataArguments.event_id)
                .build();*/

        String getUrlSuffix = "?unit_id=" + objectDataArguments.unit_id + "&" + "uuid=" + objectDataArguments.uuid + "&" + "event_id=" + objectDataArguments.event_id;

        Request request = new Request.Builder().url(apiUrl + getUrlSuffix).get() // !
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);

        return call;
    }

    public Boolean downloadFile(String url, File file)
    {
        Boolean result = false;

        defaultHttpClient.connectTimeout(10, TimeUnit.SECONDS);
        defaultHttpClient.readTimeout(1, TimeUnit.MINUTES);
        defaultHttpClient.writeTimeout(1, TimeUnit.MINUTES);
        OkHttpClient okHttpClient = defaultHttpClient.build();

        Request request = new Request.Builder().url(url).build();

        Call callDownloadURL = okHttpClient.newCall(request);

        try
        {
            okhttp3.Response response = callDownloadURL.execute();

            if (response.isSuccessful())
            {
                BufferedSource bufferedSource = response.body().source();

                if (FileUtils.bufferedSourceToFileByChunks(bufferedSource, file))
                {
                    String fileParentPath = file.getParent();
                    String fileName = file.getName();

                    if (fileName.endsWith(".part"))
                    {
                        fileName = fileName.substring(0, fileName.lastIndexOf(".part"));

                        if (file.renameTo(new File(fileParentPath + "/" + fileName)))
                        {
                            result = true;
                        }
                    }
                    result = true;
                    Log.i(APP_LOG_TAG, className + ": downloadFile " + fileName + " - success");
                }
            }

            response.body().close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public GLocation requestGeolocation(Context context, String apiUrl, String mJSONArray_WifiAPs)
    {
        Log.i(APP_LOG_TAG, className + " , Requesting location using Google Map Location Api...");

        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        JSONObject jsonObject = prepareDataForGetCellGoogleLocation(context, mJSONArray_WifiAPs);

        // Add considerIp regardless of the sim card presence
        try
        {
            jsonObject.put("considerIp", "false");
        } catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        if (jsonObject.length() == 0)
        {
            return null;
        }

        GLocation gLocation = getGoogleLocation(apiUrl, jsonObject);

        return gLocation;
    }

    private JSONObject prepareDataForGetCellGoogleLocation(Context context, String strJSONArray_WifiAPs)
    {
        JSONObject jsonObject = new JSONObject();
        JSONArray al_CellTowers;
        String strNetType;
        TelephonyManager telMngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try
        {
            JSONArray mJSONArray_WifiAPs = new JSONArray(strJSONArray_WifiAPs);
            jsonObject.put("wifiAccessPoints", mJSONArray_WifiAPs);
        } catch (Throwable getWiFiAccInfoExc)
        {
            getWiFiAccInfoExc.printStackTrace();
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to extract and send wifi access points to google geolocation API");
        }

        try
        {
            if (telMngr != null)
            {
                // add cell data if sim card is injected and ready
                if (simCardAvailableAndReady(context))
                {
                    int networkType = telMngr.getNetworkType();
                    strNetType = getNetworkTypeString(networkType);

                    al_CellTowers = SystemUtils.getCellInfo(context);
                    jsonObject.put("radioType", strNetType);
                    jsonObject.put("cellTowers", al_CellTowers);
                }
            }
        } catch (Throwable getCellularDataExc)
        {
            getCellularDataExc.printStackTrace();
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": prepareDataForGetCellGoogleLocation: Failed to extract cell info and popuplate jsonObject");
        }

        Log.d(GlobalConstants.APP_LOG_TAG, jsonObject.toString());


        return jsonObject;
    }

    public Boolean uploadAolIdsAndUrls(Context context, String apiUrl, String idsAndUrls)
    {
        Boolean result = false;

        final SharedPreferences mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String unitId = mSettings.getString("unit_id", "0000");

        try
        {

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("unit_id", unitId).addFormDataPart("uuid", SystemUtils.getDeviceUUID(context)).addFormDataPart("items", idsAndUrls).build();

            Request request = new Request.Builder().url(apiUrl).post(requestBody).build();

            defaultHttpClient.connectTimeout(10, TimeUnit.SECONDS);
            defaultHttpClient.readTimeout(1, TimeUnit.MINUTES);
            defaultHttpClient.writeTimeout(1, TimeUnit.MINUTES);

            OkHttpClient okHttpClient = defaultHttpClient.build();

            Call call = okHttpClient.newCall(request);
            okhttp3.Response response = call.execute(); // used by a service

            if (response.isSuccessful())
            {
                result = true;
                response.body().close();

                Log.i(APP_LOG_TAG, className + ": uploadAolIdsAndUrls - response is successful()");
            } else
            {
                Log.i(APP_LOG_TAG, className + ": uploadAolIdsAndUrls -  response is not successful()");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            Log.i(APP_LOG_TAG, className + ": uploadAolIdsAndUrls -  response is not successful, " + e.getMessage());
        }
        return result;
    }

    private GLocation getGoogleLocation(String apiUrl, JSONObject jsonObject)
    {
        GLocation gLocation = null;

        // call google geolocation api
        try
        {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(jsonObject));
            Request request = new Request.Builder().url(apiUrl).post(requestBody).build();

            defaultHttpClient.connectTimeout(1, TimeUnit.MINUTES);
            defaultHttpClient.readTimeout(1, TimeUnit.MINUTES);
            defaultHttpClient.writeTimeout(1, TimeUnit.MINUTES);

            OkHttpClient okHttpClient = defaultHttpClient.build();

            Call call = okHttpClient.newCall(request);
            okhttp3.Response response = call.execute(); // used by a service

            if (response.isSuccessful())
            {
                Gson gson = new Gson();
                String strJsonObject = response.body().string();
                response.body().close();

                gLocation = gson.fromJson(String.valueOf(strJsonObject), GLocation.class);

                Log.i(APP_LOG_TAG, className + " Google Location Api check, Location received");
            } else
            {
                Log.i(APP_LOG_TAG, className + " Google Location Api check, unsuccessful response received");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": NetworkUtils.requestGeolocation(): failed to call google geolocation API");
        }
        return gLocation;
    }

    public boolean simCardAvailableAndReady(Context context)
    {
        try
        {
            TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();
            return simState == TelephonyManager.SIM_STATE_READY;
        } catch (Exception exc)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, " NetworkUtils.simCardAvailableAndReady() threw an exception");
            exc.printStackTrace();
        }

        return false;
    }

    private String getNetworkTypeString(int networkType)
    {
        String strNetType = "Unknown";

        switch (networkType)
        {
            case 7:
                strNetType = "1xRTT";
                break;
            case 4:
                strNetType = "cdma";
                break;
            case 2:
                strNetType = "EDGE";
                break;
            case 14:
                strNetType = "eHRPD";
                break;
            case 5:
                strNetType = "EVDO rev. 0";
                break;
            case 6:
                strNetType = "EVDO rev. A";
                break;
            case 12:
                strNetType = "EVDO rev. B";
                break;
            case 1:
                strNetType = "GPRS";
                break;
            case 8:
                strNetType = "HSDPA";
                break;
            case 10:
                strNetType = "WCDMA"; // HSPA
                break;
            case 15:
                strNetType = "HSPA+";
                break;
            case 9:
                strNetType = "HSUPA";
                break;
            case 11:
                strNetType = "iDen";
                break;
            case 13:
                strNetType = "LTE";
                break;
            case 3:
                strNetType = "UMTS";
                break;
            case 0:
                strNetType = "Unknown";
                break;
        }
        return strNetType;
    }

    public void showAndUploadLogEvent(String className, Integer priority, String message)
    {
        switch (priority)
        {
            case 1:
            {
                Log.w(APP_LOG_TAG, className + ": " + message);
                break;
            }

            case 2:
            {
                Log.e(APP_LOG_TAG, className + ": " + message);
                break;
            }

            case 3:
            {
                Log.wtf(APP_LOG_TAG, className + ": " + message);
                break;
            }

            default:
            {
                Log.i(APP_LOG_TAG, className + ": " + message);
            }
        }

        if (okHttpClient == null)
        {
            okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES).build();
        }

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder().baseUrl(API_UPLOAD_LOGS_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        }

        if (api == null)
        {
            api = retrofit.create(RetrofitAPI.class);
        }

        if (okHttpClient != null && retrofit != null && api != null)
        {
            TimeZone timeZone = TimeZone.getDefault();

            Calendar c = Calendar.getInstance(timeZone);

            String strLocalTime = String.format(Locale.ENGLISH, "%04d", c.get(Calendar.YEAR)) + "." + String.format(Locale.ENGLISH, "%02d", c.get(Calendar.MONTH) + 1) + "." + String.format(Locale.ENGLISH, "%02d", c.get(Calendar.DAY_OF_MONTH)) + " " + String.format(Locale.ENGLISH, "%02d", c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format(Locale.ENGLISH, "%02d", c.get(Calendar.MINUTE)) + ":" + String.format(Locale.ENGLISH, "%02d", c.get(Calendar.SECOND)) + ":" + String.format(Locale.ENGLISH, "%03d", c.get(Calendar.MILLISECOND));

/*            NotificationAlert notificationAlert = new NotificationAlert(UNIT_ID, UUID, System.currentTimeMillis(),
                    strLocalTime, priority, message, null);

            String strNotif = new Gson().toJson(notificationAlert);
            Log.i(APP_LOG_TAG, className + "strNotif = " + strNotif );*/

            retrofit2.Call<ResponseBody> uploadLogEventCall = api.uploadLogEvent
                    (UNIT_ID, UUID, String.valueOf(System.currentTimeMillis()), strLocalTime, String.valueOf(priority), message);

            uploadLogEventCall.enqueue(this);
        }
    }

    @Override
    public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response)
    {
        try
        {
            if(response.isSuccessful() && response.body() != null)
            {
                String strResponse = response.body().string();

                if (strResponse != null && !strResponse.contains("\"success\":true"))
                {
                    Log.w(APP_LOG_TAG, className + ": uploadLogEventCall - Response not successful, " + strResponse);
                }
            }
            else
            {
                Log.w(APP_LOG_TAG, className + ": uploadLogEventCall - Response not successful");
            }
        } catch (Exception e)
        {
            Log.w(APP_LOG_TAG, className + ": uploadLogEventCall - onResponse exception, " + e.getMessage());
        }
    }

    @Override
    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t)
    {
        Log.w(APP_LOG_TAG, className + ": showAndUploadLogEvent failed, " + t.getMessage());
    }
}
