package com.edisoninteractive.inrideads.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.edisoninteractive.inrideads.BuildConfig;
import com.edisoninteractive.inrideads.Entities.Child;
import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.Config_JS;
import com.edisoninteractive.inrideads.Entities.InterfaceLayout;
import com.edisoninteractive.inrideads.Entities.Response;
import com.edisoninteractive.inrideads.EventHandlers.CameraFaceDetector;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.EventHandlers.SystemCommandManager;
import com.edisoninteractive.inrideads.EventHandlers.UserEvents;
import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.Interfaces.CustomEventListener;
import com.edisoninteractive.inrideads.Interfaces.FaceTrackingListener;
import com.edisoninteractive.inrideads.Interfaces.SensorEventsListener;
import com.edisoninteractive.inrideads.Presenters.AdRotator;
import com.edisoninteractive.inrideads.Presenters.AolAdPlayer;
import com.edisoninteractive.inrideads.Presenters.Banner;
import com.edisoninteractive.inrideads.Presenters.ChannelContainter;
import com.edisoninteractive.inrideads.Presenters.InteractiveBlock;
import com.edisoninteractive.inrideads.Presenters.InteractiveFrame;
import com.edisoninteractive.inrideads.Presenters.InteractiveView;
import com.edisoninteractive.inrideads.Presenters.SingleLayerChildButton;
import com.edisoninteractive.inrideads.Presenters.TwoLayersChildButton;
import com.edisoninteractive.inrideads.Presenters.WholeLayoutButton;
import com.edisoninteractive.inrideads.Presenters.WholeLayoutToggleButton;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Services.AolCacheManager;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.LONG_PRESS_TIMEOUT;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_PLAYBACK_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_STOPPED_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SET_SCREEN_BRIGHTNESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.TAPPED;
import static com.edisoninteractive.inrideads.MainActivity.aolAdPlayers;

/**
 * Created by Alex Angan one fine day
 */

public class FragDisplayBlocks extends Fragment implements FaceTrackingListener, CustomEventListener
{
    Activity activity;
    private FrameLayout flAdsContainer;
    Map<Long, ArrayList<InterfaceLayout>> layoutsTree;

    private TextView tvFacesStatus;

    private List<AdRotator> adRotators;
    private SensorEventsListener sensorEventsListener;
    View rootView;
    private CameraFaceDetector cameraFaceDetector;
    private Communicator mCommunicator;
    public EventManager eventsManager;
    List<Response> adsList;
    List<InteractiveBlock> objectsToCleanUp;
    InteractiveView interactiveView;
    private final String className = FragDisplayBlocks.class.getSimpleName();
    private int lastFacesCount;
    private Config_JS config_js;
    private View vAdminMenu;

    public FragDisplayBlocks()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        mCommunicator = (Communicator) activity;
        eventsManager = EventManager.getInstance();
        lastFacesCount = 0;

        adRotators = new ArrayList<>();
        objectsToCleanUp = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.frag_display_blocks, container, false);

        flAdsContainer = (FrameLayout) rootView.findViewById(R.id.flAdsContainer);
        tvFacesStatus = (TextView) rootView.findViewById(R.id.tvFacesStatus);

        if (BuildConfig.DEBUG && tvFacesStatus != null)
        {
            tvFacesStatus.setText("Nobody");
        }

        vAdminMenu = (View) rootView.findViewById(R.id.vAdminMenu);
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                mCommunicator.replaceFragment(FragLogin.class.getSimpleName(), new FragLogin());
            }
        };

        vAdminMenu.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    handler.postDelayed(runnable, LONG_PRESS_TIMEOUT);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    long eventDuration = motionEvent.getEventTime() - motionEvent.getDownTime();

                    if (eventDuration < LONG_PRESS_TIMEOUT)
                    {
                        handler.removeCallbacks(runnable);
                    }
                }
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        config_js = FileUtils.readMediaInterfaceConfigObject();

        EventManager eventManager = EventManager.getInstance();
        eventManager.notify(GOTO_PLAYBACK_MODE, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        config_js = FileUtils.readMediaInterfaceConfigObject();
        adsList = FileUtils.reloadAdsList(null);

        if (NetworkUtils.isNetworkAvailable(activity))
        {
            FileUtils.deleteFilesOfTypeInFolder(DATA_PATH + "/aol_cache", ".part");
            AolCacheManager aolCacheManager = new AolCacheManager(adsList);
            aolCacheManager.loadListAndFillArray();
        }

        if (config_js != null && adsList != null)
        {
            if (config_js.params != null && config_js.params.idleInterval != 0)
            {
                UserEvents.getInstance().init(config_js.params);

                flAdsContainer.setOnTouchListener(new OnTouchDebouncedListener()
                {
                    @Override
                    public boolean onTouched(View v, MotionEvent event)
                    {
                        if (event.getAction() == MotionEvent.ACTION_DOWN)
                        {
                            UserEvents.getInstance().startLastUserActivityCntDownTimer();
                        }

                        return false;
                    }
                });
            }

            putLayoutsToZorderedTree();

            displayBlocks();

            vAdminMenu.bringToFront();

            if (BuildConfig.DEBUG && tvFacesStatus != null)
            {
                tvFacesStatus.bringToFront();
            }

            if (config_js.params.faceDetectionEnabled)
            {
                cameraFaceDetector = new CameraFaceDetector(this, activity, rootView);
            } else
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, " params.faceDetectionEnabled is false");
            }

            for (int i = 0; i < adRotators.size(); i++)
            {
                AdRotator adRotator = adRotators.get(i);

                adRotator.init(activity, config_js.params, interactiveView);

                setSensorEventsListener(adRotator);
                eventsManager.subscribe(adRotator);
            }
        }
    }

    private void putLayoutsToZorderedTree()
    {
        layoutsTree = new TreeMap<>();

        for (int i = 0; i < config_js.interfaceLayout.size(); i++)
        {
            InterfaceLayout interfaceLayout = config_js.interfaceLayout.get(i);
            long depthLevel = interfaceLayout.depthLevel;

            ArrayList<InterfaceLayout> al = layoutsTree.get(depthLevel);

            if (al == null)
            {
                al = new ArrayList<>();
            }

            al.add(interfaceLayout);
            layoutsTree.put(depthLevel, al);
        }
    }

    private void displayBlocks()
    {
        Pair<Double, Double> screenRatios = ViewUtils.getScreenRatios(activity, config_js.params.interfaceWidth, config_js.params.interfaceHeight);

        if (screenRatios == null)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, " Unable to get screen dimensions !");
            return;
        }

        double widthRatio = screenRatios.first;
        double heightRatio = screenRatios.second;

        config_js.params.widthRatio = widthRatio;
        config_js.params.heightRatio = heightRatio;

        for (Map.Entry entry : layoutsTree.entrySet())
        {
            List<InterfaceLayout> al_InterfaceLayouts = (List<InterfaceLayout>) entry.getValue();

            for (int i = 0; i < al_InterfaceLayouts.size(); i++)
            {
                final InterfaceLayout interfaceLayoutConfig = al_InterfaceLayouts.get(i);

                final FrameLayout frameLayout = new FrameLayout(activity);

                int width = (int) (interfaceLayoutConfig.width * widthRatio);
                int height = (int) (interfaceLayoutConfig.height * heightRatio);

                int leftTopX = (int) (Integer.parseInt(interfaceLayoutConfig.getX()) * widthRatio);
                int leftTopY = (int) (Integer.parseInt(interfaceLayoutConfig.getY()) * heightRatio);

                frameLayout.setX(leftTopX);
                frameLayout.setY(leftTopY);

                FrameLayout.LayoutParams lpReal = new FrameLayout.LayoutParams(width, height);
                frameLayout.setLayoutParams(lpReal);

                if (interfaceLayoutConfig.backgroundImage != null)
                {
                    Drawable d = Drawable.createFromPath(DATA_PATH + interfaceLayoutConfig.backgroundImage);

                    if (d != null)
                    {
                        frameLayout.setBackground(d);
                    }
                } else
                {
                    if (interfaceLayoutConfig.backgroundColor != null && !interfaceLayoutConfig.backgroundColor.isEmpty())
                    {
                        frameLayout.setBackgroundColor(Color.parseColor(interfaceLayoutConfig.backgroundColor));
                    }
                }

                int adTypeId = (int) interfaceLayoutConfig.adTypeId;
                String iLayoutType = interfaceLayoutConfig.type;

                // Button that occupies whole layout
                if (iLayoutType.equals("button"))
                {
                    WholeLayoutButton wholeLayoutButton = new WholeLayoutButton(interfaceLayoutConfig.id, frameLayout, eventsManager, interfaceLayoutConfig);

                    wholeLayoutButton.init();

                    eventsManager.subscribe(wholeLayoutButton);

                    objectsToCleanUp.add(wholeLayoutButton);
                }

                // Toggle button that occupies whole layout
                if (iLayoutType.equals("toggle_button"))
                {
                    WholeLayoutToggleButton wholeLayoutToggleButton = new WholeLayoutToggleButton(interfaceLayoutConfig.id, frameLayout, eventsManager, interfaceLayoutConfig);

                    wholeLayoutToggleButton.init();

                    eventsManager.subscribe(wholeLayoutToggleButton);

                    objectsToCleanUp.add(wholeLayoutToggleButton);
                }

                // Banner
                if(iLayoutType.equals("admob_ad") || interfaceLayoutConfig.id.equals("custom_ad_1"))
                {
                    Banner banner = new Banner(interfaceLayoutConfig.id, frameLayout, eventsManager, interfaceLayoutConfig);

                    banner.init(activity);

                    eventsManager.subscribe(banner);

                    objectsToCleanUp.add(banner);
                }

                // Content Block View
                if (iLayoutType.equals("channel_container"))
                {
                    ChannelContainter channelContainter = new ChannelContainter(interfaceLayoutConfig.id, frameLayout, eventsManager);

                    channelContainter.init(activity);

                    eventsManager.subscribe(channelContainter);

                    AolAdPlayer aolAdPlayer = aolAdPlayers.get("tab");

                    if (aolAdPlayer == null)
                    {
                        aolAdPlayer = new AolAdPlayer();
                        aolAdPlayers.put("tab", aolAdPlayer);
                    }

                    //aolAdPlayer.subscribeListener(channelContainter);

                    objectsToCleanUp.add(channelContainter);
                }

                // Ad rotator (Screensaver or Tower block)
                if (iLayoutType.equals("ad_rotator"))
                {
                    AdRotator adRotator = new AdRotator(interfaceLayoutConfig.id, frameLayout, eventsManager, interfaceLayoutConfig, adsList);

                    AolAdPlayer aolAdPlayer = null;

                    aolAdPlayer = aolAdPlayers.get(String.valueOf(adTypeId));

                    if (aolAdPlayer == null)
                    {
                        aolAdPlayer = new AolAdPlayer();
                        aolAdPlayers.put(String.valueOf(adTypeId), aolAdPlayer);
                    }

                    adRotators.add(adRotator);

                    aolAdPlayer.subscribeListener(adRotator);

                    objectsToCleanUp.add(adRotator);
                }

                // Interactive View
                if (iLayoutType.equals("interactive"))
                {
                    InteractiveView interactiveView = new InteractiveView(interfaceLayoutConfig.id, frameLayout, eventsManager, interfaceLayoutConfig);

                    interactiveView.init(activity, config_js.params, adsList);

                    this.interactiveView = interactiveView;

                    eventsManager.subscribe(interactiveView);

                    objectsToCleanUp.add(interactiveView);
                }

                // Main menu or bright/sound Selector panel
                if (interfaceLayoutConfig.children != null && interfaceLayoutConfig.children.size() != 0)
                {
                    InteractiveFrame interactiveFrame = new InteractiveFrame(interfaceLayoutConfig.id, frameLayout, eventsManager, interfaceLayoutConfig);
                    interactiveFrame.init(activity);

                    eventsManager.subscribe(interactiveFrame);

                    for (int j = 0; j < interfaceLayoutConfig.children.size(); j++)
                    {
                        Child child = interfaceLayoutConfig.children.get(j);

                        if (child.type.equals("button"))
                        {
                            if (interfaceLayoutConfig.type.equals("main_menu"))
                            {
                                TwoLayersChildButton twoLayersChildButton = new TwoLayersChildButton(interfaceLayoutConfig.id, frameLayout, eventsManager, child, activity, screenRatios);

                                eventsManager.subscribe(twoLayersChildButton);

                                objectsToCleanUp.add(twoLayersChildButton);
                            } else if (interfaceLayoutConfig.type.equals("block"))
                            {
                                SingleLayerChildButton singleLayerChildButton = new SingleLayerChildButton(interfaceLayoutConfig.id, frameLayout, eventsManager, child, activity, screenRatios);

                                eventsManager.subscribe(singleLayerChildButton);

                                objectsToCleanUp.add(singleLayerChildButton);
                            }
                        }
                    }
                    objectsToCleanUp.add(interactiveFrame);
                }

                if (!interfaceLayoutConfig.isVisible())
                {
                    frameLayout.setVisibility(View.INVISIBLE);
                }

                //frameLayout.setAlpha(.2f);
                flAdsContainer.addView(frameLayout);
            }
        }

        if (config_js.params.onInterfaceCreated != null)
        {
            SystemCommandManager.getInstance(activity).processEvent(SET_SCREEN_BRIGHTNESS, config_js.params.onInterfaceCreated.commands);
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        try
        {
            if (cameraFaceDetector != null)
            {
                cameraFaceDetector.stopCamera();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        EventManager eventsManager = EventManager.getInstance();

        for (AdRotator adRotator : adRotators)
        {
            eventsManager.unsubscribe(adRotator);
        }

        for (InteractiveBlock interactiveBlock : objectsToCleanUp)
        {
            interactiveBlock = null;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.i(APP_LOG_TAG, className + ": onPause");

        UserEvents.getInstance().cancelUserLastActivitycountDownTimer();

        for (Map.Entry<String, AolAdPlayer> entry : aolAdPlayers.entrySet())
        {
            entry.getValue().pause();
        }

        EventManager eventManager = EventManager.getInstance();
        eventManager.notify(GOTO_STOPPED_MODE, null);
    }

    @Override
    public void onFaceDetected(int facesCount) // faces count increased
    {
        if (lastFacesCount != facesCount && facesCount == 1) // first face just appeared
        {
            Log.i(APP_LOG_TAG, className + ": User persistence detected");
            lastFacesCount = facesCount;
        }

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                UserEvents.getInstance().cancelUserLastActivitycountDownTimer();
            }
        });

        if (BuildConfig.DEBUG && tvFacesStatus != null && tvFacesStatus.getText().equals("Nobody"))
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    tvFacesStatus.setText("Face");
                }
            });
        }

        if (sensorEventsListener != null)
        {
            sensorEventsListener.onFaceDetected(facesCount);
        }
    }

    @Override
    public void onFaceMissing(int facesCount) // faces count decreased
    {
        if (facesCount == 0)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    UserEvents.getInstance().startLastUserActivityCntDownTimer();
                }
            });

            if (BuildConfig.DEBUG && tvFacesStatus != null)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tvFacesStatus.setText("Nobody");
                    }
                });
            }
        }

        if (sensorEventsListener != null)
        {
            sensorEventsListener.onFaceMissing(facesCount);
        }
    }

    public void setSensorEventsListener(SensorEventsListener sensorEventsListener)
    {
        this.sensorEventsListener = sensorEventsListener;
    }

    @Override
    public void processEvent(String eventType, List<Command> command)
    {
        if (eventType.equals(TAPPED))
        {
            UserEvents.getInstance().startLastUserActivityCntDownTimer();
        }
    }
}
