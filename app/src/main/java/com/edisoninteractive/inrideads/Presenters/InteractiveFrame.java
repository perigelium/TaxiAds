package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.InterfaceLayout;
import com.edisoninteractive.inrideads.EventHandlers.EventManager;

import static com.edisoninteractive.inrideads.Entities.Macrocommands.HIDE_BLOCK;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.KILL_DELAYED_COMMANDS;
import static com.edisoninteractive.inrideads.Entities.Macrocommands.SHOW_BLOCK;

/**
 * Created by Alex Angan one fine day
 */

public class InteractiveFrame extends InteractiveBlock
{
    private InterfaceLayout interfaceLayoutConfig;

    Activity activity;

    public InteractiveFrame(String id, FrameLayout frameLayout, EventManager eventManager, InterfaceLayout interfaceLayoutConfig)
    {
        super(id, frameLayout, eventManager);

        this.interfaceLayoutConfig = interfaceLayoutConfig;
    }

    public void init(Activity activity)
    {
        this.activity = activity;
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

        } else if (commandWithParams.strCommand.equals(KILL_DELAYED_COMMANDS))
        {

        }
    }
}
