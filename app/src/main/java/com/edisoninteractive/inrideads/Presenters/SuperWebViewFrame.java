package com.edisoninteractive.inrideads.Presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.edisoninteractive.inrideads.BuildConfig;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.StatsManager;
import com.edisoninteractive.inrideads.EventHandlers.WebViewManager;
import com.edisoninteractive.inrideads.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;

/**
 * Created by Creator on 2/12/2018.
 */

public class SuperWebViewFrame extends FrameLayout {

    private WebView webView;
    private WebSettings webSettings;
    private String contentURL;
    private String splashScreenURL;
    private String messagingProtocol;
    private SharedPreferences appPreferences;
    private View splashView;
    private boolean useSplashScreen;
    private File splashScreenFile;


    private static final String EDISON_PROTOCOL = "edison://";
    private static final String OLD_DATA_PROTOCOL = "data://";

    //****************************** Constructor **************************************//



    public SuperWebViewFrame(@NonNull Context context){
        super(context);
    }

    public SuperWebViewFrame(@NonNull Context context, @Nullable String contentURL, @Nullable String splashScreenURL){
        super(context);
        this.contentURL = contentURL;
        this.splashScreenURL = splashScreenURL;
        init();
    }

    //******************************* Overridden methods ********************************//



    //******************************** Public methods ***********************************//

    public void loadURL(@NonNull String contentURL, @Nullable String splashScreenURL){
        this.contentURL = contentURL;
        this.splashScreenURL = splashScreenURL;
        init();
    }

    public void destroy(){
         try{
            webView.stopLoading();
            removeView(webView);
            WebViewManager.getInstance().unlockWebView(webView);
            webView = null;
            Log.d(GlobalConstants.APP_LOG_TAG, "SuperWebView destroyed");
         }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "Failed to destroy SuperWebView instance");
         }
    }


    //******************************** Private methods **********************************//

    private void init() {

        appPreferences = getContext().getSharedPreferences(GlobalConstants.APP_PREFERENCES, Context.MODE_PRIVATE);

        webView = WebViewManager.getInstance().getFreeWebView(getContext());
        webView.setBackgroundColor(0xffffffff);
        this.addView(webView);
        webView.setVisibility(View.GONE);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
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

        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            WebView.setWebContentsDebuggingEnabled(true);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        final WebViewClient webViewClient = new WebViewClient() {


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.d(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame webViewClient.onReceivedError()");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.d(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame webViewClient.onReceivedError()");
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.d(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame webViewClient.onReceivedHttpError()");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame webViewClient.onPageFinished");


                // force video playback via calling window.autoPlayMedia function
                view.loadUrl("javascript:(function(){ if(typeof window.autoPlayMedia == 'function'){try{window.autoPlayMedia();}catch(error){console.log('Failed to call window.autoPlayMedia()')}}else{console.log('Page doesnt define window.autoPlayMedia()')}  })()");


                webView.setVisibility(VISIBLE);
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setDuration(150);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        splashView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                splashView.setAnimation(fadeOut);



            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean result = false; // whether to cancel loading new URL

                //detect protocol
                if(url.contains(EDISON_PROTOCOL)){
                    messagingProtocol = EDISON_PROTOCOL;
                }else if(url.contains(OLD_DATA_PROTOCOL)){
                    messagingProtocol = OLD_DATA_PROTOCOL;
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


        webView.setWebViewClient(webViewClient);
        webView.loadUrl(contentURL);


        try{
            if(splashScreenURL!=null){
                splashScreenFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + GlobalConstants.DATA_ROOT_FOLDER_NAME + splashScreenURL);
                if(splashScreenFile.exists()){

                    Drawable d = Drawable.createFromPath(splashScreenFile.getAbsolutePath());

                    if (d != null)
                    {
                        splashView = new ImageView(getContext());
                        RelativeLayout.LayoutParams lprReal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        splashView.setLayoutParams(lprReal);
                        splashView.setBackground(d);
                        addView(splashView);
                    }else{
                        Log.w(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame.init() failed to create drawable from splashscreen path. Creating default one");
                        showDefaultSplashScreen();
                    }
                }else{
                    Log.w(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame.init() failed to create splashscreen as file doesnt exist");
                    showDefaultSplashScreen();
                }
            }else{
                Log.d(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame.init() splashScreenURL not set - using default one");
                showDefaultSplashScreen();
            }

        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame.init() - failed to create splashscreen");
            exc.printStackTrace();
        }
    }


    private void showDefaultSplashScreen(){

        try{
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            splashView = inflater.inflate(R.layout.default_wv_splash, null);
            addView(splashView);
        }catch (Exception exc){
            Log.e(GlobalConstants.APP_LOG_TAG, "SuperWebViewFrame.showDefaultSplashScreen() failed to create default splash screen");
            exc.printStackTrace();
        }
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

        if ( BuildConfig.DEBUG)
        {
            Toast.makeText(getContext(),"WebView command:" + commandName, Toast.LENGTH_LONG).show();
        }

        switch (commandName){
            case "writeStats":
                try{

                    String campaignId =  null;
                    String details = null;
                    String internalStatsType = null;
                    String publicStatsType = null;
                    String statsId = null;
                    String detailsString = null;

                    if(params.has("campaignId")){
                        campaignId = params.getString("campaignId");
                    }

                    if(params.has("details")){
                        details = params.getString("details");
                        detailsString = URLDecoder.decode(details, "UTF-8");
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
                        StatsManager.getInstance(getContext()).writeStats(publicStatsType, statsId, campaignId, detailsString);
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
