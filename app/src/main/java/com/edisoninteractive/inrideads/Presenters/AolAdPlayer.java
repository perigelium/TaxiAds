package com.edisoninteractive.inrideads.Presenters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.aol.mobile.sdk.player.Binder;
import com.aol.mobile.sdk.player.EmptyPlaylistException;
import com.aol.mobile.sdk.player.InvalidRendererException;
import com.aol.mobile.sdk.player.OneSDK;
import com.aol.mobile.sdk.player.OneSDKBuilder;
import com.aol.mobile.sdk.player.Player;
import com.aol.mobile.sdk.player.PlayerStateObserver;
import com.aol.mobile.sdk.player.VideoProvider;
import com.aol.mobile.sdk.player.VideoProviderResponse;
import com.aol.mobile.sdk.player.http.model.Environment;
import com.aol.mobile.sdk.player.listener.ErrorListener;
import com.aol.mobile.sdk.player.model.ErrorState;
import com.aol.mobile.sdk.player.model.properties.Properties;
import com.aol.mobile.sdk.player.model.properties.VideoProperties;
import com.aol.mobile.sdk.player.view.PlayerView;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Interfaces.AolPlayerEventsListener;
import com.edisoninteractive.inrideads.Interfaces.AolPlayerStatesListener;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.ViewUtils;
import com.edisoninteractive.inrideads.inRideAdsApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

public class AolAdPlayer implements AolPlayerEventsListener
{
    private Context context;
    private Binder binder;

    boolean isAolLoadingOrPlaying;
    private AolPlayerEventsListener aolPlayerEventsListener;
    private OneSDK oneSDKinstance;
    private AolPlayerStatesListener aolPlayerStatesListener;
    private final String className = getClass().getSimpleName();
    private ImageView imageView;
    private String strIvBackgroundUrl;
    private Boolean loopList;
    private final String aolCacheFolderName = "aol_cache";

    public AolAdPlayer()
    {
        context = inRideAdsApp.get();

        if (binder == null)
        {
            binder = new Binder();
        }

        if (this.aolPlayerEventsListener == null)
        {
            setAolPlayerEventsListener(this);
        }

        File dir = new File(DATA_PATH, aolCacheFolderName);

        if (!dir.exists())
        {
            dir.mkdirs();
        }

        initOneSDK();
    }

    public void subscribeListener(InteractiveBlock interactiveBlock)
    {
        if (this.aolPlayerStatesListener == null)
        {
            setAolPlayerStatesListener((AolPlayerStatesListener) interactiveBlock);
        }
    }

    void loadListAndPlay(@NonNull PlayerView aolPlayerView, ImageView imageView, String strIvBackgroundUrl,
                         @NonNull String playSequence, Boolean isPlaylist, Boolean loopList)
    {
        this.imageView = imageView;
        this.strIvBackgroundUrl = strIvBackgroundUrl;
        this.loopList = loopList;

        if(!NetworkUtils.isNetworkAvailable(context))
        {
            if (aolPlayerStatesListener != null)
            {
                aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
            }
            return;
        }

        if (oneSDKinstance == null)
        {
            initOneSDK_andPlay(aolPlayerView, imageView, playSequence, isPlaylist, loopList);
            return;
        }

        try
        {
            binder.setPlayerView(aolPlayerView);

            if (isPlaylist)
            {
                Log.d(APP_LOG_TAG, className + " - " + " playList: aolItemPath = " + playSequence);

                useSDKforCachedPlaylist(playSequence);
            } else
            {
                Log.d(APP_LOG_TAG, className + " - " + " videoList: aolItemPath = " + playSequence);
                String[] strSplitRes = playSequence.split(",");

                useSDKforCachedVideolist(strSplitRes);
            }
        } catch (Exception e)

        {
            e.printStackTrace();
        }
    }

    private void initOneSDK()
    {
        if(!NetworkUtils.isNetworkAvailable(context))
        {
            return;
        }

        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject("{\"preferMP4\":true}");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new OneSDKBuilder(context)
                .setEnvironment(Environment.PRODUCTION)
                .setExtra(jsonObject)
                .create(new OneSDKBuilder.Callback()
        {
            public void onSuccess(@NonNull OneSDK oneSDK)
            {
                Log.d(APP_LOG_TAG, className + " - initOneSDK_andPlay: create new OneSDKBuilder success");

                oneSDKinstance = oneSDK;
            }

            public void onFailure(@NonNull Exception error)
            {
                Log.d(APP_LOG_TAG, className + " - " + ": create new OneSDKBuilder failure, " + error.getMessage());
            }
        });
    }

    private void initOneSDK_andPlay(final PlayerView aolPlayerView, final ImageView imageView, @NonNull final String playSequence, final Boolean isPlaylist, final Boolean loopList)
    {
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject("{\"preferMP4\":true}");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new OneSDKBuilder(context)
                .setEnvironment(Environment.PRODUCTION)
                .setExtra(jsonObject)
                .create(new OneSDKBuilder.Callback()
        {
            public void onSuccess(@NonNull OneSDK oneSDK)
            {
                Log.d(APP_LOG_TAG, className + " - initOneSDK_andPlay: create new OneSDKBuilder success");

                oneSDKinstance = oneSDK;
                aolPlayerEventsListener.onOneSDKregisterResult(oneSDK, aolPlayerView, imageView, playSequence, isPlaylist, loopList);
            }

            public void onFailure(@NonNull Exception error)
            {
                Log.d(APP_LOG_TAG, className + " - " + ": create new OneSDKBuilder failure, " + error.getMessage());

                aolPlayerEventsListener.onOneSDKregisterResult(null, aolPlayerView, imageView, playSequence, isPlaylist, loopList);
            }
        });
    }

    private void useSDKforCachedVideolist(final String[] aolPlayList)
    {
        final String methodName = "useSDKforCachedVideolist";
        Log.i(APP_LOG_TAG, className + " - " + methodName + ": started");

        VideoProvider videoProvider = oneSDKinstance.getVideoProvider();
        videoProvider.requestPlaylistModel(aolPlayList, true, null, new VideoProvider.Callback()
        {
            @Override
            public void success(@NonNull VideoProviderResponse videoProviderResponse)
            {
                Log.i(APP_LOG_TAG, className + " - useSDKforCachedVideolist requestPlaylistModel success");

                // Truncating list to 3 items
                VideoProviderResponse.PlaylistItem[] playlistItems = videoProviderResponse.playlistItems;
                int size = playlistItems.length <= 3 ? playlistItems.length : 3;

                ArrayList<VideoProviderResponse.PlaylistItem> al_cachedItems = new ArrayList<>();

                for (int i = 0; i < size; i++)
                {
                    VideoProviderResponse.PlaylistItem oldItem = playlistItems[i];

                    if(oldItem == null)
                    {
                        continue;
                    }

                    if (oldItem.video != null)
                    {
                        String fileName = "/" + aolCacheFolderName + "/" + oldItem.video.id + ".mp4";
                        File file = new File(DATA_PATH, fileName);

                        if (file.exists())
                        {
                            Log.i(APP_LOG_TAG, className + " - requestPlaylistModel found in cache: " + oldItem.video.id + ".mp4");
                            VideoProviderResponse.Video cachedVideo = oldItem.video.withUrl(file.getPath());
                            VideoProviderResponse.PlaylistItem newItem = new VideoProviderResponse.PlaylistItem(cachedVideo);
                            al_cachedItems.add(newItem);
                        }
                    }
                    else if(oldItem.voidVideo != null)
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": useSDKforCachedVideolist - requestPlaylistModel: " + oldItem.voidVideo.reason);
                    }
                }

                if(al_cachedItems.size() == 0)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": useSDKforCachedVideolist - requestPlaylistModel: al_cachedItems is empty");

                    if (aolPlayerStatesListener != null)
                    {
                        aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                    }
                    return;
                }

                VideoProviderResponse.PlaylistItem[] newItems = al_cachedItems.toArray(new VideoProviderResponse.PlaylistItem[al_cachedItems.size()]);
                VideoProviderResponse modifiedResponse = videoProviderResponse.withPlaylistItems(newItems);

                try
                {
                    Player player = oneSDKinstance.createBuilder().buildFrom(modifiedResponse);
                    Log.i(APP_LOG_TAG, className + " - oneSDKinstance.createBuilder().buildFrom(modifiedResponse) success ");

                    addAolPlayerStateObserver(player, methodName);

                    addAolPlayerErrorListener(player, methodName);

                    if (binder != null)
                    {
                        binder.setPlayer(player);
                    }
                } catch (EmptyPlaylistException | InvalidRendererException e)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - oneSDKinstance.createBuilder().buildFrom error: " + e.getMessage());

                    if (aolPlayerStatesListener != null)
                    {
                        aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                    }
                }
            }

            @Override
            public void error(@NonNull Exception e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " - useSDKforCachedVideolist requestPlaylistModel error: " + e.getMessage());

                if (aolPlayerStatesListener != null)
                {
                    aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                }
            }
        });
    }

    private void useSDKforCachedPlaylist(final String listItemId)
    {
        final String methodName = "useSDKforCachedPlaylist";
        Log.i(APP_LOG_TAG, className + " - " + methodName + ": started " + listItemId);

        VideoProvider videoProvider = oneSDKinstance.getVideoProvider();
        videoProvider.requestPlaylistModel(listItemId, true, null, new VideoProvider.Callback()
        {
            @Override
            public void success(@NonNull VideoProviderResponse videoProviderResponse)
            {
                Log.i(APP_LOG_TAG, className + " - getCachedVideo requestPlaylistModel success");

                VideoProviderResponse.PlaylistItem[] playlistItems = videoProviderResponse.playlistItems;

                // Truncating list to 3 items
                int size = playlistItems.length <= 3 ? playlistItems.length : 3;

                ArrayList<VideoProviderResponse.PlaylistItem> al_cachedItems = new ArrayList<>();

                for (int i = 0; i < size; i++)
                {
                    VideoProviderResponse.PlaylistItem oldItem = playlistItems[i];

                    if(oldItem == null)
                    {
                        continue;
                    }

                    if (oldItem.video != null)
                    {
                        String fileName = "/" + aolCacheFolderName + "/" + oldItem.video.id + ".mp4";
                        File file = new File(DATA_PATH, fileName);

                        if (file.exists())
                        {
                            Log.i(APP_LOG_TAG, className + " - requestPlaylistModel found in cache: " + oldItem.video.id + ".mp4");
                            VideoProviderResponse.Video cachedVideo = oldItem.video.withUrl(file.getPath());
                            VideoProviderResponse.PlaylistItem newItem = new VideoProviderResponse.PlaylistItem(cachedVideo);
                            al_cachedItems.add(newItem);
                        }
                    }
                    else if(oldItem.voidVideo != null)
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": useSDKforCachedPlaylist - requestPlaylistModel: " + oldItem.voidVideo.reason);
                    }
                }

                if(al_cachedItems.size() == 0)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": useSDKforCachedPlaylist - requestPlaylistModel: al_cachedItems is empty");

                    if (aolPlayerStatesListener != null)
                    {
                        aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                    }
                    return;
                }

                VideoProviderResponse.PlaylistItem[] newItems = al_cachedItems.toArray(new VideoProviderResponse.PlaylistItem[al_cachedItems.size()]);
                VideoProviderResponse moodifiedResponse = videoProviderResponse.withPlaylistItems(newItems);

                try
                {
                    Player player = oneSDKinstance.createBuilder().buildFrom(moodifiedResponse);
                    Log.i(APP_LOG_TAG, className + " - useSDKforCachedPlaylist createBuilder().buildFrom(modifiedResponse) success ");

                    addAolPlayerStateObserver(player, methodName);

                    addAolPlayerErrorListener(player, methodName);

                    if (binder != null)
                    {
                        binder.setPlayer(player);
                    }
                } catch (EmptyPlaylistException | InvalidRendererException e)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - useSDKforCachedPlaylist: createBuilder().buildFrom error: " + e.getMessage());

                    if (aolPlayerStatesListener != null)
                    {
                        aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                    }
                }
            }

            @Override
            public void error(@NonNull Exception e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " - getCachedVideo requestPlaylistModel error: " + e.getMessage());

                if (aolPlayerStatesListener != null)
                {
                    aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                }
            }
        });
    }

    private void addAolPlayerErrorListener(@NonNull final Player player, final String methodName)
    {
        player.addErrorListener(new ErrorListener()
        {
            public void onError(@NonNull ErrorState errorState)
            {
                if (isAolLoadingOrPlaying)
                {
                    isAolLoadingOrPlaying = false;
                    aolPlayerEventsListener.onPlayError(methodName, errorState.toString());
                }
            }
        });
    }

    private void addAolPlayerStateObserver(@NonNull Player aolPlayer, final String methodName)
    {
        aolPlayer.addPlayerStateObserver(new PlayerStateObserver()
        {
            Boolean videoItemPlayStarted = false;
            Boolean adIsLoading = false;
            Boolean listItemIsLoading = false;

            public void onPlayerStateChanged(@NonNull Properties properties)
            {
                if (properties.ad.isLoading && !adIsLoading)
                {
                    adIsLoading = true;

                    aolPlayerEventsListener.onAdLoadingStarted();
                }

                if (!properties.ad.isLoading && adIsLoading)
                {
                    adIsLoading = false;

                    aolPlayerEventsListener.onAdLoadingFinished();
                }

                VideoProperties videoProperties = properties.playlistItem.video;

                if (videoProperties == null)
                {
                    aolPlayerEventsListener.onListItemNotPlayable(methodName);

                    if (!properties.playlist.hasNextVideo || properties.playlist.isLastVideo)
                    {
                        if (aolPlayerStatesListener != null)
                        {
                            aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
                        }
                    }

                    return;
                }

                if (videoProperties.isPlaying && !videoItemPlayStarted)
                {
                    videoItemPlayStarted = true;
                    isAolLoadingOrPlaying = true;

                    if(properties.playlist.isFirstVideo)
                    {
                        aolPlayerEventsListener.onPlayListPlayStarted(methodName, "");
                    }

                    aolPlayerEventsListener.onListItemPlayStarted(methodName, videoProperties.title);
                }

                if (videoProperties.isFinished && videoItemPlayStarted)
                {
                    videoItemPlayStarted = false;
                    isAolLoadingOrPlaying = false;

                    aolPlayerEventsListener.onListItemPlayFinished(methodName, videoProperties.title);

                    if ((!properties.playlist.hasNextVideo || properties.playlist.isLastVideo))
                    {
                        aolPlayerEventsListener.onPlayListCompleted(methodName);
                    }
                }

                if (videoProperties.isLoading && !listItemIsLoading)
                {
                    listItemIsLoading = true;
                    isAolLoadingOrPlaying = true;

                    aolPlayerEventsListener.onListItemLoadingStarted(methodName, videoProperties.title);
                }

                if (!videoProperties.isLoading && listItemIsLoading)
                {
                    listItemIsLoading = false;
                    isAolLoadingOrPlaying = true;

                    aolPlayerEventsListener.onListItemLoadingFinished(methodName, videoProperties.title);
                }
            }
        });
    }


    private void setAolPlayerEventsListener(AolPlayerEventsListener aolPlayerEventsListener)
    {
        this.aolPlayerEventsListener = aolPlayerEventsListener;
    }

    private void setAolPlayerStatesListener(AolPlayerStatesListener aolPlayerStatesListener)
    {
        this.aolPlayerStatesListener = aolPlayerStatesListener;
    }

    @Override
    public void onBuildForVideoError(String methodTitle, String errMsg)
    {
        Log.d(APP_LOG_TAG, className + " - " + methodTitle + ": onBuildForVideoError - " + errMsg);

        isAolLoadingOrPlaying = false;

        if (aolPlayerStatesListener != null)
        {
            aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
        }
    }

    @Override
    public void onPlayError(String methodTitle, String errMsg)
    {
        Log.d(APP_LOG_TAG, className + " - " + methodTitle + ": onPlayError - " + errMsg);

        if(errMsg.equals("CONNECTION_ERROR") || errMsg.equals("CONTENT_ERROR"))
        {
            if (aolPlayerStatesListener != null)
            {
                aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
            }
        }
    }

    public void onOneSDKregisterResult(OneSDK oneSDK, final PlayerView aolPlayerView, final ImageView imageView, @NonNull String playSequence, boolean isPlaylist, boolean loopList)
    {
        boolean isAolPlayerAvailable = oneSDK != null && binder != null && this.aolPlayerEventsListener != null;

        if (isAolPlayerAvailable)
        {
            loadListAndPlay(aolPlayerView, imageView, strIvBackgroundUrl, playSequence, isPlaylist, loopList);
        } else
        {
            Log.d(APP_LOG_TAG, className + ": AolPlayer is not available");
            if (aolPlayerStatesListener != null)
            {
                aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
            }
        }
    }

    @Override
    public void onPlayListCompleted(String methodTitle)
    {
        Log.i(APP_LOG_TAG, className + " - PlayList Completed");

        if (loopList)
        {
            replay();
        }
        else
        {
            //rewind();

            if (aolPlayerStatesListener != null)
            {
                aolPlayerStatesListener.onAolPlayerStateStopped(AolAdPlayer.this);
            }
        }
    }

    @Override
    public void onListItemNotPlayable(String useSDKforPlaylist)
    {
        Log.i(APP_LOG_TAG, className + ": List Item Not Playable, videoProperties is null");
    }

    @Override
    public void onAdPlayStarted()
    {
        Log.d(APP_LOG_TAG, className + ": Ad Play Started");
    }

    @Override
    public void onListItemPlayStarted(String methodTitle, String videoTitle)
    {
        Log.i(APP_LOG_TAG, className + " - " + methodTitle + ": List Item Play Started: " + videoTitle);
    }

    @Override
    public void onListItemPlayFinished(String methodTitle, String videoTitle)
    {
        Log.i(APP_LOG_TAG, className + " - " + methodTitle + ": List Item Play Finished: " + videoTitle);
    }

    @Override
    public void onPlayListPlayStarted(String methodTitle, String videoTitle)
    {
        Log.i(APP_LOG_TAG, className + " - " + methodTitle + ": PlayList Play Started " + videoTitle);

        if (aolPlayerStatesListener != null)
        {
            aolPlayerStatesListener.onAolPlayerStatsToSave(AolAdPlayer.this);
        }
    }

    @Override
    public void onAdLoadingStarted()
    {
        Log.d(APP_LOG_TAG, className + ": Ad Load Started");
        ViewUtils.setImageViewBackground(imageView, strIvBackgroundUrl);
    }

    @Override
    public void onAdLoadingFinished()
    {
        Log.d(APP_LOG_TAG, className + ": Ad Load Finished");
        ViewUtils.clearImageViewBackground(imageView);
    }

    @Override
    public void onListItemLoadingStarted(String methodTitle, String videoTitle)
    {
        Log.i(APP_LOG_TAG, className + " - " + methodTitle + ": List Item Load Started: " + videoTitle);
        ViewUtils.setImageViewBackground(imageView, strIvBackgroundUrl);
    }

    @Override
    public void onListItemLoadingFinished(String methodTitle, String videoTitle)
    {
        ViewUtils.clearImageViewBackground(imageView);
        Log.i(APP_LOG_TAG, className + " - " + methodTitle + ": List Item Load Finished: " + videoTitle);
    }

    public void pause()
    {
        Player player = binder.getPlayer();

        if (player != null)
        {
            player.pause();
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": pause - success");
        }
    }

    void stop()
    {
        Log.i(GlobalConstants.APP_LOG_TAG, className + ": player pause - failed, binder.onPause");
        binder.onPause();
    }

    void resume()
    {
        Player player = binder.getPlayer();

        if (player != null)
        {
            player.play();
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": resume - success");
        } else
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": resume - failed");
        }
    }

    private void replay()
    {
        Player player = binder.getPlayer();

        if (player != null)
        {
            player.replay();
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": replay - success");
        } else
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": replay - failed");
        }
    }

    private void rewind()
    {
        Player player = binder.getPlayer();

        if (player != null)
        {
            player.seekTo(0.1);
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": rewind - success");
        } else
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + ": rewind - failed");
        }
    }
}
