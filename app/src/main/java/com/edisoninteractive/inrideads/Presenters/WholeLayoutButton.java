package com.edisoninteractive.inrideads.Presenters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.InterfaceLayout;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.EventHandlers.UserEvents;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.KILL_DELAYED_COMMANDS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_PRESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;

/**
 * Created by Alex Angan one fine day
 */

public class WholeLayoutButton extends InteractiveBlock
{
    InterfaceLayout interfaceLayoutConfig;

    public WholeLayoutButton(String id, FrameLayout frameLayout, EventManager eventsManager, InterfaceLayout interfaceLayoutConfig)
    {
        super(id, frameLayout, eventsManager);

        this.interfaceLayoutConfig = interfaceLayoutConfig;
    }

    public void init()
    {
        setInterfaceButtonDefaultBackground(interfaceLayoutConfig, frameLayout);

        frameLayout.setOnTouchListener(new OnTouchDebouncedListener()
        {
            @Override
            public boolean onTouched(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    UserEvents.getInstance().startLastUserActivityCntDownTimer();

                    setInterfaceButtonPressedBackground(interfaceLayoutConfig, frameLayout);

                    /*if(interfaceLayoutConfig.getOnPress() != null)
                    {
                        eventsManager.notify(ON_PRESS, interfaceLayoutConfig.getOnPress().commands);
                    }*/
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    setInterfaceButtonDefaultBackground(interfaceLayoutConfig, frameLayout);

                    if(interfaceLayoutConfig.getOnPress() != null)
                    {
                        eventsManager.notify(ON_PRESS, interfaceLayoutConfig.getOnPress().commands);
                    }
                }

                return true;
            }
        });
    }

    private void setInterfaceButtonPressedBackground(InterfaceLayout interfaceLayoutConfig, FrameLayout frameLayout)
    {
        if (interfaceLayoutConfig.downStateImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + interfaceLayoutConfig.downStateImageURL);

            if (d != null)
            {
                frameLayout.setBackground(d);
            }
        }else if(interfaceLayoutConfig.downStateColor!=null){
            frameLayout.setBackgroundColor(Color.parseColor(interfaceLayoutConfig.downStateColor));
        }
    }

    private void setInterfaceButtonDefaultBackground(InterfaceLayout interfaceLayoutConfig, FrameLayout frameLayout)
    {
        if (interfaceLayoutConfig.upStateImageURL != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + interfaceLayoutConfig.upStateImageURL);

            if (d != null)
            {
                frameLayout.setBackground(d);
            }
        }else if(interfaceLayoutConfig.upStateColor != null){
            frameLayout.setBackgroundColor(Color.parseColor(interfaceLayoutConfig.upStateColor));
        }
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        if (commandWithParams.strCommand.equals(HIDE_BLOCK))
        {
            setInterfaceButtonDefaultBackground(interfaceLayoutConfig, frameLayout);
            frameLayout.setVisibility(View.INVISIBLE);

        } else if (commandWithParams.strCommand.equals(SHOW_BLOCK))
        {
            frameLayout.setVisibility(View.VISIBLE);
        }
        else if (commandWithParams.strCommand.equals(KILL_DELAYED_COMMANDS))
        {
            CountDownTimer countDownTimer = al_CountDownTimers.get(id);

            if (countDownTimer != null)
            {
                countDownTimer.cancel();
            }
        }
    }
}
