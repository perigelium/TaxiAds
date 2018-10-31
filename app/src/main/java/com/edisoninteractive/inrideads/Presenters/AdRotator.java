package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.aol.mobile.sdk.player.view.PlayerView;
import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.BuildConfig;
import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.InterfaceLayout;
import com.edisoninteractive.inrideads.Entities.LocationTrackPoint;
import com.edisoninteractive.inrideads.Entities.Macrocommands;
import com.edisoninteractive.inrideads.Entities.Params;
import com.edisoninteractive.inrideads.Entities.Response;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.EventHandlers.StatsManager;
import com.edisoninteractive.inrideads.EventHandlers.UserEvents;
import com.edisoninteractive.inrideads.Interfaces.AolPlayerStatesListener;
import com.edisoninteractive.inrideads.Interfaces.CustomEventListener;
import com.edisoninteractive.inrideads.Interfaces.SensorEventsListener;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.MyTextUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.ViewUtils;
import com.edisoninteractive.inrideads.ViewOverrides.MyContentControlsView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.AD_MOB_TIMESTAMP_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.HALF_EQUATOR_LENGTH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.INTERSTITIAL_AD_DURATION;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.INTERSTITIAL_AD_PERIOD_BASE;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.INTERSTITIAL_AD_PERIOD_DEVIATION;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.MIN_IMAGE_PLAY_DURATION;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_PLAYBACK_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_STOPPED_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_SHOW_INTERACTIVE;
import static com.edisoninteractive.inrideads.MainActivity.aolAdPlayers;

/**
 * Created by Alex Angan one fine day
 */

public class AdRotator extends InteractiveBlock implements CustomEventListener, SensorEventsListener, AolPlayerStatesListener
{
    private InterfaceLayout interfaceLayout;
    private Params params;
    private List<Response> adsList;

    private long lastUserTapTimeStamp;
    private long lastFaceDetectedTimeStamp;
    private double humanPresenceThreshold;

    private List<Pair<Integer, Long>> facesQuantAndTimestamps;
    private int lastFacesCount;
    private PlayerView aolPlayerView;
    private VideoView videoView;
    private Boolean isVideoViewPlaying;
    private ImageView imageView;
    private TreeMap<Long, Response> adsTree;
    private ArrayList<Response> adsOrderedList;

    private int curAdIndex;
    private int adTypeId;
    private Boolean imageViewIsPlaying;
    AolAdPlayer aolAdPlayer;
    private CountDownTimer playImageTimer;
    private InteractiveView interactiveView;
    private final String className = AdRotator.class.getSimpleName();
    private boolean faceDetectionEnabled;
    private List<Command> l_ShowInteractiveCommands;
    private Activity activity;
    private long unixTimePrevCycle;
    private CountDownTimer playDefaultImage;
    private int videoViewPosition;

    //private Interstitial interstitial;

    public AdRotator(String id, FrameLayout frameLayout, EventManager eventsManager, InterfaceLayout interfaceLayout, List<Response> adsList)
    {
        super(id, frameLayout, eventsManager);
        this.interfaceLayout = interfaceLayout;
        this.adsList = adsList;
    }

    public void init(Activity activity, Params params, InteractiveView interactiveView)
    {
        this.params = params;
        this.activity = activity;
        this.interactiveView = interactiveView;

        aolPlayerView = new PlayerView(activity);
        videoView = new VideoView(activity);
        imageView = new ImageView(activity);

        final RelativeLayout relativeLayout = new RelativeLayout(activity);

        RelativeLayout.LayoutParams lprReal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        relativeLayout.setLayoutParams(lprReal);

        lprReal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        aolPlayerView.setLayoutParams(lprReal);

        // Enable/disable displaying of certain view controls
        MyContentControlsView myContentControlsView = new MyContentControlsView(activity);
        aolPlayerView.setContentControls(myContentControlsView);

        videoView.setLayoutParams(lprReal);
        imageView.setLayoutParams(lprReal);

        relativeLayout.addView(videoView); // bottom layer
        relativeLayout.addView(aolPlayerView); // middle layer
        relativeLayout.addView(imageView); // top layer

        frameLayout.addView(relativeLayout);

        prepareAds();
    }

    private void prepareAds()
    {
        adTypeId = (int) interfaceLayout.adTypeId;

        aolAdPlayer = aolAdPlayers.get(String.valueOf(adTypeId));

        if (params.onShowInteractive != null)
        {
            l_ShowInteractiveCommands = params.onShowInteractive.commands;
        }

        faceDetectionEnabled = params.faceDetectionEnabled;
        humanPresenceThreshold = params.humanPresenceThreshold * 1000;
        facesQuantAndTimestamps = new ArrayList<>();
        lastFacesCount = 0;
        imageViewIsPlaying = false;
        isVideoViewPlaying = false;
        curAdIndex = -1;

        adsTree = new TreeMap<>();
        adsOrderedList = new ArrayList<>();
        putAdsInOrderedList(adTypeId);

        facesQuantAndTimestamps = new ArrayList<>();

        frameLayout.setOnTouchListener(new OnTouchDebouncedListener()
        {
            @Override
            public boolean onTouched(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    // Save touch statistics
                    try
                    {
                        if (curAdIndex != -1 && adsOrderedList.size() != 0)
                        {
                            Response currentAdObject = adsOrderedList.get(curAdIndex);

                            StatsManager.getInstance(aolPlayerView.getContext()).writeStats(StatsManager.AD_ROTATOR_PRESS, String.valueOf(currentAdObject.adId), String.valueOf(currentAdObject.campaignId), "null");
                        }

                    } catch (Exception writeTouchStatsExc)
                    {
                        if (!BuildConfig.DEBUG)
                        {
                            Crashlytics.logException(writeTouchStatsExc);
                        }
                        Log.w(GlobalConstants.APP_LOG_TAG, "Failed to log AD_ROTATOR_PRESS (8) stats");
                    }

                    //reset user activity timer
                    UserEvents.getInstance().startLastUserActivityCntDownTimer();
                }

                return false;
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if (curAdIndex != -1 && adsOrderedList.size() != 0)
                    {
                        Response currentAdObject = adsOrderedList.get(curAdIndex);

                        StatsManager.getInstance(aolPlayerView.getContext()).writeStats(StatsManager.AD_ROTATOR_PRESS, String.valueOf(currentAdObject.adId), String.valueOf(currentAdObject.campaignId), "null");

                        if (currentAdObject.interactFile != null)
                        {
                            eventsManager.notify(ON_SHOW_INTERACTIVE, l_ShowInteractiveCommands);
                            Log.i(APP_LOG_TAG, className + " - " + adTypeId + ": onTouch");

                            if (interactiveView != null && currentAdObject.interactiveId != null)
                            {
                                pauseAllPlayers();
                                interactiveView.play(currentAdObject);
                            }
                        }
                    }

                    //reset user activity timer
                    UserEvents.getInstance().startLastUserActivityCntDownTimer();
                }

                return false;
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra)
            {
                String strExtra = String.format("Error(%s)", extra);
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " - " + adTypeId + ": MediaPlayer error - " + strExtra + " , this video cannot be played");

                isVideoViewPlaying = false;

                nextListOrItem();

                return true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                Log.d(APP_LOG_TAG, className + " - " + adTypeId + ": MediaPlayer item play completed");

                if (curAdIndex != -1)
                {
                    Response curAd = adsOrderedList.get(curAdIndex);

                    if (curAd.geoBinding != null)
                    {
                        long unixTimeNow = System.currentTimeMillis();
                        updateNextTimePlayAllowed(unixTimeNow);
                    }
                }

                videoView.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoView.stopPlayback();
                isVideoViewPlaying = false;

                nextListOrItem();
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                Log.d(APP_LOG_TAG, className + " - " + adTypeId + ": MediaPlayer item play starting");

                if (curAdIndex != -1)
                {
                    Response curAd = adsOrderedList.get(curAdIndex);
                    saveStatsToDb(curAd.adId, curAd.campaignId);
                }

                aolPlayerView.setVisibility(View.INVISIBLE);
                ViewUtils.clearImageViewBackground(imageView);

                videoView.start();
                isVideoViewPlaying = true;
            }
        });

/*        if(BuildConfig.ENABLE_AD_MOB)
        {
            interstitial = Interstitial.getInstance();
            interstitial.init(activity);
        }*/
    }

    private void rotateAds()
    {
        playDefaultImageAndGoNext();
    }

    private void nextListOrItem()
    {
        Log.d(APP_LOG_TAG, className + " - " + adTypeId + ": nextListOrItem");
        long unixTimeNow = System.currentTimeMillis();

/*        if(BuildConfig.ENABLE_AD_MOB)
        {
            interstitial.tryToShowAdd();
        }*/

        if (adsOrderedList.size() == 0)
        {
            Log.i(APP_LOG_TAG, className + " - " + adTypeId + ": nextListOrItem -> return, adsOrderedList is empty");

            if (adTypeId == 2)
            {
                playDefaultImageAndGoNext();
            }
            return;
        }

        rotateOrderedListIndex();

        Response curAd = adsOrderedList.get(curAdIndex);

        if (curAd.adFile == null)
        {
            Log.i(APP_LOG_TAG, className + " - " + adTypeId + ": nextListOrItem -> return, adFile path is empty");

            if (adTypeId == 2)
            {
                playDefaultImageAndGoNext();
            }
            return;
        }

        if (!isInDateTimeBounds(curAd))
        {
            if (adTypeId == 2)
            {
                playDefaultImageAndGoNext();
            }
            return;
        }

        if (adTypeId == 2 && curAdIndex == 0 && unixTimePrevCycle != 0 && (unixTimeNow - unixTimePrevCycle < (MIN_IMAGE_PLAY_DURATION * 1000)))
        {
            unixTimePrevCycle = unixTimeNow;
            playDefaultImageAndGoNext();
            return;
        }

        // skip .flv files, there's no player to play them
        if (FileUtils.isFileOfType(curAd.adFile, ".flv") && adsOrderedList.size() > 1)
        {
            playDefaultImageAndGoNext();
            return;
        }

        boolean playForFace = faceDetectionEnabled && facePresentOrUserTapped();

        Response geoAd = isThereAtLeastOneGeoAdNearCurrentLocation();

        if (geoAd != null && (geoAd.nextTimePlayAllowed < unixTimeNow || geoAd.nextTimePlayAllowed == 0))
        {
            curAd = geoAd;
            curAdIndex = adsOrderedList.indexOf(geoAd);
        } else
        {
            curAd = getNextAdForFaceOrNobody(curAd, playForFace);

            if (curAd == null)
            {
                playDefaultImageAndGoNext();
                return;
            }
        }

        int duration = (int) curAd.duration;

        if (duration < MIN_IMAGE_PLAY_DURATION)
        {
            duration = (int) MIN_IMAGE_PLAY_DURATION;
        }

        playSelectedItem(curAd, duration);
    }

    private void writeTimestampAsJsonFile(String entryName, long unixTimeNow)
    {
        long nextTimeInterstitialAdPlayAllowed;
        Random rnd = new Random();

        int adShift = rnd.nextInt(INTERSTITIAL_AD_PERIOD_DEVIATION);
        int randomizedAdPeriod = INTERSTITIAL_AD_PERIOD_BASE + adShift;

        nextTimeInterstitialAdPlayAllowed = unixTimeNow + randomizedAdPeriod + INTERSTITIAL_AD_DURATION;

        String strJson = "{\"" + entryName + "\":\"" + String.valueOf(nextTimeInterstitialAdPlayAllowed) + "\"}";

        FileUtils.writeStringToFile(AD_MOB_TIMESTAMP_PATH, strJson);
    }

    private void playSelectedItem(Response curAd, int duration)
    {
        if (!curAd.adFile.startsWith("aol:"))
        {
            if (aolAdPlayer.isAolLoadingOrPlaying)
            {
                Log.d(APP_LOG_TAG, className + " - " + adTypeId + ": aolPlayer - Pause called");
                aolAdPlayer.pause();
            }

            selectAndPlayLocalFile(DATA_PATH + curAd.adFile, duration);

        } else if (NetworkUtils.isNetworkAvailable(activity))
        {
            String strPlaylist = curAd.adFile.substring(4); // removes list prefix (aol:)

            if (!strPlaylist.isEmpty())
            {
                ViewUtils.clearImageViewBackground(imageView);
                aolPlayerView.setVisibility(View.VISIBLE);

                aolAdPlayer.loadListAndPlay(aolPlayerView, imageView, null, strPlaylist, curAd.isPlaylist, false);
            }
        } else
        {
            playDefaultImageAndGoNext();
        }
    }

    private void playDefaultImageAndGoNext()
    {
        if (adTypeId != 2)
        {
            nextListOrItem();
            return;
        }

        Log.i(APP_LOG_TAG, className + " - " + adTypeId + ": playDefaultImageAndGoNext()");

        imageView.setVisibility(View.VISIBLE);
        imageView.setBackgroundResource(R.drawable.edison_interactive);
        imageViewIsPlaying = true;

        if (playDefaultImage != null)
        {
            playDefaultImage.cancel();
        }

        playDefaultImage = new CountDownTimer(MIN_IMAGE_PLAY_DURATION * 1000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                ViewUtils.clearImageViewBackground(imageView);

                imageViewIsPlaying = false;
                this.cancel();

                nextListOrItem();
            }
        }.start();
    }

    private void updateNextTimePlayAllowed(long unixTimeNow)
    {
        Response prevAd;

        if (curAdIndex != -1)
        {
            prevAd = adsOrderedList.get(curAdIndex);

            if (prevAd.geoBinding != null)
            {
                if (prevAd.geoBinding.repeatInterval != 0)
                {
                    prevAd.setNextTimePlayAllowed(unixTimeNow + prevAd.geoBinding.repeatInterval * 1000);
                }
            }
        }
    }

    private void rotateOrderedListIndex()
    {
        curAdIndex++;

        if (curAdIndex == adsOrderedList.size())
        {
            curAdIndex = 0;
        }
    }

    private Response getNextAdForFaceOrNobody(Response ad, boolean playForFace)
    {
        if (playForFace)
        {
            int i = 0;
            // seek first suitable for face item
            for (i = 0; i < adsOrderedList.size(); i++)
            {
                if ((ad.forFace != null && !ad.forFace) || ad.geoBinding != null)
                {
                    rotateOrderedListIndex();

                    ad = adsOrderedList.get(curAdIndex);
                } else
                {
                    break;
                }
            }

            if (i == adsOrderedList.size())
            {
                ad = null;
            }
        } else
        {
            int i = 0;
            // seek first suitable for nobody item
            for (i = 0; i < adsOrderedList.size(); i++)
            {
                if ((ad.forFace != null && ad.forFace) || ad.geoBinding != null)
                {
                    rotateOrderedListIndex();

                    ad = adsOrderedList.get(curAdIndex);
                } else
                {
                    break;
                }
            }

            if (i == adsOrderedList.size())
            {
                ad = null;
            }
        }
        return ad;
    }

    private void selectAndPlayLocalFile(String itemPath, int duration)
    {
        Log.d(APP_LOG_TAG, className + " - " + adTypeId + ": selectAndPlayLocalFile: " + itemPath);

        boolean isImageFile = FileUtils.hasImageExtension(itemPath);

        if (isImageFile && duration != 0)
        {
            playImage(itemPath, duration);
        } else
        {
            playLocalVideo(itemPath);
        }
    }

    public void playLocalVideo(String itemPath)
    {
        videoView.setVideoPath(itemPath); // start playing
        Log.d(APP_LOG_TAG, className + " - " + adTypeId + ": videoView - start");
    }

    private Response isThereAtLeastOneGeoAdNearCurrentLocation()
    {
        if (LocationTrackPoint.mlastLocation == null)
        {
            return null;
        }

        MyTextUtils textUtils = MyTextUtils.getInstance();
        Map<Double, Response> geoPlayList = new TreeMap<>();

        for (int i = 0; i < adsOrderedList.size(); i++)
        {
            Response ad = adsOrderedList.get(i);

            if (ad.geoBinding != null)
            {
                List<com.edisoninteractive.inrideads.Entities.Location> locations = ad.geoBinding.locations;

                double minRadius = HALF_EQUATOR_LENGTH;

                for (int j = 0; j < locations.size(); j++)
                {
                    String strLocations = locations.get(j).location;
                    int radius = (int) locations.get(j).radius;
                    String[] parts = strLocations.split(",");

                    if (parts.length < 2 || parts[0].isEmpty() || parts[1].isEmpty())
                    {
                        continue;
                    }

                    double itemLatitude = Double.valueOf(parts[0]);
                    double itemLongitude = Double.valueOf(parts[1]);

                    double curLatitude = LocationTrackPoint.mlastLocation.getLatitude();
                    double curLongitude = LocationTrackPoint.mlastLocation.getLongitude();

                    int distance = (int) textUtils.getFlatEarthDistance(itemLatitude, itemLongitude, curLatitude, curLongitude);

                    if (distance > radius)
                    {
                        continue;
                    }

                    if (minRadius > radius)
                    {
                        minRadius = radius;
                    }
                }

                while (geoPlayList.get(minRadius) != null)
                {
                    minRadius++;
                }

                if (minRadius != HALF_EQUATOR_LENGTH)
                {
                    geoPlayList.put(minRadius, ad);
                }
            }
        }

        List<Response> ads = new ArrayList<>();
        ads.addAll(geoPlayList.values());

        Log.d(APP_LOG_TAG, className + ":isThereAtLeastOneGeoAdNearCurrentLocation geoPlayList size: " + String.valueOf(geoPlayList.size()));

        return ads.size() != 0 ? ads.get(0) : null;
    }

    private boolean facePresentOrUserTapped() // is user tapped or viewed screen before humanPresenceThreshold time expired ?
    {
        if (lastFaceDetectedTimeStamp == 0 && lastUserTapTimeStamp == 0)
        {
            return false;
        }

        long unixTimeNow = System.currentTimeMillis();

        long facePresentTimeDelta = unixTimeNow - lastFaceDetectedTimeStamp;
        long tappedTimeDelta = unixTimeNow - lastUserTapTimeStamp;

        boolean facePresent = facePresentTimeDelta < humanPresenceThreshold;
        boolean tapped = tappedTimeDelta < humanPresenceThreshold;

        return facePresent || tapped;
    }

    private void playImage(String strPathToFile, int duration)
    {
        Drawable d = Drawable.createFromPath(strPathToFile);

        if (d != null)
        {
            imageView.setBackground(d);
            imageViewIsPlaying = true;

            if (curAdIndex != -1 && duration > MIN_IMAGE_PLAY_DURATION)
            {
                Response curAd = adsOrderedList.get(curAdIndex);
                saveStatsToDb(curAd.adId, curAd.campaignId);
            }
        }

        if (playImageTimer != null)
        {
            playImageTimer.cancel();
        }

        playImageTimer = new CountDownTimer(duration * 1000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                ViewUtils.clearImageViewBackground(imageView);

                imageViewIsPlaying = false;
                nextListOrItem();
            }
        }.start();
    }

    private void putAdsInOrderedList(int adTypeId) // arrange ads according to ad.adOrder values
    {
        for (int j = 0; j < adsList.size(); j++)
        {
            Response ad = adsList.get(j);

            if (ad.adType == adTypeId)
            {
                if (!(ad.adFile.startsWith("aol:")))
                {
                    if (!FileUtils.hasImageExtension(ad.adFile) && !FileUtils.hasVideoExtension(ad.adFile))
                    {
                        continue;
                    }

                    String strFilePath = DATA_PATH + ad.adFile;
                    File file = new File(strFilePath);

                    if (!file.exists())
                    {
                        continue;
                    }

                    while (adsTree.get(ad.adOrder) != null)
                    {
                        ad.adOrder++;
                    }

                    adsTree.put(ad.adOrder, ad);
                } else
                {
                    while (adsTree.get(ad.adOrder) != null)
                    {
                        ad.adOrder++;
                    }

                    adsTree.put(ad.adOrder, ad);
                }
            }
        }

        for (Map.Entry entry : adsTree.entrySet())
        {
            Response ad = (Response) entry.getValue();
            {
                adsOrderedList.add(ad);
            }
        }

        Log.i(APP_LOG_TAG, className + " - " + adTypeId + ": Found " + String.valueOf(adsOrderedList.size()) + " playable ads");
    }

    private boolean isInDateTimeBounds(Response ad)
    {
        if (ad.dateFrom == null || ad.dateTo == null)
        {
            return true;
        }

        MyTextUtils myTextUtils = MyTextUtils.getInstance();
        long dateFromUnixTime = myTextUtils.dateStringToUnixTime(ad.dateFrom);
        long dateToUnixTime = myTextUtils.dateStringToUnixTime(ad.dateTo);

        long timeFromUnixTime = myTextUtils.timeStringToUnixTime(ad.timeFrom);
        long timeToUnixTime = myTextUtils.timeStringToUnixTime(ad.timeTo);

        long unixTimeNow = System.currentTimeMillis();

        boolean isInDateTimeBounds = dateFromUnixTime < unixTimeNow && unixTimeNow < dateToUnixTime && (timeFromUnixTime == timeToUnixTime || (timeFromUnixTime < unixTimeNow && unixTimeNow < timeToUnixTime));

        return isInDateTimeBounds;
    }

    @Override
    public void onFaceDetected(int facesCount)
    {
        if (facesCount != lastFacesCount)
        {
            lastFaceDetectedTimeStamp = System.currentTimeMillis();
            facesQuantAndTimestamps.add(new Pair<Integer, Long>(facesCount, lastFaceDetectedTimeStamp));

            lastFacesCount = facesCount;
        }
    }

    @Override
    public void onFaceMissing(int facesCount)
    {
        if (facesCount != lastFacesCount)
        {
            long unixTimeStamp = System.currentTimeMillis();
            facesQuantAndTimestamps.add(new Pair<Integer, Long>(facesCount, unixTimeStamp));

            if (facesCount > 0)
            {
                lastUserTapTimeStamp = unixTimeStamp;
            }

            lastFacesCount = facesCount;
        }
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        switch (commandWithParams.strCommand)
        {
            case Macrocommands.HIDE_BLOCK:
                pauseAllPlayers();
                videoView.setVisibility(View.INVISIBLE);
                frameLayout.setVisibility(View.INVISIBLE);
                break;

            case Macrocommands.SHOW_BLOCK:
                frameLayout.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoView.stopPlayback();

                if (adTypeId == 2)
                {
                    UserEvents.getInstance().cancelUserLastActivitycountDownTimer();
                }

                resumeCurrentPlayerOrPlayNextItem();
                break;

            case GOTO_STOPPED_MODE:
                pauseAllPlayers();
                break;

            case GOTO_PLAYBACK_MODE:
                if (frameLayout.getVisibility() != View.VISIBLE)
                {
                    frameLayout.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.stopPlayback();
                }
                rotateAds();
                break;

            case ON_SHOW_INTERACTIVE:
                pauseAllPlayers();
                break;
        }
    }

    private void resumeCurrentPlayerOrPlayNextItem()
    {
        Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ": resumeCurrentPlayerOrPlayNextItem()");

        if (adsOrderedList.size() == 0)
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ": resumeCurrentPlayerOrPlayNextItem -> return, adsOrderedList is empty");

            if (adTypeId == 2)
            {
                playDefaultImageAndGoNext();
            }
            return;
        }

        if (aolAdPlayer.isAolLoadingOrPlaying)
        {
            aolPlayerView.setVisibility(View.VISIBLE);
            aolAdPlayer.resume();
            Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ": aolAdPlayer - Resume called");
        } else if (isVideoViewPlaying && !videoView.isPlaying()) // && !mediaPlayer.isPlaying()
        {
            videoView.seekTo(videoViewPosition);
            videoView.resume();
        } else
        {
            nextListOrItem();
        }
    }

    private void saveStatsToDb(long adId, long campaignId)
    {
        try
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ":  saveStatsToDb() called");
            StatsManager.getInstance(aolPlayerView.getContext()).writeStats(StatsManager.AD_ROTATOR, String.valueOf(adId), String.valueOf(campaignId), "null");
        } catch (Exception ex)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " - " + adTypeId + ": saveStatsToDb() Exception : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void pauseAllPlayers()
    {
        Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ": pauseAllPlayers");

        if (videoView.isPlaying())
        {
            videoViewPosition = videoView.getCurrentPosition();

            videoView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.stopPlayback();

            Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ": videoView - Pause called");
        }

        if (aolAdPlayer.isAolLoadingOrPlaying)
        {
            Log.i(APP_LOG_TAG, className + " - " + adTypeId + ": aolPlayer - Pause called");
            aolAdPlayer.pause();
        }

        if (playImageTimer != null)
        {
            playImageTimer.cancel();
            ViewUtils.clearImageViewBackground(imageView);
        }

        if (playDefaultImage != null)
        {
            playDefaultImage.cancel();
        }
    }

    @Override
    public void onAolPlayerStateStopped(AolAdPlayer aolPlayer) // callback from aolAdPlayer
    {
        if (aolAdPlayer.equals(aolPlayer))
        {
            Log.i(GlobalConstants.APP_LOG_TAG, className + " - " + adTypeId + ": onAolPlayerStateStopped");
            playDefaultImageAndGoNext();
        }
    }

    @Override
    public void onAolPlayerStatsToSave(AolAdPlayer aolPlayer)
    {
        if (aolAdPlayer.equals(aolPlayer) && curAdIndex != -1)
        {
            Response curAd = adsOrderedList.get(curAdIndex);
            saveStatsToDb(curAd.adId, curAd.campaignId);
        }
    }
}
