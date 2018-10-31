package com.edisoninteractive.inrideads;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.AD_MOB_KEY;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan one fine day
 */

public class inRideAdsApp extends Application
{
    private static inRideAdsApp instance;

    public static inRideAdsApp get()
    {
        return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;

        if (!BuildConfig.DEBUG)
        {
/*            Fabric.with(this, new Crashlytics());

            final MyExceptionHandler myExceptionHandler = new MyExceptionHandler(this);
            Thread.setDefaultUncaughtExceptionHandler(myExceptionHandler);*/

/*            new ANRWatchDog(15000) // ANR interval in milliseconds
                    .setANRListener(new ANRWatchDog.ANRListener()
                    {
                        @Override
                        public void onAppNotResponding(ANRError error)
                        {
                            Log.e(APP_LOG_TAG, "AppNotResponding error: " + error.getMessage());
                            myExceptionHandler.uncaughtException(Thread.currentThread(), error);
                        }
                    }).setIgnoreDebugger(true) // set true in case ANR detects needed in debug mode
                    .start();*/
        }

        if (BuildConfig.ENABLE_AD_MOB)
        {
            MobileAds.initialize(this, AD_MOB_KEY); // Edison Interactive ID
        } else
        {
            Log.w(APP_LOG_TAG, " - AD_MOB is disabled");
        }
    }


/*        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);*/

    //GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);


}
