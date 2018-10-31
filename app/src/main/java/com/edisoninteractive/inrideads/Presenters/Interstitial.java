package com.edisoninteractive.inrideads.Presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.edisoninteractive.inrideads.BuildConfig;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.AD_MOB_LIVE_INTERSTITIAL_AD_ID;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.AD_MOB_TEST_INTERSTITIAL_AD_ID;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.AD_MOB_TIMESTAMP_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.INTERSTITIAL_AD_DURATION;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.INTERSTITIAL_AD_PERIOD_BASE;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.INTERSTITIAL_AD_PERIOD_DEVIATION;

public class Interstitial
{
    private String className = getClass().getSimpleName();
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;

    private static volatile Interstitial instance;

    public static Interstitial getInstance()
    {
        if (instance == null)
        {
            synchronized (Interstitial.class)
            {
                if (instance == null)
                {
                    instance = new Interstitial();
                }
            }
        }
        return instance;
    }

    private Interstitial()
    {
    }

    public void init(final Context context)
    {
        if (mInterstitialAd == null)
        {
            mInterstitialAd = new InterstitialAd(context);

            if (BuildConfig.DEBUG)
            {
                mInterstitialAd.setAdUnitId(AD_MOB_TEST_INTERSTITIAL_AD_ID);
            } else
            {
                mInterstitialAd.setAdUnitId(AD_MOB_LIVE_INTERSTITIAL_AD_ID);
            }

            mInterstitialAd.setAdListener(new AdListener()
            {
                @Override
                public void onAdLoaded()
                {
                    // Code to be executed when an ad finishes loading.
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(int errorCode)
                {
                    // Code to be executed when an ad request fails.
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdFailedToLoad, errorCode: " + String.valueOf(errorCode));
                }

                @Override
                public void onAdOpened()
                {
                    // Code to be executed when the ad is displayed.
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdOpened");

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Intent intent = new Intent(context, context.getClass());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);  // FLAG_ACTIVITY_REORDER_TO_FRONT
                            context.startActivity(intent);

                        }
                    }, INTERSTITIAL_AD_DURATION);
                }

                @Override
                public void onAdLeftApplication()
                {
                    // Code to be executed when the user has left the app.
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdLeftApplication");
                }

                @Override
                public void onAdClosed()
                {
                    // Code to be executed when when the interstitial ad is closed.
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - : onAdClosed");

                    if (!mInterstitialAd.isLoaded() && !mInterstitialAd.isLoading())
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " The interstitial wasn't loaded yet, trying to load...");

                        mInterstitialAd.loadAd(adRequest);
                    }
                }
            });
        }

        if (adRequest == null)
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

        if (!mInterstitialAd.isLoaded() && !mInterstitialAd.isLoading())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " The interstitial wasn't loaded yet, trying to load...");

            mInterstitialAd.loadAd(adRequest);
        }
    }

    public void tryToShowAdd()
    {
        if (mInterstitialAd.isLoaded())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " The interstitial is loaded.");
            long unixTimeNow = System.currentTimeMillis();

            long nextTimeInterstitialAdPlayAllowed = getNextTimeInterstitialAdPlayAllowed(unixTimeNow);

            if (nextTimeInterstitialAdPlayAllowed < unixTimeNow)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " Showing the interstitial ad");

                if(writeTimestampAsJsonFile("ad_mob_timestamp", unixTimeNow))
                {
                    mInterstitialAd.show();
                }
            } else
            {
                Log.w(APP_LOG_TAG, "Next time interstitial start allowed in " + String.valueOf((nextTimeInterstitialAdPlayAllowed - unixTimeNow) / 1000 / 60) + " minutes");
            }
        } else if (!mInterstitialAd.isLoading())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " The interstitial wasn't loaded yet, trying to load...");

            mInterstitialAd.loadAd(adRequest);
        }
    }

    private long getNextTimeInterstitialAdPlayAllowed(long unixTimeNow)
    {
        long nextTimeInterstitialAdPlayAllowed = 0;
        File fileAdMobTimeStamp = new File(AD_MOB_TIMESTAMP_PATH);

        if (fileAdMobTimeStamp.exists())
        {
            nextTimeInterstitialAdPlayAllowed = unixTimeNow;
            String strFileContent = FileUtils.readFileToString(fileAdMobTimeStamp);

            try
            {
                JSONObject jsonObject = new JSONObject(strFileContent);
                String strAdMobTimestamp = jsonObject.get("ad_mob_timestamp").toString();
                nextTimeInterstitialAdPlayAllowed = Long.valueOf(strAdMobTimestamp);

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return nextTimeInterstitialAdPlayAllowed;
    }

    private Boolean writeTimestampAsJsonFile(String entryName, long unixTimeNow)
    {
        Boolean result = false;
        long nextTimeInterstitialAdPlayAllowed;
        Random rnd = new Random();

        int adShift = rnd.nextInt(INTERSTITIAL_AD_PERIOD_DEVIATION);
        int randomizedAdPeriod = INTERSTITIAL_AD_PERIOD_BASE + adShift;

        nextTimeInterstitialAdPlayAllowed = unixTimeNow + randomizedAdPeriod + INTERSTITIAL_AD_DURATION;

        String strJson = "{\"" + entryName + "\":\"" + String.valueOf(nextTimeInterstitialAdPlayAllowed) + "\"}";

        if(FileUtils.writeStringToFile(AD_MOB_TIMESTAMP_PATH, strJson))
        {
            result = true;
        }

        return result;
    }
}
