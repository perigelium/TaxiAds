package com.edisoninteractive.inrideads.Presenters;

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
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SELECT_TOGGLE_BUTTON;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.UNSELECT_TOGGLE_BUTTON;

/**
 * Created by Alex Angan one fine day
 */

public class WholeLayoutToggleButton extends InteractiveBlock
{
    InterfaceLayout interfaceLayoutConfig;

    public WholeLayoutToggleButton(String id, FrameLayout frameLayout, EventManager eventsManager, InterfaceLayout interfaceLayoutConfig)
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
            public boolean onTouched(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    UserEvents.getInstance().startLastUserActivityCntDownTimer();

                    if (interfaceLayoutConfig.toggled)
                    {
                        setInterfaceButtonDefaultBackground(interfaceLayoutConfig, frameLayout);

                        eventsManager.notify(UNSELECT_TOGGLE_BUTTON, interfaceLayoutConfig.onUnselect.commands);
                    } else
                    {
                        setInterfaceButtonPressedBackground(interfaceLayoutConfig, frameLayout);

                        eventsManager.notify(SELECT_TOGGLE_BUTTON, interfaceLayoutConfig.onSelect.commands);
                    }
                    interfaceLayoutConfig.toggled = !interfaceLayoutConfig.toggled;
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
        }
    }

    @Override
    protected void executeCommand(CommandWithParams commandWithParams)
    {
        if (commandWithParams.strCommand.equals(HIDE_BLOCK))
        {
            frameLayout.setVisibility(View.INVISIBLE);

        } else if (commandWithParams.strCommand.equals(SHOW_BLOCK))
        {
            frameLayout.setVisibility(View.VISIBLE);
        }
        else if (commandWithParams.strCommand.equals(UNSELECT_TOGGLE_BUTTON))
        {
            setInterfaceButtonDefaultBackground(interfaceLayoutConfig, frameLayout);
            interfaceLayoutConfig.toggled = false;
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
