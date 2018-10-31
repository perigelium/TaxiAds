package com.edisoninteractive.inrideads.EventHandlers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;

import java.util.Vector;


/**
 *  WebViewManager
 *
 *  24-MAY-2018
 *
 *  Edison Interactive
 */
public class WebViewManager {

    private static final WebViewManager ourInstance = new WebViewManager();

    public static WebViewManager getInstance() {
        return ourInstance;
    }

    private Vector<WebViewRecord> webviewRecords;

    private WebViewManager() {
        webviewRecords = new Vector<WebViewRecord>();
    }

    public void unlockWebView(WebView instance){

        final WebViewRecord targetRecord;

        for (WebViewRecord record:
                webviewRecords) {
            if(record.webView == instance){

                targetRecord = record;

                //reset webview settings
                WebViewClient webViewClient = new WebViewClient();
                targetRecord.webView.setWebViewClient(webViewClient);

                //reset webview content to blank page
                targetRecord.webView.loadUrl("about:blank");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        targetRecord.locked = false;
                        Log.d(GlobalConstants.APP_LOG_TAG, "WebView unloaded and unlocked : " + targetRecord.id);
                    }
                }, 600);

                break;
            }
        }

    }

    public WebView getFreeWebView(Context context){
        WebView result = null;

        for (WebViewRecord record:
             webviewRecords) {
            if(!record.locked){
                record.locked = true;
                Log.d(GlobalConstants.APP_LOG_TAG, "WebView reused and locked :" + record.id);
                result = record.webView;
                break;
            }
        }

        if(result==null){
            result = new WebView(context);

            WebViewRecord newRec = new WebViewRecord();
            newRec.id = (long) (Math.random()*500);
            newRec.locked = true;
            newRec.webView = result;
            webviewRecords.add(newRec);
            Log.d(GlobalConstants.APP_LOG_TAG, "WebView created and locked :" + newRec.id + " count=" + webviewRecords.size());
        }
        
        return result;
    }
}

class WebViewRecord {
    public long id;
    public WebView webView;
    public boolean locked;
}
