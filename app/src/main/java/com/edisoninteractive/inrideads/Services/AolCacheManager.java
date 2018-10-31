package com.edisoninteractive.inrideads.Services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.aol.mobile.sdk.player.OneSDK;
import com.aol.mobile.sdk.player.OneSDKBuilder;
import com.aol.mobile.sdk.player.VideoProvider;
import com.aol.mobile.sdk.player.VideoProviderResponse;
import com.aol.mobile.sdk.player.http.model.Environment;
import com.edisoninteractive.inrideads.Entities.AolCache;
import com.edisoninteractive.inrideads.Entities.Response;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.MyTextUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.edisoninteractive.inrideads.inRideAdsApp;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_AOL_CACHE_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CRITICAL_FREE_SPACE_ON_DEVICE;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

interface AolPlaylistIdsEventsListener
{
    void onOneSDKregisterResult(OneSDK oneSDK);
}

// For disabling direct caching (V.1) comment both calls to DownloadPlayListItems() method (row 308, 381) or ctrl+click on method header

public class AolCacheManager implements AolPlaylistIdsEventsListener
{
    private List <AolCache> idsAndUrls;
    private final List <Pair<String, String>> playlists;
    private List<Response> adsList;
    private Context context;
    private OneSDK oneSDKinstance;
    private final String className = getClass().getSimpleName();
    private AolPlaylistIdsEventsListener aolPlaylistIdsEventsListener;
    private Integer videoProviderCallbacks;
    private Integer totalCalls;
    private final String aolCacheFolderName = "aol_cache";

    public AolCacheManager(List<com.edisoninteractive.inrideads.Entities.Response> adsList)
    {
        context = inRideAdsApp.get();
        this.adsList = adsList;

        if (this.aolPlaylistIdsEventsListener == null)
        {
            setAolPlaylistIdsEventsListener(this);
        }

        idsAndUrls = new ArrayList<>();

        playlists = MyTextUtils.findAllPlaylistsInConfig_JS();

        if (playlists != null)
        {
            for (int i = 0; i < playlists.size(); i++)
            {
                String id = playlists.get(i).first;
                String playSequence = playlists.get(i).second;
                String[] strSplitRes = playSequence.split(",");

                List<String> playItems = new ArrayList<String>(Arrays.asList(strSplitRes));

                // Truncating list to 3 items
                if (playItems.size() > 3)
                {
                    List<String> subList = playItems.subList(0, 3);
                    strSplitRes = subList.toArray(new String[0]);

                    StringBuilder builder = new StringBuilder();
                    for (String s : strSplitRes)
                    {
                        builder.append(s);
                        builder.append(",");
                    }
                    String str = builder.toString();

                    playSequence = str.substring(0, str.length() - 1);
                    playlists.set(i, new Pair<String, String>(id, playSequence));
                }
            }

            Log.d(APP_LOG_TAG, className + " - found aol playlists: " + playlists.size());
        }
    }

    public void loadListAndFillArray()
    {
        if (oneSDKinstance == null)
        {
            initOneSDK_andPlay();
            return;
        }

        if (adsList == null || playlists == null)
        {
            return;
        }

        totalCalls = 0;
        videoProviderCallbacks = 0;

        for (int j = 0; j < adsList.size(); j++)
        {
            com.edisoninteractive.inrideads.Entities.Response ad = adsList.get(j);

            if (ad.adFile != null && ad.adFile.startsWith("aol:"))
            {
                String playSequence = ad.adFile.substring(4);

                try
                {
                    if (ad.isPlaylist)
                    {
                        Log.d(APP_LOG_TAG, className + " - " + " playList: aolItemPath = " + playSequence);
                        useSDKforCachedPlaylist(playSequence, null, ad.adId);
                    } else
                    {
                        Log.d(APP_LOG_TAG, className + " - " + " videoList: aolItemPath = " + playSequence);
                        String[] strSplitRes = playSequence.split(",");

                        useSDKforCachedVideolist(strSplitRes, null, ad.adId);
                    }
                    totalCalls++;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        for (int j = 0; j < playlists.size(); j++)
        {
            Long id = 0L;

            try
            {
                id = playlists.get(j).first != null ? Math.round(Double.valueOf(playlists.get(j).first)) : 0;
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
            String playSequence = playlists.get(j).second;

            if(playSequence == null)
            {
                continue;
            }

            try
            {
                if (!playSequence.contains(","))
                {
                    Log.d(APP_LOG_TAG, className + " - " + " playList: aolItemPath = " + playSequence);
                    useSDKforCachedPlaylist(playSequence, id, null);
                } else
                {
                    String[] strSplitRes = playSequence.split(",");
                    useSDKforCachedVideolist(strSplitRes, id, null);
                }
                totalCalls++;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void initOneSDK()
    {
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject("{\"preferMP4\":true}");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new OneSDKBuilder(context).setEnvironment(Environment.PRODUCTION).setExtra(jsonObject).create(new OneSDKBuilder.Callback()
        {
            public void onSuccess(@NonNull OneSDK oneSDK)
            {
                Log.d(APP_LOG_TAG, className + " - initOneSDK_andPlay: create new OneSDKBuilder success");

                oneSDKinstance = oneSDK;
            }

            public void onFailure(@NonNull Exception error)
            {
                Log.d(APP_LOG_TAG, className + " - " + ": create new OneSDKBuilder failure");
            }
        });
    }

    private void initOneSDK_andPlay()
    {
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject("{\"preferMP4\":true}");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new OneSDKBuilder(context).setEnvironment(Environment.PRODUCTION).setExtra(jsonObject).create(new OneSDKBuilder.Callback()
        {
            public void onSuccess(@NonNull OneSDK oneSDK)
            {
                Log.d(APP_LOG_TAG, className + " - initOneSDK_andPlay: create new OneSDKBuilder success");

                oneSDKinstance = oneSDK;
                aolPlaylistIdsEventsListener.onOneSDKregisterResult(oneSDK);
            }

            public void onFailure(@NonNull Exception error)
            {
                Log.d(APP_LOG_TAG, className + " - " + ": create new OneSDKBuilder failure");

                aolPlaylistIdsEventsListener.onOneSDKregisterResult(null);
            }
        });
    }

    private void useSDKforCachedVideolist(final String[] aolPlayList, final Long tabId, final Long adId)
    {
        final String methodName = "useSDKforCachedVideolist";
        Log.i(APP_LOG_TAG, className + " - " + methodName + ": started");

        VideoProvider videoProvider = oneSDKinstance.getVideoProvider();

        videoProvider.requestPlaylistModel(aolPlayList, true, null, new VideoProvider.Callback()
        {
            @Override
            public void success(@NonNull VideoProviderResponse videoProviderResponse)
            {
                Log.i(APP_LOG_TAG, className + " " + methodName + " -  requestPlaylistModel success");

                VideoProviderResponse.PlaylistItem[] playlistItems = videoProviderResponse.playlistItems;

                int size = playlistItems.length <= 3 ? playlistItems.length : 3;
                ArrayList<VideoProviderResponse.PlaylistItem> al_toDownloadItems = new ArrayList<>();

                for (int i = 0; i < size; i++)
                {
                    VideoProviderResponse.PlaylistItem oldItem = playlistItems[i];

                    if(oldItem == null)
                    {
                        continue;
                    }

                    if (oldItem.video != null && oldItem.video.url.contains(".mp4?"))
                    {
                        idsAndUrls.add(new AolCache(tabId, adId, oldItem.video.id, oldItem.video.url));

                        String fileName = "/" + aolCacheFolderName + "/" + oldItem.video.id + ".mp4";
                        File file = new File(DATA_PATH, fileName);

                        if (!file.exists())
                        {
                            al_toDownloadItems.add(oldItem);
                        }
                    }
                    else if(oldItem.voidVideo != null)
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": useSDKforCachedVideolist - requestPlaylistModel: " + oldItem.voidVideo.reason);
                    }
                }
                videoProviderCallbacks++;

                if(totalCalls.equals(videoProviderCallbacks))
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " " + methodName + " - " + ": idsAndUrls size = " + idsAndUrls.size());

                    deleteObsoleteCachedVideos();
                }

                if (al_toDownloadItems.size() != 0 && NetworkUtils.isNetworkAvailable(context))
                {
                    DownloadPlayListItems(al_toDownloadItems);
                }
            }

            @Override
            public void error(@NonNull Exception e)
            {
                videoProviderCallbacks++;

                if(totalCalls.equals(videoProviderCallbacks))
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " " + methodName + " - " + ": idsAndUrls size = " + idsAndUrls.size());
                }
                Log.i(APP_LOG_TAG, className + " " + methodName + " - requestPlaylistModel error: " + e.getMessage());
            }
        });
    }

    private void useSDKforCachedPlaylist(final String listItemId, final Long tabId, final Long adId)
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

                int size = playlistItems.length <= 3 ? playlistItems.length : 3;
                ArrayList<VideoProviderResponse.PlaylistItem> al_toDownloadItems = new ArrayList<>();

                for (int i = 0; i < size; i++)
                {
                    VideoProviderResponse.PlaylistItem oldItem = playlistItems[i];

                    if (oldItem == null)
                    {
                        continue;
                    }

                    if (oldItem.video != null && oldItem.video.url.contains(".mp4?"))
                    {
                        idsAndUrls.add(new AolCache(tabId, adId, oldItem.video.id, oldItem.video.url));

                        String fileName = "/" + aolCacheFolderName + "/" + oldItem.video.id + ".mp4";
                        File file = new File(DATA_PATH, fileName);

                        if (!file.exists())
                        {
                            al_toDownloadItems.add(oldItem);
                        }
                    }
                    else if(oldItem.voidVideo != null)
                    {
                        NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": useSDKforCachedPlaylist - requestPlaylistModel: " + oldItem.voidVideo.reason);
                    }
                }
                videoProviderCallbacks++;

                if (totalCalls.equals(videoProviderCallbacks))
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " " + methodName + " - " + ": idsAndUrls size = " + idsAndUrls.size());

                    deleteObsoleteCachedVideos();
                }

                if (al_toDownloadItems.size() != 0 && NetworkUtils.isNetworkAvailable(context))
                {
                    DownloadPlayListItems(al_toDownloadItems);
                }
            }

            @Override
            public void error(@NonNull Exception e)
            {
                videoProviderCallbacks++;

                if(totalCalls.equals(videoProviderCallbacks))
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " " + methodName + " - " + ": idsAndUrls size = " + idsAndUrls.size());
                }
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " - getCachedVideo requestPlaylistModel error: " + e.getMessage());
            }
        });
    }

    private void deleteObsoleteCachedVideos()
    {
        List <String> filePaths = new ArrayList<>();

        for (int i = 0; i < idsAndUrls.size(); i++)
        {
            String id = idsAndUrls.get(i).video_id;
            String filePath = DATA_PATH + "/aol_cache/" + id + ".mp4";
            filePaths.add(filePath);
        }

        Gson gson = new Gson();
        String strUrls = gson.toJson(idsAndUrls);

        NetworkUtils networkUtils = NetworkUtils.getInstance();
        networkUtils.uploadAolIdsAndUrls(context, API_AOL_CACHE_URL, strUrls);

        FileUtils.delFilesMissedInCurrentConfig(DATA_PATH + "/aol_cache", filePaths);
    }

    public void onOneSDKregisterResult(OneSDK oneSDK)
    {
        if (oneSDK != null)
        {
            loadListAndFillArray();
        } else
        {
            Log.d(APP_LOG_TAG, className + ": Aol oneSDK not available");
        }
    }

    private void setAolPlaylistIdsEventsListener(AolPlaylistIdsEventsListener aolPlaylistIdsEventsListener)
    {
        this.aolPlaylistIdsEventsListener = aolPlaylistIdsEventsListener;
    }

    private void DownloadPlayListItems(final ArrayList<VideoProviderResponse.PlaylistItem> al_NewItems)
    {
        Float freeMBytesAvailable = SystemUtils.getMBytesInExternalStorageAvailable();
        long folderSize = FileUtils.getFolderSize(new File(DATA_PATH + "/aol_cache"));

        if (freeMBytesAvailable < CRITICAL_FREE_SPACE_ON_DEVICE || folderSize > freeMBytesAvailable / 2)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - DownloadPlayListItems: Low disk space, downloading playlist item skipped");
            return;
        }

        for (int i = 0; i < al_NewItems.size(); i++)
        {
            final VideoProviderResponse.PlaylistItem finalNewItem = al_NewItems.get(i);

            String filePath = "/" + aolCacheFolderName + "/" + finalNewItem.video.id + ".mp4";

            Log.i(APP_LOG_TAG, className + ": file " + finalNewItem.video.id + ".mp4 added in queue for downloading in background");

            Intent intent = new Intent(context, DownloadFileService.class);
            intent.setAction("com.edisoninteractive.inrideads.Services.action.DownloadFile");
            intent.putExtra("com.edisoninteractive.inrideads.Services.extra.Url", finalNewItem.video.url);
            intent.putExtra("com.edisoninteractive.inrideads.Services.extra.FilePath", filePath);
            context.startService(intent);
        }
    }
}
