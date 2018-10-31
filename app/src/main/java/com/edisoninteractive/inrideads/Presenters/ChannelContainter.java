package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.edisoninteractive.inrideads.BuildConfig;
import com.edisoninteractive.inrideads.Entities.CSSstyleAttrs;
import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.Params_;
import com.edisoninteractive.inrideads.Entities.Tab;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.EventHandlers.UserEvents;
import com.edisoninteractive.inrideads.Utils.MyTextUtils;

import java.util.ArrayList;
import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_STOPPED_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.KILL_DELAYED_COMMANDS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_ENTER_IDLE_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_STD_CHANNEL;

/**
 * Created by Alex Angan one fine day
 */

public class ChannelContainter extends InteractiveBlock
{
    private String className = getClass().getSimpleName();
    private FrameLayout frameLayout;
    private Activity activity;
    private FrameLayout frameLayoutContent;
    private TabContentRenderer tabContentRenderer;
    private int tabsCount;
    private List<Button> tabButtons;
    private long lastChannelCreationTimestamp;
    private Interstitial interstitial;

    public ChannelContainter(String id, FrameLayout frameLayout, EventManager eventManager)
    {
        super(id, frameLayout, eventManager);

        this.frameLayout = frameLayout;
    }

    public void init(final Activity activity)
    {
        this.activity = activity;

        if(BuildConfig.ENABLE_AD_MOB)
        {
            interstitial = Interstitial.getInstance();
            interstitial.init(activity);
        }
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        switch (commandWithParams.strCommand)
        {
            case SHOW_BLOCK:
                frameLayout.setVisibility(View.VISIBLE);
                break;

            case KILL_DELAYED_COMMANDS:
                CountDownTimer countDownTimer = al_CountDownTimers.get(id);

                if (countDownTimer != null)
                {
                    countDownTimer.cancel();
                }
                break;

            case SHOW_STD_CHANNEL:
                // Show channel_containter
                if (commandWithParams.params.channelConfig.tabs != null && commandWithParams.params.channelConfig.tabs.size() != 0)
                {
                    // check if command list have show_std_channel command and there are at least 2 sec between last 2 commands
                    long now = System.currentTimeMillis();
                    long showChannelDiff = now - lastChannelCreationTimestamp;
                    lastChannelCreationTimestamp = now;

                    if (showChannelDiff > GlobalConstants.SHOW_STD_CHANNEL_CUT_OFF_INTERVAL)
                    {
                        buildTabs(commandWithParams.params);
                        showFirstTabContent(commandWithParams.params);
                    } else
                    {
                        // ignore the whole list of commands if show_std_channel comes too frequently
                        Log.w(GlobalConstants.APP_LOG_TAG, "User attempts to invoke show_std_channel too frequent. Ignoring....");
                    }
                }
                break;

            case HIDE_BLOCK:
            case ON_ENTER_IDLE_MODE:
            case GOTO_STOPPED_MODE:
                if (tabContentRenderer != null)
                {
                    tabContentRenderer.stopRendering();
                }
                frameLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void buildTabs(final Params_ params_)
    {
        frameLayout.removeAllViews();
        tabButtons = new ArrayList<>();

        LinearLayout linearLayoutParent = new LinearLayout(activity);

        LinearLayout.LayoutParams lprParent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayoutParent.setOrientation(LinearLayout.VERTICAL);

        linearLayoutParent.setLayoutParams(lprParent);

        int tabHeight = (int) Math.round(params_.channelConfig.tabHeight);

        LinearLayout linearLayoutTabs = new LinearLayout(activity);

        LinearLayout.LayoutParams lprTabs = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tabHeight);
        linearLayoutTabs.setOrientation(LinearLayout.HORIZONTAL);

        linearLayoutTabs.setLayoutParams(lprTabs);

        tabsCount = params_.channelConfig.tabs.size();

        if (tabsCount != 1) // don't show tabs if tab is single
        {
            try
            {
                int tabDelimiterWidth = Math.round(Float.valueOf(params_.channelConfig.tabDelimiterSize));
                final int tabUpColor = Color.parseColor(params_.channelConfig.tabUpStateDefaultColor);
                final int tabDownColor = Color.parseColor(params_.channelConfig.tabDownStateDefaultColor);
                int tabDelimiterColor = Color.parseColor(params_.channelConfig.tabDelimiterColor);
                final CSSstyleAttrs cssUpStyleAttrs = MyTextUtils.retrieveCSSstyle(params_.channelConfig.tabLabelUpStateStyle);
                final CSSstyleAttrs cssDownStyleAttrs = MyTextUtils.retrieveCSSstyle(params_.channelConfig.tabLabelDownStateStyle);

                for (int i = 0; i < tabsCount; i++)
                {
                    final Button viewTab = new Button(activity);
                    LinearLayout.LayoutParams lpTabReal = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    lpTabReal.weight = 1;
                    viewTab.setLayoutParams(lpTabReal);

                    final String upStateImageURL = params_.channelConfig.tabs.get(i).upStateImageURL;
                    final String downStateImageURL = params_.channelConfig.tabs.get(i).downStateImageURL;

                    setTabButtonDefaultBackground(upStateImageURL, tabUpColor, viewTab);

                    viewTab.setText(params_.channelConfig.tabs.get(i).header);

                    if (cssUpStyleAttrs != null)
                    {
                        viewTab.setTextColor(Color.parseColor(cssUpStyleAttrs.color));
                        viewTab.setTextSize(cssUpStyleAttrs.fontSize);

                        if (cssUpStyleAttrs.fontWeight.length() != 0 && cssUpStyleAttrs.fontWeight.equals("bold"))
                        {
                            viewTab.setTypeface(null, Typeface.BOLD);
                        }
                    }

                    final int finalI = i;

                    viewTab.setOnTouchListener(new OnTouchDebouncedListener()
                    {
                        @Override
                        public boolean onTouched(View view, MotionEvent event)
                        {
                            if (event.getAction() == MotionEvent.ACTION_DOWN)
                            {
                                UserEvents.getInstance().startLastUserActivityCntDownTimer();

                                setAllTabsBackgroundToDefaultState(cssUpStyleAttrs, params_, tabUpColor);
                                setTabButtonPressedBackground(downStateImageURL, tabDownColor, viewTab);

                                if (cssDownStyleAttrs != null)
                                {
                                    viewTab.setTextColor(Color.parseColor(cssDownStyleAttrs.color));
                                    viewTab.setTextSize(cssDownStyleAttrs.fontSize);

                                    if (cssDownStyleAttrs.fontWeight.length() != 0 && cssDownStyleAttrs.fontWeight.equals("bold"))
                                    {
                                        viewTab.setTypeface(null, Typeface.BOLD);
                                    }
                                }

                                showTabContent(params_.channelConfig.tabs.get(finalI));
                            }

                            return false;
                        }
                    });

                    tabButtons.add(viewTab);
                    linearLayoutTabs.addView(viewTab);

                    if (i == tabsCount - 1) // last tab not followed by delimiter
                    {
                        break;
                    }

                    View viewDelimiter = new View(activity);
                    LinearLayout.LayoutParams lpDelimiterReal = new LinearLayout.LayoutParams(tabDelimiterWidth, tabHeight);
                    viewDelimiter.setLayoutParams(lpDelimiterReal);
                    viewDelimiter.setBackgroundColor(tabDelimiterColor);
                    linearLayoutTabs.addView(viewDelimiter);
                }
                linearLayoutParent.addView(linearLayoutTabs);

            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
        }

        frameLayoutContent = new FrameLayout(activity);

        FrameLayout.LayoutParams lprContent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        frameLayoutContent.setLayoutParams(lprContent);

        linearLayoutParent.addView(frameLayoutContent);
        frameLayout.addView(linearLayoutParent);
    }

    private void showFirstTabContent(Params_ params_)
    {
        if (tabButtons != null && tabButtons.size() > 1)
        {
            long downTime = System.currentTimeMillis();
            long eventTime = SystemClock.elapsedRealtime() + 100;
            float x = 0.0f;
            float y = 0.0f;
            int metaState = 0;

            MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0, 0, metaState);

            tabButtons.get(0).dispatchTouchEvent(motionEvent);
        } else if (tabsCount > 0)
        {
            showTabContent(params_.channelConfig.tabs.get(0));
        } else
        {
            //no tabs at all - show error icon
        }
    }

    private void showTabContent(Tab tab)
    {
        if (tabContentRenderer != null)
        {
            tabContentRenderer.stopRendering();
        }

        String type = tab.type;

        if (type.equals("aol"))
        {
            tabContentRenderer = new TabAolContentRenderer(activity, frameLayoutContent, tab);
        } else if (type.equals("file"))
        {
            tabContentRenderer = new TabLocalFileRenderer(activity, frameLayoutContent, tab);
        } else if (type.equals("webview") || type.equals("web_view_local"))
        {
            tabContentRenderer = new TabWebViewRenderer(activity, frameLayoutContent, tab);
        }

        if (tabContentRenderer != null)
        {
            if(BuildConfig.ENABLE_AD_MOB)
            {
                interstitial.tryToShowAdd();
            }
            
            tabContentRenderer.startRendering();
        }
    }

    private void setAllTabsBackgroundToDefaultState(CSSstyleAttrs cssUpStyleAttrs, Params_ params_, int tabUpColor)
    {
        for (int i = 0; i < tabButtons.size(); i++)
        {
            Button tabButton = tabButtons.get(i);

            String upStateImageURL = params_.channelConfig.tabs.get(i).upStateImageURL;

            setTabButtonDefaultBackground(upStateImageURL, tabUpColor, tabButton);

            if (cssUpStyleAttrs != null)
            {
                tabButton.setTextColor(Color.parseColor(cssUpStyleAttrs.color));
                tabButton.setTextSize(cssUpStyleAttrs.fontSize);

                if (cssUpStyleAttrs.fontWeight.length() != 0 && cssUpStyleAttrs.fontWeight.equals("bold"))
                {
                    tabButton.setTypeface(null, Typeface.BOLD);
                }
            }
        }
        frameLayoutContent.removeAllViews();
    }

    private void setTabButtonDefaultBackground(String upStateImageURL, int upStateColor, Button button)
    {
        if (upStateImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + upStateImageURL);

            if (d != null)
            {
                button.setBackground(d);
            }
        } else
        {
            button.setBackgroundColor(upStateColor);
        }
    }

    private void setTabButtonPressedBackground(String downStateImageURL, int downStateColor, Button button)
    {
        if (downStateImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + downStateImageURL);

            if (d != null)
            {
                button.setBackground(d);
            }
        } else
        {
            button.setBackgroundColor(downStateColor);
        }
    }
}
