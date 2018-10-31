package com.edisoninteractive.inrideads.EventHandlers;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Window;

import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.Macro;
import com.edisoninteractive.inrideads.Entities.Macrocommands;
import com.edisoninteractive.inrideads.Presenters.InteractiveBlock;
import com.edisoninteractive.inrideads.Utils.BrightnessUtils;
import com.edisoninteractive.inrideads.Utils.DelayedCommandTimer;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.edisoninteractive.inrideads.Utils.VolumeUtils;

import java.util.Arrays;
import java.util.Map;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DEBOUNCED_TOUCH_EVENTS_ENABLED;

/**
 * Created by mdumik on 02.01.2018.
 */

public class SystemCommandManager extends InteractiveBlock
{
    private static SystemCommandManager instance;
    private EventManager eventsManager;
    private Context context;
    private Window window;
    private String className = SystemCommandManager.class.getSimpleName();

    //private SystemCommandManager(Context context, Window window)
    public SystemCommandManager(Context context)
    {
        super();
        this.context = context;
        this.eventsManager = EventManager.getInstance();
    }

    public static SystemCommandManager getInstance(Context context)
    {
        if (instance == null)
        {
            //instance = new SystemCommandManager(context, window);
            instance = new SystemCommandManager(context);
        }
        return instance;
    }


    public void handleInternalEvent(String eventType){

        switch (eventType){

            case Macro.Event.Internal.POWER_CONNECTED:

                //1. Brightness to 100%
                BrightnessUtils.getInstance().setBrightness(1f, context);

                //2. Dispatch STOPPED_MODE for ads
                eventsManager.notify(Macro.Command.Internal.GOTO_PLAYBACK_MODE, null);

                //3. Kill power related delayed commands - shutdown/reboot etc
                killPowerDelayedCommands();

                //4. Allow touch listeners
                DEBOUNCED_TOUCH_EVENTS_ENABLED = true;
            break;

            case Macro.Event.Internal.POWER_DISCONNECTED:
                //1. Brightness to 10%
                BrightnessUtils.getInstance().setBrightness(0.1f, context);

                //2. Dispatch GOTO_STOP_MODE for adrotators
                eventsManager.notify(Macro.Command.Internal.GOTO_STOPPED_MODE, null);

                //3. Disallow touch listeners
                DEBOUNCED_TOUCH_EVENTS_ENABLED = false;
            break;

        }

    }


    public void executeCommand(CommandWithParams commandWithParams)
    {
        Log.i(GlobalConstants.APP_LOG_TAG, className + ": " + commandWithParams.strCommand + " received");

        switch (commandWithParams.strCommand)
        {


            /*case Macrocommands.ON_POWER_DISCONNECTED:
                Log.i(GlobalConstants.APP_LOG_TAG, "GOTO_STOPPED_MODE THROUGH SYS COM MANGR : " + + commandWithParams.uniqueID);
                DEBOUNCED_TOUCH_EVENTS_ENABLED = false;

                BrightnessUtils.getInstance(window).setBrightness(0.1f, context);
                eventsManager.notify(Macrocommands.GOTO_STOPPED_MODE, null);
                break;*/

            /*case Macro.Command.Internal.GOTO_PLAYBACK_MODE:
                Log.i(GlobalConstants.APP_LOG_TAG, "GOTO_PLAYBACK_MODE THROUGH SYS COM MANGR :" + commandWithParams.uniqueID);
                DEBOUNCED_TOUCH_EVENTS_ENABLED = true;
                BrightnessUtils.getInstance().setBrightness(1f, context);
                stopInteractiveBlockTimers(false);
            break;*/

            // Will not be handled from config anymore
            /*case Macro.Command.Config.KILL_POWER_DELAYED_COMMANDS:
                killPowerDelayedCommands();
            break;*/

            case Macro.Command.Config.SHUT_DOWN:
                //Toast.makeText(context,"SHUT DOWN!!!!!!!", Toast.LENGTH_LONG);
                SystemUtils.shutdownDevice(context);
            break;

            case Macro.Command.Config.REBOOT:
                SystemUtils.rebootDevice(context);
            break;

            case Macro.Command.Config.KILL_DELAYED_COMMANDS:
                stopInteractiveBlockTimers(true);
            break;

            case Macro.Command.Config.SET_SCREEN_BRIGHTNESS:
                if (commandWithParams.params != null && commandWithParams.params.value != null)
                {
                    BrightnessUtils.getInstance().setBrightness((float) commandWithParams.params.value.doubleValue(), context);
                }
            break;

            case Macro.Command.Config.INCREASE_SCREEN_BRIGHTNESS:
                BrightnessUtils.getInstance().increaseBrightness(context);
            break;

            case Macro.Command.Config.DECREASE_SCREEN_BRIGHTNESS:
                BrightnessUtils.getInstance().decreaseBrightness(context);
            break;

            case Macro.Command.Config.INCREASE_VOLUME:
                VolumeUtils.increaseVolume(context);
            break;

            case Macro.Command.Config.SET_VOLUME:
                if (commandWithParams.params != null && commandWithParams.params.value != null)
                {
                    VolumeUtils.setVolume(context, commandWithParams.params.value);
                }
            break;

            case Macro.Command.Config.DECREASE_VOLUME:
                VolumeUtils.decreaseVolume(context);
            break;

            case Macro.Command.Config.UPDATE_IDLE_TIMEOUT:
                long value = Math.round(commandWithParams.params.value);
                UserEvents.getInstance().startLastUserActivityCntDownTimer(value);
                break;
        }

        Log.d(GlobalConstants.APP_LOG_TAG, className + "Services/SystemCommandManager: executeCommand(): " + commandWithParams.strCommand);
    }

    private void stopInteractiveBlockTimers(boolean broadkastExclusive)
    {
        for (Map.Entry entry : al_CountDownTimers.entrySet())
        {
            CountDownTimer countDownTimer = (CountDownTimer) entry.getValue();
            String strId = (String) entry.getKey();

            if (broadkastExclusive && Arrays.asList(eventsManager.blockOperations).contains(strId))
            {
                Log.i(GlobalConstants.APP_LOG_TAG, className + ": executeCommand KILL_DELAYED_COMMANDS for: " + entry.getKey() + " skipped" );
               continue;
            }

            if (countDownTimer != null)
            {
                Log.i(GlobalConstants.APP_LOG_TAG, className + ": executeCommand: countDownTimer.cancel: " + entry.getKey());
                countDownTimer.cancel();
                al_CountDownTimers.remove(strId);
            }
        }
    }

    private void killPowerDelayedCommands(){
        for(Map.Entry entry : al_CountDownTimers.entrySet())
        {
            DelayedCommandTimer countDownTimer = (DelayedCommandTimer) entry.getValue();

            //Log.i(GlobalConstants.APP_LOG_TAG, "Key : " + entry.getKey() + " command " + countDownTimer.command.strCommand);

            //if ((countDownTimer != null) && (countDownTimer.command.strCommand.equals(Macrocommands.SHUT_DOWN)))
            if (((countDownTimer != null)) && countDownTimer.command.strCommand.equals(Macrocommands.SHUT_DOWN))
            {
                countDownTimer.cancel();
                //al_CountDownTimers.remove(entry.getKey());
                //al_CountDownTimers.removeAt(al_CountDownTimers.indexOfKey(entry.getKey()));

                Log.i(GlobalConstants.APP_LOG_TAG, className + ": KILL_POWER_DELAYED_COMMANDS canceling " + entry.getKey() + " cmd:" + countDownTimer.command.strCommand);
                //countDownTimer.cancel();
                //al_CountDownTimers.remove(strId);
            }
        }
    }
}
