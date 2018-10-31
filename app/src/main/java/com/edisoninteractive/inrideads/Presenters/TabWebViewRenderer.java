package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.Tab;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import java.net.URLDecoder;

/**
 * Created by Alex Angan on 29.01.2018.
 */

class TabWebViewRenderer extends TabContentRenderer
{
    private Activity activity;
    private boolean isLocalResource;
    private String contentURL;
    private SharedPreferences appPreferences;
    private boolean initialized;
    private SuperWebViewFrame superWebView;


    /**
     * TabWebViewRenderer is a standard renderer for local and remote web content
     * displayed through tabbed channel.
     * @param activity
     * @param frameLayout
     * @param tab
     */
    public TabWebViewRenderer(Activity activity, FrameLayout frameLayout, Tab tab)
    {
        super(frameLayout, tab);
        this.activity = activity;
    }


    //************************* Public methods ***********************************//


    ///************************ Private methods *********************************//

    private void initialize(){

        if(initialized){
            Log.w(GlobalConstants.APP_LOG_TAG, "TabWebViewRenderer has been already initialized");
            return;
        }

        if(tab.type.equals("web_view_local")){
            isLocalResource = true;
        }

        appPreferences = activity.getSharedPreferences(GlobalConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        String pathToAssetsFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + GlobalConstants.DATA_ROOT_FOLDER_NAME;
        String unit_id = appPreferences.getString("unit_id", "0000");
        String decodedURL = null;

        Uri.Builder builder = new Uri.Builder();

        if(isLocalResource){
            try {

                String safeCampaignId = null;
                String safeStatsId = null;

                try{
                    safeCampaignId = (tab.campaignId!=null) ? String.valueOf((int)  Double.parseDouble(tab.campaignId)) : "null";
                }catch (Exception parseCampaignIdExc){
                    Log.e(GlobalConstants.APP_LOG_TAG, "TabWebViewRenderer.initialize() failed to convert campaignId to integer");
                    parseCampaignIdExc.printStackTrace();
                }

                try{
                    safeStatsId = (tab.statsId!=null) ? String.valueOf((int) Double.parseDouble(tab.statsId)) : "null";
                }catch (Exception parseStatsIdExc){
                    Log.e(GlobalConstants.APP_LOG_TAG, "TabWebViewRenderer.initialize() failed to convert statsId to integer");
                    parseStatsIdExc.printStackTrace();
                }

                builder.appendPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + GlobalConstants.DATA_ROOT_FOLDER_NAME + tab.contentURL)
                        .appendQueryParameter("pathToAssetsFolder", "file:///" + pathToAssetsFolder)
                        .appendQueryParameter("liveMode", "true")
                        .appendQueryParameter("dispatchMessages", "true")
                        .appendQueryParameter("broadcastMultipleMessages", "true")
                        .appendQueryParameter("useEdisonProtocol", "true")
                        .appendQueryParameter("unit_id", unit_id)
                        .appendQueryParameter("uuid", SystemUtils.getDeviceUUID(activity))
                        .appendQueryParameter("rnd", String.valueOf(Math.random()))
                        .appendQueryParameter("campaignId", safeCampaignId)
                        .appendQueryParameter("elementTouchStatsType", "tab_element")
                        .appendQueryParameter("formId", tab.formId)
                        .appendQueryParameter("statsId", safeStatsId);
                contentURL = "file:/" + builder.build().toString();
                decodedURL = URLDecoder.decode(contentURL, "UTF-8");
            }catch (Exception exc) {
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to initiate loading local web page: " + exc.getMessage());
                exc.printStackTrace();
            }
        }else{
            try {
                decodedURL =  URLDecoder.decode(tab.contentURL, "UTF-8");
            }catch (Exception exc) {
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to initiate loading remote web page: " + exc.getMessage());
            }
        }

        if(decodedURL != null){
            superWebView = new SuperWebViewFrame(activity.getApplicationContext(), decodedURL, tab.splashScreen);
            frameLayout.addView(superWebView);
        }else{
            // TODO
            // show error icon in the middle of the view
            // notify server
            Log.e(GlobalConstants.APP_LOG_TAG,"Cant create webview - url is null or mallformed");
        }


        initialized = true;
    }

    //************************* Overridden methods *****************************//


    @Override
    void startRendering()
    {
        initialize();
    }

    @Override
    void stopRendering()
    {
        try{
            if(superWebView!=null){

                if(frameLayout!=null && frameLayout.indexOfChild(superWebView)!=-1){
                    frameLayout.removeView(superWebView);
                }

                superWebView.destroy();
                superWebView = null;
            }
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to release WebView instance");
        }

    }
}
