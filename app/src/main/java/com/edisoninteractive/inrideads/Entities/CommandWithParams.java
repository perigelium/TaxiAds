package com.edisoninteractive.inrideads.Entities;

public class CommandWithParams
{
    public String strCommand;
    public long delay;
    public Params_ params;
    public long uniqueID;

    public CommandWithParams(String strCommand, long delay, Params_ params)
    {
        this.strCommand = strCommand;
        this.delay = delay;
        this.params = params;
        uniqueID = System.currentTimeMillis();
    }
}
