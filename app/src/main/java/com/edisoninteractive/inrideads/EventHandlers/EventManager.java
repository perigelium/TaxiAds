package com.edisoninteractive.inrideads.EventHandlers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Interfaces.CustomEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.CHANNEL_NAV_BACK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.CLEAR_CHANNEL_CONTAINER;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.DECREASE_SCREEN_BRIGHTNESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.DECREASE_VOLUME;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.DISABLE_FACE_DETECTION;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ENABLE_FACE_DETECTION;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.FREEZE_WEB_CONTENT;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_IDLE_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_PLAYBACK_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_SLEEP_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_STOPPED_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.GOTO_WORK_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.INCREASE_SCREEN_BRIGHTNESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.INCREASE_VOLUME;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.KILL_DELAYED_COMMANDS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.LOCATION_UPDATED;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_ENTER_IDLE_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_LONG_PRESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_POWER_CONNECTED;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_POWER_DISCONNECTED;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_PRESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_SHOW_INTERACTIVE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.ON_START_IN_POWER_OFF_MODE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.RESTART_COMMAND;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SELECT_TOGGLE_BUTTON;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SET_BLOCK_PROPERTY;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SET_GLOBAL_ARGUMENT;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SET_SCREEN_BRIGHTNESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_RADMIN;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_SHUTDOWN_AD;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_STARTUP_AD;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_STD_CHANNEL;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_WEB_CONTENT;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHUT_DOWN;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.START_EXTERNAL_PROCESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.STOP_ALL_EXTERNAL_PROCESSES;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.STOP_EXTERNAL_PROCESS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.TAPPED;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.TRACE;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.UNFREEZE_WEB_CONTENT;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.UNSELECT_TOGGLE_BUTTON;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.UPDATE_IDLE_TIMEOUT;

/**
 * Created by Alex Angan one fine day
 */

public class EventManager
{
    private static volatile EventManager instance;

    public static EventManager getInstance()
    {
        if (instance == null)
        {
            synchronized (EventManager.class)
            {
                if (instance == null)
                {
                    instance = new EventManager();
                }
            }
        }
        return instance;
    }

    Map<String, List<CustomEventListener>> listeners = new HashMap<>();

    public String[] blockOperations = {SHOW_BLOCK, HIDE_BLOCK, ON_PRESS, ON_LONG_PRESS, ON_ENTER_IDLE_MODE, ON_SHOW_INTERACTIVE,
            SELECT_TOGGLE_BUTTON, UNSELECT_TOGGLE_BUTTON, SHOW_STD_CHANNEL, CHANNEL_NAV_BACK, SET_BLOCK_PROPERTY, SHOW_WEB_CONTENT,
            SHOW_SHUTDOWN_AD, SHOW_STARTUP_AD, CLEAR_CHANNEL_CONTAINER, LOCATION_UPDATED, TAPPED, RESTART_COMMAND,

            SET_GLOBAL_ARGUMENT, SHOW_RADMIN, UPDATE_IDLE_TIMEOUT, KILL_DELAYED_COMMANDS, SHUT_DOWN, SET_SCREEN_BRIGHTNESS,
            FREEZE_WEB_CONTENT, UNFREEZE_WEB_CONTENT, TRACE, GOTO_IDLE_MODE, GOTO_SLEEP_MODE, GOTO_WORK_MODE, GOTO_PLAYBACK_MODE,
            GOTO_STOPPED_MODE, INCREASE_SCREEN_BRIGHTNESS, DECREASE_SCREEN_BRIGHTNESS, START_EXTERNAL_PROCESS,
            STOP_EXTERNAL_PROCESS, STOP_ALL_EXTERNAL_PROCESSES, INCREASE_VOLUME, DECREASE_VOLUME, ENABLE_FACE_DETECTION,
            DISABLE_FACE_DETECTION, ON_START_IN_POWER_OFF_MODE, ON_POWER_CONNECTED, ON_POWER_DISCONNECTED};

    private EventManager()
    {
        for (String operation : blockOperations)
        {
            this.listeners.put(operation, new ArrayList<CustomEventListener>());
        }
    }

    public void subscribe(CustomEventListener listener)
    {
        for (String eventType : blockOperations)
        {
            List<CustomEventListener> users = listeners.get(eventType);

            if (users == null)
            {
                continue;
            }
            users.add(listener);
        }

        Log.i(APP_LOG_TAG, "EventManager - all listeners subscribed");
    }

    public void unsubscribe(CustomEventListener listener)
    {
        for (String eventType : blockOperations)
        {
            List<CustomEventListener> users = listeners.get(eventType);

            if (users == null)
            {
                continue;
            }

            int index = users.indexOf(listener);
            users.remove(index);
        }
    }
    
    public void notify(@NonNull String eventType, List<Command> commands)
    {
        List<CustomEventListener> users = listeners.get(eventType);

        if (users == null)
        {
            Log.w(APP_LOG_TAG, "EventManager - notify: no such eventType registered: " + eventType);
            return;
        }

        for (CustomEventListener listener : users)
        {
            listener.processEvent(eventType, commands);
        }
    }
}
