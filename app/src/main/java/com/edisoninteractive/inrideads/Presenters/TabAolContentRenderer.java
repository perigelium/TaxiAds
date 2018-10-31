package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aol.mobile.sdk.player.view.PlayerView;
import com.edisoninteractive.inrideads.Entities.Tab;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.EventHandlers.StatsManager;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.ViewUtils;
import com.edisoninteractive.inrideads.ViewOverrides.MyContentControlsView;

import static com.edisoninteractive.inrideads.MainActivity.aolAdPlayers;

/**
 * Created by Alex Angan on 29.01.2018.
 */

public class TabAolContentRenderer extends TabContentRenderer
{
    private final PlayerView aolPlayerView;
    private final ImageView imageView;
    private AolAdPlayer aolAdPlayer;
    private Activity activity;

    public TabAolContentRenderer(final Activity activity, FrameLayout frameLayout, final Tab tab)
    {
        super(frameLayout, tab);

        this.activity = activity;
        aolPlayerView = new PlayerView(activity);
        imageView = new ImageView(activity);

        // Relative layout supports rules for stretching video to whole screen
        final RelativeLayout relativeLayout = new RelativeLayout(activity);

        RelativeLayout.LayoutParams lprReal = new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        relativeLayout.setLayoutParams(lprReal);

        lprReal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        imageView.setLayoutParams(lprReal);
        aolPlayerView.setLayoutParams(lprReal);

        // Enable/disable displaying of certain view controls
        MyContentControlsView myContentControlsView = new MyContentControlsView(activity);
        aolPlayerView.setContentControls(myContentControlsView);

        relativeLayout.addView(aolPlayerView);
        relativeLayout.addView(imageView);
        frameLayout.addView(relativeLayout);

        aolAdPlayer = aolAdPlayers.get("tab");

        imageView.setOnTouchListener(new OnTouchDebouncedListener()
        {
            @Override
            public boolean onTouched(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    StatsManager.getInstance(aolPlayerView.getContext()).writeStats(StatsManager.AD_ROTATOR_PRESS,
                            String.valueOf(tab.statsId),
                            String.valueOf(tab.campaignId),
                            "null");
                }

                return false;
            }
        });
    }

    public void startRendering()
    {
        if(tab.playList != null)
        {
            if (!tab.playList.isEmpty() && NetworkUtils.isNetworkAvailable(activity))
            {
                ViewUtils.setImageViewBackground(imageView, tab.splashScreen);
                aolAdPlayer.loadListAndPlay(aolPlayerView, imageView, tab.splashScreen, tab.playList, tab.isPlaylist, tab.loop);
            }
        }
    }

    @Override
    void stopRendering()
    {
        if(aolAdPlayer != null)
        {
            aolAdPlayer.pause();
        }
    }
}
