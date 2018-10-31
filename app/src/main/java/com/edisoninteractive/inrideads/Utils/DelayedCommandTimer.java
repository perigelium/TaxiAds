package com.edisoninteractive.inrideads.Utils;

import android.os.CountDownTimer;

import com.edisoninteractive.inrideads.Entities.CommandWithParams;

public abstract class DelayedCommandTimer extends CountDownTimer
{
    public CommandWithParams command;

    public DelayedCommandTimer(long millisInFuture, long countDownInterval, CommandWithParams commandWithParams)
    {
        super(millisInFuture, countDownInterval);
        command = commandWithParams;
    }
}
