package com.edisoninteractive.inrideads.Interfaces;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.aol.mobile.sdk.player.OneSDK;
import com.aol.mobile.sdk.player.view.PlayerView;

/**
 * Created by Alex Angan on 16.02.2018.
 */

public interface AolPlayerEventsListener
{
    void onBuildForVideoError(String methodTitle, String errMsg);

    void onPlayError(String methodTitle, String errMsg);

    void onOneSDKregisterResult(OneSDK oneSDK, final PlayerView aolPlayerView, final ImageView imageView,
                                @NonNull String playSequence, boolean isPlaylist, boolean loopList);

    void onPlayListCompleted(String methodTitle);

    void onListItemNotPlayable(String methodTitle);

    void onAdLoadingStarted();

    void onAdLoadingFinished();

    void onAdPlayStarted();

    void onListItemLoadingStarted(String methodTitle, String videoTitle);

    void onListItemLoadingFinished(String methodTitle, String videoTitle);

    void onListItemPlayStarted(String methodTitle, String videoTitle);

    void onListItemPlayFinished(String methodTitle, String videoTitle);

    void onPlayListPlayStarted(String methodName, String title);
}
