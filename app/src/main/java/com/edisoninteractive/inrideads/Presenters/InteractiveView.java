package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.InterfaceLayout;
import com.edisoninteractive.inrideads.Entities.Params;
import com.edisoninteractive.inrideads.Entities.Response;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import java.io.File;
import java.net.URLDecoder;
import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.KILL_DELAYED_COMMANDS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;

/**
 * Created by Alex Angan one fine day
 */

public class InteractiveView extends InteractiveBlock
{
    private InterfaceLayout interfaceLayoutConfig;

    Activity activity;
    Params params;
    List<Response> adsList;
    private ImageButton ibReplay;
    private VideoView videoView;
    private MediaController mediaController;
    private SuperWebViewFrame superWebViewFrame;
    RelativeLayout.LayoutParams lprReal;
    private boolean isLocalWebResource = true;
    private RelativeLayout relativeLayout;
    Response currentInteractiveAd;
    String interactiveURL;



    public InteractiveView(String id, FrameLayout frameLayout, EventManager eventManager, InterfaceLayout interfaceLayoutConfig)
    {
        super(id, frameLayout, eventManager);

        this.interfaceLayoutConfig = interfaceLayoutConfig;
    }

    public void init(Activity activity, Params params, List<Response> adsList)
    {
        this.activity = activity;
        this.params = params;
        this.adsList = adsList;

        // Create layout params for general layout
        lprReal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        // Create general layout
        relativeLayout = new RelativeLayout(activity);
        relativeLayout.setLayoutParams(lprReal);

        // Get play icon
        Drawable iconPlay = activity.getResources().getDrawable(R.drawable.icon_play);

        // Prepare layout params for replay button
        RelativeLayout.LayoutParams lpWRAP_CONTENT =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpWRAP_CONTENT.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Create play button
        ibReplay = new ImageButton(activity);
        ibReplay.setLayoutParams(lpWRAP_CONTENT);
        ibReplay.setImageDrawable(iconPlay);
        ibReplay.setVisibility(View.INVISIBLE);

        ibReplay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    ibReplay.setVisibility(View.INVISIBLE);
                    mediaController.hide();
                    if (!videoView.isPlaying())
                    {
                        videoView.start();
                    }
                } catch (Exception ex)
                {
                    Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: videoView cannot play this video, " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        //relativeLayout.addView(ibReplay);
        frameLayout.addView(relativeLayout);

    }

    public void play(Response initiatorAd)
    {
        disposeExistingContent();

        adsList = FileUtils.reloadAdsList(null);

        //find interactive ad data
        if(adsList!=null){
            for (Response adDef :
                    adsList) {


                if((adDef.interactiveId!=null) && adDef.interactiveId.equals(initiatorAd.interactiveId)){
                    currentInteractiveAd = adDef;
                    interactiveURL = DATA_PATH + initiatorAd.interactFile;
                    break;
                }
            }
        }

        if(currentInteractiveAd != null){

            Log.d(APP_LOG_TAG, "InteractiveView - playLocalMediaFile: " + interactiveURL);

            File file = new File(interactiveURL);

            if (file.exists())
            {
                boolean isImageFile = FileUtils.hasImageExtension(interactiveURL);
                boolean isLocalHTMLFile = FileUtils.isFileOfType(interactiveURL, ".html");
                boolean isVideoFile = FileUtils.isFileOfType(interactiveURL, ".mp4");

                if (isImageFile)
                {
                    showImageInteractive();
                }
                else if(isLocalHTMLFile)
                {
                    showHTMLInteractive();
                }
                else if(isVideoFile)
                {
                    showVideoInteractive();
                }else{
                    Log.w(GlobalConstants.APP_LOG_TAG, "InteractiveView.playLocalMediaFile - interact file type is unknown");
                }
            }
        }

    }

    private void disposeWebView(){
        if(superWebViewFrame!=null){
            try{
                superWebViewFrame.destroy();
                relativeLayout.removeView(superWebViewFrame);
            }catch (Exception exc){
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to dispose webview");
            }

            superWebViewFrame = null;
        }
    }

    private void disposeVideoView(){
        if(videoView!=null){
            try{
                videoView.stopPlayback();
                relativeLayout.removeView(videoView);
                videoView = null;
            }catch (Exception exc){
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to dispose video view");
            }
            videoView = null;
        }
    }

    private void disposeExistingContent(){
        disposeVideoView();
        disposeWebView();
        currentInteractiveAd = null;
    }

    private void createAndPrepareVideoView()
    {
        videoView = new VideoView(activity);
        videoView.setLayoutParams(lprReal);

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
                {
                    String strExtra = String.format("Error(%s)", extra);
                    Log.e(APP_LOG_TAG, getClass().getSimpleName() + " - MediaPlayer error - " + strExtra + " , this video cannot be played");
                    return true;
                }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.stopPlayback();
                videoView.seekTo(0);
                mediaController.show(0);
                ibReplay.setVisibility(View.VISIBLE);
                Log.d(APP_LOG_TAG, "MediaPlayer item play completed");
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
                @Override
                public void onPrepared(MediaPlayer mp)
                {
                    Log.d(APP_LOG_TAG, "MediaPlayer item play starting");

                    ibReplay.setVisibility(View.INVISIBLE);
                    mediaController.hide();

                    videoView.start();
                    videoView.setBackground(null);
                }
        });



        mediaController = new MediaController(activity);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);


        /*if(videoView.getParent()==null){
            relativeLayout.addView(videoView);
        }

        if(ibReplay.getParent()==null){
            relativeLayout.addView(ibReplay);
        }*/


        if(videoView.getParent()==null) {
            relativeLayout.addView(videoView);
        }

        if(ibReplay.getParent()==null){
            relativeLayout.addView(ibReplay);
        }



    }

    private void showVideoInteractive(){
        createAndPrepareVideoView();
        videoView.setVideoPath(interactiveURL);
        videoView.start();
    }

    private void showHTMLInteractive(){

        superWebViewFrame = new SuperWebViewFrame(activity);
        relativeLayout.addView(superWebViewFrame);

        SharedPreferences appPreferences = activity.getSharedPreferences(GlobalConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        String pathToAssetsFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + GlobalConstants.DATA_ROOT_FOLDER_NAME;
        String unit_id = appPreferences.getString("unit_id", "0000");
        String decodedURL = null;
        String contentURL = null;

        Uri.Builder builder = new Uri.Builder();

        if(isLocalWebResource){
            try {
                builder.appendPath(interactiveURL)
                        .appendQueryParameter("pathToAssetsFolder", "file:///" + pathToAssetsFolder)
                        .appendQueryParameter("liveMode", "true")
                        .appendQueryParameter("dispatchMessages", "true")
                        .appendQueryParameter("broadcastMultipleMessages", "true")
                        .appendQueryParameter("useEdisonProtocol", "true")
                        .appendQueryParameter("unit_id", unit_id)
                        .appendQueryParameter("uuid", SystemUtils.getDeviceUUID(activity))
                        .appendQueryParameter("rnd", String.valueOf(Math.random()))
                        .appendQueryParameter("campaignId", String.valueOf(currentInteractiveAd.campaignId))
                        .appendQueryParameter("elementTouchStatsType", "interactive_element")
                        .appendQueryParameter("formId", String.valueOf(currentInteractiveAd.adId))
                        .appendQueryParameter("statsId", String.valueOf(currentInteractiveAd.adId));
                contentURL = "file:/" + builder.build().toString();
                decodedURL = URLDecoder.decode(contentURL, "UTF-8");
            }catch (Exception exc) {
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to initiate loading local web page: " + exc.getMessage());
            }
        }else{
            try {
                decodedURL =  URLDecoder.decode(interactiveURL, "UTF-8");
            }catch (Exception exc) {
                Log.e(GlobalConstants.APP_LOG_TAG, "Failed to initiate loading remote web page: " + exc.getMessage());
            }
        }

        if(decodedURL != null){
            //superWebView = new SuperWebViewFrame(activity.getApplicationContext(), decodedURL, tab.splashScreen);
            superWebViewFrame.loadURL(decodedURL, null);
            superWebViewFrame.setVisibility(View.VISIBLE);
        }else{
            // TODO
            // show error icon in the middle of the view
            // notify server
            Log.e(GlobalConstants.APP_LOG_TAG,"Cant create webview - url is null");
        }
    }

    private void showImageInteractive()
    {
        //create new videoView
        createAndPrepareVideoView();

        Drawable d = Drawable.createFromPath(interactiveURL);

        if (d != null)
        {
            videoView.setBackground(d);
        }
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        if (commandWithParams.strCommand.equals(HIDE_BLOCK))
        {
            disposeExistingContent();
            frameLayout.setVisibility(View.INVISIBLE);

            /*videoView.stopPlayback();
            videoView.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.INVISIBLE);

            if(superWebViewFrame!=null){
               superWebViewFrame.setVisibility(View.GONE);
               superWebViewFrame.destroy();
               superWebViewFrame = null;
            }*/

        } else if (commandWithParams.strCommand.equals(SHOW_BLOCK))
        {
            frameLayout.setVisibility(View.VISIBLE);

        } else if (commandWithParams.strCommand.equals(KILL_DELAYED_COMMANDS))
        {
            CountDownTimer countDownTimer = al_CountDownTimers.get(id);

            if (countDownTimer != null)
            {
                countDownTimer.cancel();
            }
        }
    }
}
