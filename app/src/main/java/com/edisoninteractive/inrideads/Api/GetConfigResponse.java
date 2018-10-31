package com.edisoninteractive.inrideads.Api;

import android.util.Log;

import com.edisoninteractive.inrideads.Entities.ConfigData;
import com.edisoninteractive.inrideads.Entities.REST_ConfigSubItem;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIGS_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

public class GetConfigResponse extends ApiResponse
{
    private ConfigData configData;
    private List<REST_ConfigSubItem> configItems;
    private final String className = getClass().getSimpleName();

    @Override
    public boolean parseResponse(String responseString)// throws JSONException
    {
        Gson gson = new Gson();

        JSONObject jsonObject = null;

        try
        {
            jsonObject = new JSONObject(responseString);
        } catch (Exception e)
        {
            e.printStackTrace();

            Log.i(APP_LOG_TAG, className + " - parseResponse to jsonObject failed, " + e.getMessage());

            return false;
        }

        if (jsonObject.has("success"))
        {
            try
            {
                final String strSuccess = jsonObject.getString("success");

                if (strSuccess.equals("true"))
                {
                    Log.i(APP_LOG_TAG, className + " - parseResponse - jsonString success: true");

                    FileUtils.writeStringToFile(DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/globalConfig.js", responseString);

                    if (jsonObject.length() != 0)
                    {
                        if (!fillConfigItemsList(gson, jsonObject))
                        {
                            return false;
                        }
                    }
                } else
                {
                    Log.i(APP_LOG_TAG, className + " - parseResponse - jsonString success: false");

                    return false;
                }

            } catch (Exception e)
            {
                Log.i(APP_LOG_TAG, className + " - parseResponse failed, " + e.getMessage());
                e.printStackTrace();

                return false;
            }
        } else
        {
            Log.i(APP_LOG_TAG, className + " - parseResponse - jsonString success: not found");
            return false;
        }

        return true;
    }

    private boolean fillConfigItemsList(Gson gson, JSONObject configDataJSONobject)
    {
        boolean result = false;

        configData = gson.fromJson(String.valueOf(configDataJSONobject), ConfigData.class);

        JSONArray subItemJSONarray = null;

        if (configDataJSONobject.has("items"))
        {
            try
            {
                subItemJSONarray = configDataJSONobject.getJSONArray("items");

                Type typeConfigSubItem = new TypeToken<List<REST_ConfigSubItem>>()
                {
                }.getType();

                configItems = gson.fromJson(String.valueOf(subItemJSONarray), typeConfigSubItem);

                configData.setItems(null);

                if (configItems != null)
                {
                    result = true;
                }

            } catch (JSONException e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " - parseResponse - fillConfigItemsList JSONException");
                e.printStackTrace();
            }
        } else
        {
            Log.i(APP_LOG_TAG, className + " - fillConfigItemsList, items not found");
        }

        return result;
    }

    public List<REST_ConfigSubItem> getConfigItems()
    {
        return configItems;
    }

    public ConfigData getConfigData()
    {
        return configData;
    }
}
