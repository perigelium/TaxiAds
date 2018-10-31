package com.edisoninteractive.inrideads.Presenters;

import android.util.ArrayMap;
import android.util.Log;
import android.widget.FrameLayout;

import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;
import com.edisoninteractive.inrideads.Interfaces.CustomEventListener;
import com.edisoninteractive.inrideads.Utils.DelayedCommandTimer;
import com.edisoninteractive.inrideads.Utils.MyTextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Angan one fine day
 */

public abstract class InteractiveBlock implements CustomEventListener
{
    protected static ArrayMap<String, DelayedCommandTimer> al_CountDownTimers = new ArrayMap<>();
    protected String id;
    protected FrameLayout frameLayout;
    protected EventManager eventsManager;

    protected abstract void executeCommand(CommandWithParams commandWithParams);

    protected InteractiveBlock()
    {
    }

    InteractiveBlock(String id, FrameLayout frameLayout, final EventManager eventsManager)
    {
        this.frameLayout = frameLayout;
        this.id = id;
        this.eventsManager = eventsManager;
    }

    @Override
    public void processEvent(String eventType, List<Command> commands)
    {

        List<CommandWithParams> lstCommandWithParams = new ArrayList<>();
        String timerId;

        if (commands == null)
        {
            CommandWithParams commandWithParams = new CommandWithParams(eventType, 0, null);
            lstCommandWithParams.add(commandWithParams);
        } else
        {
            lstCommandWithParams = MyTextUtils.getCommandString(id, commands);
        }

        if (id != null)
        {
            timerId = id;
        } else
        {
            timerId = eventType;
        }

        for (final CommandWithParams commandWithParams : lstCommandWithParams)
        {
            if (commandWithParams.delay == 0)
            {
                executeCommand(commandWithParams);
            } else
            {
                DelayedCommandTimer countDownTimer = al_CountDownTimers.get(timerId);

                if (countDownTimer != null)
                {
                    countDownTimer.cancel();
                }

                long millisecondsDelay = Math.round(commandWithParams.delay) * 1000;

                Log.i(GlobalConstants.APP_LOG_TAG, commandWithParams.strCommand + ": processEvent -> delay: " + millisecondsDelay);

                countDownTimer = new DelayedCommandTimer(millisecondsDelay, 1000, commandWithParams)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {

                    }

                    @Override
                    public void onFinish()
                    {
                        executeCommand(this.command);
                    }
                };

                al_CountDownTimers.put(timerId, countDownTimer);
                countDownTimer.start();
            }
        }
    }
}
