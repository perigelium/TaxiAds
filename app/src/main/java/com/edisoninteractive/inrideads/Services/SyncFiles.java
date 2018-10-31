package com.edisoninteractive.inrideads.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.ConfigData;
import com.edisoninteractive.inrideads.Entities.ConfigDataArguments;
import com.edisoninteractive.inrideads.Entities.Counters;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;
import com.edisoninteractive.inrideads.Entities.ObjCallAttrs;
import com.edisoninteractive.inrideads.Entities.ObjectDataArguments;
import com.edisoninteractive.inrideads.Entities.REST_ConfigSubItem;
import com.edisoninteractive.inrideads.Interfaces.BoundServiceListener;
import com.edisoninteractive.inrideads.Interfaces.RetrofitAPI;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.MyTextUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.edisoninteractive.inrideads.Entities.DownloadResultStatuses.STATUS_DOWNLOADED_TO_ROOT;
import static com.edisoninteractive.inrideads.Entities.DownloadResultStatuses.STATUS_DOWNLOAD_ITEM_FAILED;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ACTION_MAKE_SYNC_CONTACT;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_GET_CONFIG_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_GET_FILES_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_SEND_EVENTS_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIGS_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_ROOT_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.LOW_FREE_SPACE_ON_DEVICE;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.PARAMS_JS_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.STORAGE_DIR;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

/**
 * Created by Alex Angan one fine day
 */

public class SyncFiles extends IntentService implements Callback, retrofit2.Callback<ResponseBody>
{
    private String className = getClass().getSimpleName();

    private ObjectDataArguments objectDataArguments;
    private List<Call> callDownloadObjectsList;
    private List<ObjCallAttrs> objectCallAttrs;

    private ConfigData configData;
    private boolean oneItemDownloadFailed;

    private Retrofit retrofit;
    private RetrofitAPI api;
    private Gson gson;
    public Counters counters;
    private String l_eventIdsRoot;

    private Timer requestConfigDataTimer;
    private Timer getFilesTimer;
    private boolean restart_app;
    private SyncFilesBinder syncFilesBinder;

    private ArrayList<String> al_ExistingFiletypesPaths;
    private OkHttpClient.Builder defaultHttpClient;
    private BoundServiceListener mListener;
    private Boolean lowDiscMemory;

    public SyncFiles()
    {
        super("SyncFiles");
    }

    @Override
    public void onCreate()
    {
        counters = new Counters();
        l_eventIdsRoot = "";
        gson = new Gson();
        restart_app = false;
        syncFilesBinder = new SyncFilesBinder();
        al_ExistingFiletypesPaths = new ArrayList<>();
        defaultHttpClient = new OkHttpClient.Builder();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(APP_LOG_TAG, className + ": onStartCommand");

        String action = intent != null ? intent.getAction() : null;

        if (action != null && action.equals(ACTION_MAKE_SYNC_CONTACT))
        {
            Log.i(APP_LOG_TAG, className + ": ACTION_MAKE_SYNC_CONTACT");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.i(APP_LOG_TAG, className + ": onHandleIntent");

        String action = intent != null ? intent.getAction() : null;
        Boolean forced = intent != null && intent.getBooleanExtra("forced", false);

        Log.i(APP_LOG_TAG, className + ":  forced = " + String.valueOf(forced));

        if (action != null && action.equals(ACTION_MAKE_SYNC_CONTACT))
        {
            startUpdate(forced);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.i(APP_LOG_TAG, className + ": onDestroy");

        mListener = null;
        resetVariables();
        counters.reset();

        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        Log.i(APP_LOG_TAG, className + ": onLowMemory");

        super.onLowMemory();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(APP_LOG_TAG, className + " onBind");

        resetVariables();
        counters.reset();

        return syncFilesBinder;
    }

    @Override
    public void onRebind(Intent intent)
    {
        super.onRebind(intent);

        Log.i(APP_LOG_TAG, className + " onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(APP_LOG_TAG, className + " onUnbind");

        mListener = null;
        resetVariables();
        counters.reset();

        return super.onUnbind(intent);
    }

    public class SyncFilesBinder extends Binder
    {
        public SyncFiles getService()
        {
            return SyncFiles.this;
        }

        public void setListener(BoundServiceListener listener)
        {
            mListener = listener;
        }
    }

    private void startUpdate(Boolean forced)
    {
        Log.i(APP_LOG_TAG, className + ": startUpdate");

        int mBytesInExternalStorageAvailable = (int) SystemUtils.getMBytesInExternalStorageAvailable();
        lowDiscMemory = mBytesInExternalStorageAvailable < LOW_FREE_SPACE_ON_DEVICE;

        if (lowDiscMemory)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ":  Low disk memory, starting sync with forced mode");

            forced = true;
        }

        String strTimeStampFilePath = DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/lastSyncTimestamp.evt";

        final File fileLastSyncTimestamp = new File(strTimeStampFilePath);

        if (!fileLastSyncTimestamp.exists())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": lastSyncTimestamp does not exist, starting sync with forced mode");
            forced = true;
        }

        if (forced)
        {
            counters.setmBytesInStorageAvailable(mBytesInExternalStorageAvailable);
            counters.setUnitId(UNIT_ID);
        }

        requestConfigData(forced);
    }

    public void resetVariables()
    {
        oneItemDownloadFailed = false;
    }

    private void getConfigDataFromServer(Boolean forced)
    {
        String wifi_address = SystemUtils.getWifiMAC(this);
        String strForcedUpdate = forced ? "1" : "0";
        String appVersionString = SystemUtils.getAppVersionString(this);
        String deviceUUID = SystemUtils.getDeviceUUID(this);
        Random random = new Random();
        String strRandom = String.valueOf(random.nextInt(Integer.MAX_VALUE));

        String strLatitude = "";
        String strLongitude = "";
        String strAccuracy = "";
        String strProviderName = "";

        if (LocationTrackPoint.mlastLocation != null)
        {
            double latitude = LocationTrackPoint.mlastLocation.getLatitude();
            double longitude = LocationTrackPoint.mlastLocation.getLongitude();
            strLatitude = String.valueOf(latitude);
            strLongitude = String.valueOf(longitude);
            strAccuracy = String.valueOf(LocationTrackPoint.mlastLocation.getAccuracy());
            strProviderName = String.valueOf(LocationTrackPoint.mlastLocation.getProvider());
        }

        ConfigDataArguments configDataArguments = new ConfigDataArguments(strForcedUpdate, UNIT_ID, deviceUUID, strRandom, wifi_address, appVersionString, strLatitude, strLongitude, strAccuracy, strProviderName);

        counters.setStateRequestingContent(true);

        Log.i(APP_LOG_TAG, className + ": getConfigDataFromServer started");

        getGlobalConfig(configDataArguments, forced);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e)
    {
        for (int i = 0; i < objectCallAttrs.size(); i++)
        {
            ObjCallAttrs objCallAttrs = objectCallAttrs.get(i);

            if (call == objCallAttrs.getCall())
            {
                oneItemDownloadFailed = true;

                counters.incObjectsFailed();
                counters.decObjectsToDownload();
                counters.activeCalls--;

                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Download failure " + objCallAttrs.getFileName() + " , reason: " + e.getMessage());
                sendItemEvent(objCallAttrs.getEventId(), STATUS_DOWNLOAD_ITEM_FAILED);
                break;
            }
        }

        checkIfAllCallsProcessedAndApplyUpdates();
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
    {
        if (!response.isSuccessful())
        {
            counters.incObjectsFailed();
            counters.decObjectsToDownload();
            counters.activeCalls--;
            return;
        }

        for (int i = 0; i < objectCallAttrs.size(); i++)
        {
            ObjCallAttrs objCallAttrs = objectCallAttrs.get(i);

            if (call == objCallAttrs.getCall())
            {
                counters.incObjectsReceived();
                counters.decObjectsToDownload();
                counters.activeCalls--;

                if (saveResponseBodyToFile(response, objCallAttrs))
                {
                    sendItemEvent(objCallAttrs.getEventId(), STATUS_DOWNLOADED_TO_ROOT);
                } else
                {
                    sendItemEvent(objCallAttrs.getEventId(), STATUS_DOWNLOAD_ITEM_FAILED);
                }

                break;
            }
        }

        checkIfAllCallsProcessedAndApplyUpdates();
    }

    private void checkIfAllCallsProcessedAndApplyUpdates()
    {
        // all calls processed successfully
        if (counters.totalObjectCount != 0 && counters.objectsToDownload == 0)
        {
            try
            {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            applyUpdatesAndNotifyServer();
        }
    }

    private void applyUpdatesAndNotifyServer()
    {
        Log.i(APP_LOG_TAG, className + ": applyUpdatesAndNotifyServer started");
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        flushEvents();

        if (!oneItemDownloadFailed)
        {
            long lastSuccessfulSyncTimestamp = System.currentTimeMillis();
            String jsonStrSyncTimeStamp = "{\"lastSyncTimestamp\":\"" + lastSuccessfulSyncTimestamp + "\"}";

            if (!FileUtils.writeStringToFile(DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/" + "lastSyncTimestamp.evt", jsonStrSyncTimeStamp))
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Unable to save sync timestamp into file lastSyncTimestamp.evt");
            }

            sendItemEvent(mSettings.getString("restart_app_event_id", ""), STATUS_DOWNLOADED_TO_ROOT);

            mSettings.edit().putInt("lastSyncFilesResult", 1).apply();
        }

        if (oneItemDownloadFailed)
        {
            sendItemEvent(mSettings.getString("apply_updates_event_id", ""), STATUS_DOWNLOAD_ITEM_FAILED);
            sendItemEvent(mSettings.getString("restart_app_event_id", ""), STATUS_DOWNLOAD_ITEM_FAILED);

            mSettings.edit().putInt("lastSyncFilesResult", -1).apply();
        }

        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": al_ExistingFiletypesPaths.size: " + al_ExistingFiletypesPaths.size());

        getFilesTimer.cancel();

        mSettings.edit().putInt("lastSyncFilesResult", 1).apply();

        onSyncProcessCompleted(!oneItemDownloadFailed);
    }

    private void onSyncProcessCompleted(Boolean successfully)
    {
        Log.i(APP_LOG_TAG, className + ": Sync contact completed");
        resetVariables();
        counters.reset();

        counters.setStateRequestingContent(false);
        counters.setStateDownloadingContent(successfully);
        counters.setSyncRequestSucceeded(successfully);

        restart_app = false;

        if (successfully)
        {
            Log.i(APP_LOG_TAG, className + ": Restart activity called ");
            try
            {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            SystemUtils.restartActivity(getApplicationContext(), true);
        } else
        {
            if (mListener != null)
            {
                mListener.finishedDownloading();
            }
        }
    }

    private void flushEvents()
    {
        if (l_eventIdsRoot.length() != 0)
        {
            l_eventIdsRoot = l_eventIdsRoot.substring(0, l_eventIdsRoot.length() - 1);
            sendItemEvents(l_eventIdsRoot, STATUS_DOWNLOADED_TO_ROOT);
        }
    }

    private Boolean saveResponseBodyToFile(Response response, ObjCallAttrs objCallAttrs)
    {
        Boolean result = false;

        String strFilePath = "/" + DATA_ROOT_FOLDER_NAME + objCallAttrs.getPathSuffix();

        File filePath = new File(STORAGE_DIR, strFilePath);

        if (strFilePath.length() < 60 && strFilePath.contains("{\"success\":true,\"error\":0,\"message\":\"Missing event.\"}"))
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": JSON inside instead of file content !");
            return false;
        }

        if (!filePath.exists())
        {
            filePath.mkdirs();
        }

        File file = new File(filePath, objCallAttrs.getFileName());

        try
        {
            BufferedSource bufferedSource = response.body().source();

            result = FileUtils.bufferedSourceToFileByChunks(bufferedSource, file);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        response.body().close();

        return result;
    }

    private void saveRemoteObjectsArrayIntoFolders(boolean forcedUpdate)
    {
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (getFilesTimer != null)
        {
            getFilesTimer.cancel();
        }

        getFilesTimer = new Timer();

        getFilesTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                counters.incGetFilesTime();
            }
        }, 1000, 1000);

        File downloadDirectory = new File(STORAGE_DIR, DATA_ROOT_FOLDER_NAME);

        if (!downloadDirectory.exists() && !downloadDirectory.mkdir())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Unable to create " + DATA_ROOT_FOLDER_NAME);
            return;
        }

        callDownloadObjectsList = new LinkedList<>();
        objectCallAttrs = new ArrayList<>();
        List<REST_ConfigSubItem> configItems = configData.getItems();

        objectDataArguments = new ObjectDataArguments(configData.getUnit_id(), configData.getUuid(), configData.getMac(), null, null);

        if (forcedUpdate)
        {
            for (int i = 0; i < configItems.size(); i++)
            {
                REST_ConfigSubItem rest_configSubItem = configItems.get(i);
                String remoteObjectName = rest_configSubItem.getFile_name();
                String strFilePathRoot = STORAGE_DIR + "/" + DATA_ROOT_FOLDER_NAME + rest_configSubItem.getPath() + remoteObjectName;

                al_ExistingFiletypesPaths.add(strFilePathRoot);
            }

            if (al_ExistingFiletypesPaths.size() != 0)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": al_ExistingFiletypesPaths size =  " + al_ExistingFiletypesPaths.size());

                String strMediaFolder = DATA_PATH + "/media";
                String strAdsFolder = DATA_PATH + "/ads";
                String strTabsFolder = DATA_PATH + "/tabs";
                String strRssFolder = DATA_PATH + "/rss";

                if (lowDiscMemory)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": delFilesMissedInCurrentConfig started");

                    FileUtils.delFilesMissedInCurrentConfig(strMediaFolder, al_ExistingFiletypesPaths);
                    FileUtils.delFilesMissedInCurrentConfig(strAdsFolder, al_ExistingFiletypesPaths);
                    FileUtils.delFilesMissedInCurrentConfig(strTabsFolder, al_ExistingFiletypesPaths);
                    FileUtils.delFilesMissedInCurrentConfig(strRssFolder, al_ExistingFiletypesPaths);
                }
            }
        }

        for (int i = 0; i < configItems.size(); i++)
        {
            REST_ConfigSubItem rest_configSubItem = configItems.get(i);
            String pathSuffix = rest_configSubItem.getPath();
            String action = rest_configSubItem.getAction();
            String remoteObjectName = rest_configSubItem.getFile_name();
            String command = rest_configSubItem.getCommand();
            Object oContent = rest_configSubItem.getContent();
            String eventId = rest_configSubItem.getEvent_id();

            if (action.equals("execute"))
            {
                counters.incCommandsQuant();
                counters.decObjectsToDownload();

                if (command != null)
                {
                    if (command.equals("apply_updates"))
                    {
                        mSettings.edit().putString("apply_updates_event_id", eventId).apply();

                        sendItemEvent(rest_configSubItem.getEvent_id(), STATUS_DOWNLOADED_TO_ROOT);
                    }

                    if (command.equals("restart_app"))
                    {
                        mSettings.edit().putString("restart_app_event_id", eventId).apply();
                        restart_app = true;
                    }

                    if (command.equals("reset_unit_content"))
                    {
                        sendItemEvent(rest_configSubItem.getEvent_id(), STATUS_DOWNLOADED_TO_ROOT);
                    }

                    Log.i(APP_LOG_TAG, className + " : " + command + " command received");
                }
            } else
            {
                if (pathSuffix == null || remoteObjectName == null)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " - Name of rest_configSubItem or its path suffix is null, skipping file download.");
                    continue;
                }

                String strFilePathRoot = STORAGE_DIR + "/" + DATA_ROOT_FOLDER_NAME + pathSuffix + remoteObjectName;

                if (oContent != null)
                {
                    counters.incObjectsWithContent();
                    counters.decObjectsToDownload();

                    String strContent = contentFromJsonToString(rest_configSubItem);

                    if (FileUtils.writeItemContentToFile(pathSuffix, remoteObjectName, strContent))
                    {
                        sendItemEvent(eventId, STATUS_DOWNLOADED_TO_ROOT);
                    } else
                    {
                        sendItemEvent(eventId, STATUS_DOWNLOAD_ITEM_FAILED);
                    }
                } else
                {
                    File filePath = new File(strFilePathRoot);
                    boolean pathExists = false;

                    if (!filePath.exists())
                    {
                        File filePathRoot = new File(strFilePathRoot);

                        if (filePathRoot.exists())
                        {
                            pathExists = true;
                        }
                    } else
                    {
                        pathExists = true;
                    }

                    String md5Hash = "";
                    boolean downloadRequired = false;

                    if (pathExists)
                    {
                        try
                        {
                            MyTextUtils myTextUtils = MyTextUtils.getInstance();
                            md5Hash = myTextUtils.getMD5Checksum(strFilePathRoot);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if (!md5Hash.equals(rest_configSubItem.getHash()))
                        {
                            downloadRequired = true;
                        } else // file already exists
                        {
                            counters.incObjectsAlreadyExist();
                            counters.decObjectsToDownload();

                            l_eventIdsRoot += rest_configSubItem.getEvent_id() + ",";
                        }
                    } else
                    {
                        downloadRequired = true;
                    }

                    if (downloadRequired)
                    {
                        while (callDownloadObjectsList.size() > counters.objectsReceived + counters.objectsFailed + 9)
                        {
                            try
                            {
                                TimeUnit.MILLISECONDS.sleep(1000);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        downloadFileItem(rest_configSubItem);
                    }
                }
            }
        }

        checkIfAllCallsProcessedAndApplyUpdates();
    }

    private void downloadFileItem(REST_ConfigSubItem rest_configSubItem)
    {
        NetworkUtils networkUtils = NetworkUtils.getInstance();

        String strFilePath = STORAGE_DIR + "/" + DATA_ROOT_FOLDER_NAME + rest_configSubItem.getPath() + rest_configSubItem.getFile_name();

        objectDataArguments.setFile(rest_configSubItem.getFile_name());
        objectDataArguments.setEvent_id(rest_configSubItem.getEvent_id());

        if (!FileUtils.isFileOfType(strFilePath, ".flv"))
        {
            if (callDownloadObjectsList.add(networkUtils.downloadObjectData(this, API_GET_FILES_URL, objectDataArguments)))
            {
                objectCallAttrs.add(new ObjCallAttrs(callDownloadObjectsList.get(callDownloadObjectsList.size() - 1), rest_configSubItem.getPath(), rest_configSubItem.getFile_name(), rest_configSubItem.getHash(), rest_configSubItem.getEvent_id(), rest_configSubItem.getAction().equals("save_temp") || rest_configSubItem.getAction().equals("download_temp")));

                counters.incCallsSent();
                counters.activeCalls++;
            }
        } else // .flv file
        {
            counters.incObjectsSkipped();
            counters.decObjectsToDownload();

            sendItemEvent(rest_configSubItem.getEvent_id(), STATUS_DOWNLOADED_TO_ROOT);
        }
    }

    private String contentFromJsonToString(REST_ConfigSubItem rest_configSubItem)
    {
        String strContent = "";
        String str_rest_configSubItem_json = gson.toJson(rest_configSubItem);

        JsonObject jsonObj = gson.fromJson(str_rest_configSubItem_json, JsonElement.class).getAsJsonObject();

        JsonElement elem = jsonObj.get("content");

        if (elem.isJsonObject())
        {
            strContent = elem.toString();
        } else
        {
            strContent = rest_configSubItem.getContent().toString();
        }
        return strContent;
    }

    private void sendItemEvent(String eventId, String eventStatus)
    {
        if (retrofit == null)
        {
               OkHttpClient okHttpClient =  defaultHttpClient
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(1, TimeUnit.MINUTES)
                        .writeTimeout(1, TimeUnit.MINUTES).build();

            retrofit = new Retrofit.Builder().baseUrl(API_SEND_EVENTS_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

            api = retrofit.create(RetrofitAPI.class);
        }

        if (api != null)
        {
            retrofit2.Call<ResponseBody> uploadFileCall = api.uploadEvent(eventId, eventStatus, configData.getUnit_id(), configData.getUuid(), configData.getMac());

            uploadFileCall.enqueue(SyncFiles.this);
        }
    }

    private void sendItemEvents(String eventId, String eventStatus)
    {
        if (retrofit == null)
        {
            OkHttpClient okHttpClient =  defaultHttpClient
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES).build();

            retrofit = new Retrofit.Builder().baseUrl(API_SEND_EVENTS_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

            api = retrofit.create(RetrofitAPI.class);
        }

        if (api != null)
        {
            retrofit2.Call<ResponseBody> uploadFileCall = api.uploadEvents(eventId, eventStatus, configData.getUnit_id(), configData.getUuid(), configData.getMac());

            uploadFileCall.enqueue(SyncFiles.this);
        }
    }

    private void getGlobalConfig(ConfigDataArguments configDataArguments, Boolean forced)
    {
        Log.i(APP_LOG_TAG, className + ": getGlobalConfig");

        String strConfigDataArguments = new Gson().toJson(configDataArguments);
        Log.i(APP_LOG_TAG, className + ": strConfigDataArguments= " + strConfigDataArguments);

        if (retrofit == null)
        {
            OkHttpClient okHttpClient =  defaultHttpClient
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(3, TimeUnit.MINUTES)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder().baseUrl(API_GET_CONFIG_URL)
                    .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

            api = retrofit.create(RetrofitAPI.class);
        }

        if (api != null)
        {
            retrofit2.Call<ConfigData> getConfigCall = api.getGlobalConfig(configDataArguments.force, configDataArguments.unit_id,
                    configDataArguments.uuid, configDataArguments.rnd, configDataArguments.wifi_mac,
                    configDataArguments.sw_version, configDataArguments.latitude, configDataArguments.longitude,
                    configDataArguments.location_accuracy, configDataArguments.location_provider_name);

            retrofit2.Response<ConfigData> globalConfigResponse = null;

            try
            {
                globalConfigResponse = getConfigCall.execute();

                if(globalConfigResponse.isSuccessful())
                {
                    configData = globalConfigResponse.body();
                }

/*                ResponseBody responseBody = getConfigCall.execute().body();

                Log.i(APP_LOG_TAG, className + ": responseBody = " + responseBody.string());*/

            } catch (Exception e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": Error receiving global config, " + e.getMessage());
            }

            SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            if (requestConfigDataTimer != null)
            {
                requestConfigDataTimer.cancel();
            }

            if (configData == null)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Error receiving global config, Config Data is empty ");
                onSyncProcessCompleted(false);
                return;
            }

            Log.i(APP_LOG_TAG, className + ": Global config received successfully");

            String strConfigData = new Gson().toJson(configData);
            FileUtils.writeStringToFile(DATA_PATH + "/configs/globalConfig.js", strConfigData);

            List<REST_ConfigSubItem> configItems = configData.getItems();

            if (configItems == null)
            {
                Log.i(APP_LOG_TAG, className + ": Config Items is empty, " + configData.getResponse());
                onSyncProcessCompleted(false);
                return;
            }

            //if ((configData != null && configItems != null))
            if (!UNIT_ID.equals(configData.getUnit_id()))
            {
                UNIT_ID = configData.getUnit_id();
                counters.setUnitId(UNIT_ID);

                mSettings.edit().putString("unit_id", UNIT_ID).apply();

                String pathToFileInConfig = DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/" + PARAMS_JS_NAME;
                String jsonStrUnitId = "{\"unitId\":\"" + UNIT_ID + "\"}";
                FileUtils.writeStringToFile(pathToFileInConfig, jsonStrUnitId);
            }

            if (configItems.size() != 0)
            {
                resetVariables();
                //counters.reset();
                counters.setObjectsToDownload(configItems.size());
                counters.setTotalObjectCount(configItems.size());

                Log.i(APP_LOG_TAG, className + ": saveRemoteObjectsArrayIntoFolders, " + configItems.size() + " objects to process, started...");
                counters.setStateRequestingContent(false);
                counters.setStateDownloadingContent(true);

                saveRemoteObjectsArrayIntoFolders(forced);
            } else
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Config Items is empty");
                onSyncProcessCompleted(false);
            }
        }
    }

    @Override
    public void onResponse(@NonNull retrofit2.Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response)
    {
        counters.incEventsSentSuccesfully();
    }

    @Override
    public void onFailure(@NonNull retrofit2.Call<ResponseBody> call, @NonNull Throwable t)
    {
        counters.incEventsSendFailed();
    }

    private void requestConfigData(Boolean forced)
    {
        if (requestConfigDataTimer != null)
        {
            requestConfigDataTimer.cancel();
        }
        requestConfigDataTimer = new Timer();

        requestConfigDataTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                counters.incGetConfigTime();
            }
        }, 1000, 1000);

        Log.i(APP_LOG_TAG, className + ": sync contact started with forced mode = " + String.valueOf(forced));

        getConfigDataFromServer(forced);
    }
}
