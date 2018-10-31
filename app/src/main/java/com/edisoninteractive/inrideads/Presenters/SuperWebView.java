package com.edisoninteractive.inrideads.Presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.StatsManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Creator on 1/29/2018.
 * A wrapper around webview component that displays and
 * communicates with local/customized html content
 */

public class SuperWebView extends WebView {

    private Context appContext;
    private WebSettings webSettings;
    private String url;
    private String messagingProtocol;

    private static final String EDISON_PROTOCOL = "edison://";
    private static final String DATA_PROTOCOL = "data://";

    //*********************************** Constructor *****************************************//
    public SuperWebView(Context context, String url){
        super(context);
        this.appContext = context;
        this.url = url;
        //init();
    }

    //********************************** Private methods **************************************//
    private void init() {
        webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + GlobalConstants.DATA_ROOT_FOLDER_NAME);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setUseWideViewPort(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setEnableSmoothTransition(true);
        webSettings.setLoadWithOverviewMode(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            WebView.setWebContentsDebuggingEnabled(true);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        WebViewClient webViewClient = new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.w(GlobalConstants.APP_LOG_TAG, "onLoadResource");
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.w(GlobalConstants.APP_LOG_TAG, "onReceivedError");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.w(GlobalConstants.APP_LOG_TAG, "onReceivedError");
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.w(GlobalConstants.APP_LOG_TAG, "onReceivedHttpError");
            }


            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                Log.w(GlobalConstants.APP_LOG_TAG, "onPageCommitVisible");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.w(GlobalConstants.APP_LOG_TAG, "onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.w(GlobalConstants.APP_LOG_TAG, "onPageFinished");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean result = false; // whether to cancel loading new URL

                //detect protocol
                if(url.contains(SuperWebView.EDISON_PROTOCOL)){
                    messagingProtocol = SuperWebView.EDISON_PROTOCOL;
                }else if(url.contains(SuperWebView.DATA_PROTOCOL)){
                    messagingProtocol = SuperWebView.DATA_PROTOCOL;
                }else{
                    messagingProtocol = null;
                    return result;
                }

                if (url.contains(messagingProtocol)) {  // Do not navigate to new URL if WebView input data format detected
                    Log.d(GlobalConstants.APP_LOG_TAG, "WebView input detected!");


                    String commandStringProcessed = url.replace(messagingProtocol, "");
                    JSONObject commandJSON = null;

                    try {
                        commandJSON = new JSONObject(commandStringProcessed);

                        if (commandJSON.has("messages")) {
                            JSONArray commandsArray = commandJSON.getJSONArray("messages");
                            for (int i = 0; i < commandsArray.length(); i++) {
                                JSONObject cmdObject = commandsArray.getJSONObject(i);
                                processMessageFromWebView(cmdObject);
                            }
                        } else {
                            processMessageFromWebView(commandJSON);
                        }

                    } catch (Exception exc) {
                        Log.e(GlobalConstants.APP_LOG_TAG, "Failed to convert webview response to json");
                    }

                    result = true;
                } else {
                    result = false;
                }

                return result;
            }
        };

        setWebViewClient(webViewClient);




        loadUrl(url);
        //loadUrl("http://google.com");
    }


    public void initialize(){
        init();
    }


    /**
     *  Handles message delivered from webview
     * @param commandJSON
     */
    private void processMessageFromWebView(JSONObject commandJSON){
        Log.w(GlobalConstants.APP_LOG_TAG, "Got a command from webview");
        String commandName = null;
        JSONObject params = null;

        try{
            commandName = commandJSON.getString("command");
            params = commandJSON.getJSONObject("params");
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to process message from webview: mallformed command name or missing command params");
            return;
        }

        switch (commandName){
            case "writeStats":
                try{
                    String campaignId =  null;
                    String details = null;
                    String internalStatsType = null;
                    String publicStatsType = null;
                    String statsId = null;

                    if(params.has("campaignId")){
                        campaignId = params.getString("campaignId");
                    }

                    if(params.has("details")){
                        details = params.getString("details");
                    }

                    if(params.has("type")){
                        internalStatsType = params.getString("type");
                        publicStatsType = getPublicStatsType(internalStatsType);
                    }

                    if(params.has("statsId")){
                        statsId = params.getString("statsId");
                    }


                    try {
                        Log.d(GlobalConstants.APP_LOG_TAG, "Presenters/ContentBlockView: processMessageFromWebView() writeStats");
                        StatsManager.getInstance(appContext).writeStats(publicStatsType, statsId, campaignId, details);
                    } catch (Exception ex) {
                        Log.d(GlobalConstants.APP_LOG_TAG, "Presenters/ContentBlockView: processMessageFromWebView() writeStats EXCEPTION : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }catch (Exception writeStatsExc){
                    Log.e(GlobalConstants.APP_LOG_TAG, "Failed to write stats generated by webview");
                }
                break;


            case "report_user_activity":

            break;

            case "invokeJSMethod":

            break;

        }
    }

    /**
     *
     * @param internalStatsType
     * @return
     */
    private String getPublicStatsType(String internalStatsType){
        String result = null;

        HashMap<String, String> legacyStatsHashMap = new HashMap<>();
        legacyStatsHashMap.put("ad_rotator", "1");
        legacyStatsHashMap.put("interactive", "2");
        legacyStatsHashMap.put("button", "3");
        legacyStatsHashMap.put("toggle_btn", "4");
        legacyStatsHashMap.put("tab", "5");
        legacyStatsHashMap.put("rss_item_thumb", "6");
        legacyStatsHashMap.put("listings_item_thumb", "7");
        legacyStatsHashMap.put("ad_rotator_press", "8");
        legacyStatsHashMap.put("form_input", "9");
        legacyStatsHashMap.put("channel", "10");
        legacyStatsHashMap.put("tab_element", "11");
        legacyStatsHashMap.put("interactive_element", "12");
        legacyStatsHashMap.put("face_detected", "13");
        legacyStatsHashMap.put("uptime", "14");
        legacyStatsHashMap.put("acc_on", "15");
        legacyStatsHashMap.put("acc_off", "16");

        result = legacyStatsHashMap.get(internalStatsType);

        return result;
    }


}