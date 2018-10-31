package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.InterfaceLayout;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.KILL_DELAYED_COMMANDS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;

/**
 * Created by Alex Angan 2018.08.09
 */

public class Banner extends InteractiveBlock
{
    private String className = getClass().getSimpleName();
    private InterfaceLayout interfaceLayoutConfig;
    Activity activity;
    private AdView mAdView;
    private AdRequest adRequest;

    public Banner(String id, FrameLayout frameLayout, EventManager eventManager, InterfaceLayout interfaceLayoutConfig)
    {
        super(id, frameLayout, eventManager);

        this.interfaceLayoutConfig = interfaceLayoutConfig;
    }

    public void init(Activity activity)
    {
        this.activity = activity;

        if(mAdView == null)
        {
            mAdView = new AdView(activity);

            final RelativeLayout relativeLayout = new RelativeLayout(activity);

            RelativeLayout.LayoutParams lprReal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            relativeLayout.setLayoutParams(lprReal);

            lprReal.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            relativeLayout.setGravity(Gravity.CENTER);

            mAdView.setLayoutParams(lprReal);
            mAdView.setAdSize(new AdSize(320, 130)); // AdSize.LARGE_BANNER
            mAdView.setAdUnitId("ca-app-pub-2756860740531206/5262281181");

            if(adRequest == null)
            {
                adRequest = new AdRequest.Builder()
                        .addTestDevice("E599A0B765143CCF0E505522F958892D") // 8ec7 7
                        .addTestDevice("802082E6B9A5B4726A850C154D5540BF") // 29c9
                        .addTestDevice("7E03CBEF4EC3FA81589809F9F9A610F8") // f118
                        .addTestDevice("ED5BA28527EC008BCDA0FC41F97501CA") // 06d9
                        .addTestDevice("8D9EDD488BD071FCEB8B8F76BF95CC07") // bcfa 7
                        .addTestDevice("2FC8D4001B360D3F668493CF091904CD") // e783
                        .build();
            }

            mAdView.setAdListener(new AdListener()
            {
                @Override
                public void onAdLoaded()
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(int errorCode)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdFailedToLoad, errorCode: " + String.valueOf(errorCode));
                }

                @Override
                public void onAdOpened()
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdOpened");
                }

                @Override
                public void onAdLeftApplication()
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdLeftApplication");
                }

                @Override
                public void onAdClosed()
                {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdClosed");

                    mAdView.loadAd(adRequest);
                }
            });

            relativeLayout.addView(mAdView);
            frameLayout.addView(relativeLayout);
        }

        mAdView.loadAd(adRequest);
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        if (commandWithParams.strCommand.equals(HIDE_BLOCK))
        {
            frameLayout.setVisibility(View.INVISIBLE);

        } else if (commandWithParams.strCommand.equals(SHOW_BLOCK))
        {
            frameLayout.setVisibility(View.VISIBLE);

        } else if (commandWithParams.strCommand.equals(KILL_DELAYED_COMMANDS))
        {

        }
    }
}
