package com.edisoninteractive.inrideads.EventHandlers;

import android.os.CountDownTimer;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.Params;

import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_ENTER_IDLE_MODE;

/**
 * Created by Alex Angan on 09.03.2018.
 */

public class UserEvents
{
    private static volatile UserEvents instance;

    public static UserEvents getInstance()
    {
        if (instance == null)
        {
            synchronized (UserEvents.class)
            {
                if (instance == null)
                {
                    instance = new UserEvents();
                }
            }
        }
        return instance;
    }

    private CountDownTimer userLastActivitycountDownTimer;
    private long userIdleInterval;
    private List<Command> commands;
    private String className = getClass().getSimpleName();
    private Params params;

    public void init(Params params)
    {
        userIdleInterval = params.idleInterval;
        this.params = params;

        if(params.onEnterIdleMode != null)
        {
            commands = params.onEnterIdleMode.commands;
        }
    }

    public void startLastUserActivityCntDownTimer()
    {
        Log.i(APP_LOG_TAG, className + ": User activity detected");

        if (userLastActivitycountDownTimer != null)
        {
            userLastActivitycountDownTimer.cancel();
        }

        userLastActivitycountDownTimer = new CountDownTimer(userIdleInterval * 1000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                Log.i(APP_LOG_TAG, className + ": On enter idle mode invoked");
                userIdleInterval = params.idleInterval;
                EventManager eventManager = EventManager.getInstance();
                eventManager.notify(ON_ENTER_IDLE_MODE, commands);
            }
        }.start();
    }

    public void cancelUserLastActivitycountDownTimer()
    {
        if (userLastActivitycountDownTimer != null)
        {
            userLastActivitycountDownTimer.cancel();
        }
    }

    public void startLastUserActivityCntDownTimer(long userIdleInterval)
    {
        this.userIdleInterval = userIdleInterval;
        startLastUserActivityCntDownTimer();
    }
}
