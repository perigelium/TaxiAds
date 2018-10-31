package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.edisoninteractive.inrideads.Entities.CSSstyleAttrs;
import com.edisoninteractive.inrideads.Entities.Child;
import com.edisoninteractive.inrideads.Entities.Child_;
import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.EventHandlers.StatsManager;
import com.edisoninteractive.inrideads.EventHandlers.UserEvents;
import com.edisoninteractive.inrideads.Utils.MyTextUtils;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_PRESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_STD_CHANNEL;

/**
 * Created by Alex Angan one fine day
 */

public class TwoLayersChildButton extends InteractiveBlock
{
    private final Button imageButton;
    private final ImageView imageView;
    private Activity activity;

    public TwoLayersChildButton(String id, FrameLayout frameLayout, final EventManager eventsManager,
                                final Child child, Activity activity, Pair<Double, Double> screenDimensions)
    {
        super(id, frameLayout, eventsManager);
        this.activity = activity;

        int leftTopX = (int) (child.x * screenDimensions.first);
        int leftTopY = (int) (child.y * screenDimensions.second);

        int childWidth = (int) (child.hitAreaWidth * screenDimensions.first);
        int childHeight = (int) (child.hitAreaHeight * screenDimensions.second);

        if (childWidth == 0) childWidth = (int) (child.buttonWidth * screenDimensions.first);
        if (childHeight == 0) childHeight = (int) (child.buttonHeight * screenDimensions.second);

        FrameLayout.LayoutParams lpReal = new FrameLayout.LayoutParams(childWidth, childHeight);

        imageButton = new Button(activity);

        Child_ child_ = child.children.get(0);

        if(child_!=null && child_.text != null && child_.text.length() != 0)
        {
            CSSstyleAttrs cssStyleAttrs = MyTextUtils.retrieveCSSstyle(child_.style);

            if(cssStyleAttrs != null)
            {
                imageButton.setTextColor(Color.parseColor(cssStyleAttrs.color));
                imageButton.setTextSize(cssStyleAttrs.fontSize);

                if (cssStyleAttrs.fontWeight.length() != 0 && cssStyleAttrs.fontWeight.equals("bold"))
                {
                    imageButton.setTypeface(null, Typeface.BOLD);
                }
            }
            imageButton.setText(child_.text);
        }

        imageButton.setX(leftTopX);
        imageButton.setY(leftTopY);
        imageButton.setLayoutParams(lpReal);

        //imageButton.setAlpha(0.2f);

        setChildViewDefaultBackground(child, imageButton);

        imageButton.setOnTouchListener(new OnTouchDebouncedListener()
        {
            @Override
            public boolean onTouched(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    UserEvents.getInstance().startLastUserActivityCntDownTimer();

                    setChildViewPressedBackground(child, imageButton);

                    /*if (child.onPress != null && child.onPress.commands != null)
                    {
                        eventsManager.notify(ON_PRESS, child.onPress.commands);
                    }*/
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    setChildViewDefaultBackground(child, imageButton);

                    saveButtonStats(child);

                    if (child.onPress != null && child.onPress.commands != null)
                    {
                        eventsManager.notify(ON_PRESS, child.onPress.commands);
                    }

                }

                return true;
            }
        });

        frameLayout.addView(imageButton);

        imageView = new ImageView(activity);

        imageView.setX(leftTopX);
        imageView.setY(leftTopY);
        imageView.setLayoutParams(lpReal);

        //imageView.setAlpha(0.2f);

        if (child.topImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + child.topImageURL);

            if (d != null)
            {
                imageView.setImageDrawable(d);
            } else
            {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

        frameLayout.addView(imageView);
    }

    private void setChildViewPressedBackground(Child child, View view)
    {
        if (child.downStateImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + child.downStateImageURL);

            if (d != null)
            {
                view.setBackground(d);
            }
        } else if (child.downStateColor != null && !child.downStateColor.isEmpty())
        {
            view.setBackgroundColor(Color.parseColor(child.downStateColor));
        }
    }

    private void setChildViewDefaultBackground(Child child, View view)
    {
        if (child.upStateImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + child.upStateImageURL);

            if (d != null)
            {
                view.setBackground(d);
            }
        } else if (child.upStateColor != null && !child.upStateColor.isEmpty())
        {
            view.setBackgroundColor(Color.parseColor(child.upStateColor));
        }
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        if (commandWithParams.strCommand.equals(HIDE_BLOCK))
        {
            imageButton.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);

        } else if (commandWithParams.strCommand.equals(SHOW_BLOCK))
        {
            imageButton.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void saveButtonStats(Child child)
    {
        for (Command command : child.onPress.commands)
        {
            if (TextUtils.equals(command.command, SHOW_STD_CHANNEL))
            {
                try
                {
                    Log.d(GlobalConstants.APP_LOG_TAG, "Presenters/TwoLayoutsChildButton: saveButtonStats() called");

                    StatsManager.getInstance(activity).writeStats(
                            StatsManager.CHANNEL,
                            String.valueOf(child.statsId),
                            String.valueOf(child.campaignId),
                            "null");
                } catch (Exception ex)
                {
                    Log.d(GlobalConstants.APP_LOG_TAG, "Presenters/TwoLayoutsChildButton: saveButtonStats() EXCEPTION : " + ex.getMessage());

                    ex.printStackTrace();
                }
            }
        }
    }
}
