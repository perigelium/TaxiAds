package com.edisoninteractive.inrideads.Entities;

import java.util.List;

public class ConfigData
{
    private boolean success;
    private String unit_id;
    private String uuid;
    private String mac;
    private boolean isNew;
    private int error;
    private int itemCount;
    private String response;
    private long timeStamp;
    private double updateSize;

    private List<REST_ConfigSubItem> items;

    public ConfigData()
    {
    }

    public List<REST_ConfigSubItem> getItems()
    {
        return items;
    }

    public void setItems(List<REST_ConfigSubItem> items)
    {
        this.items = items;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getUnit_id()
    {
        return unit_id;
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getMac()
    {
        return mac;
    }

    public boolean isNew()
    {
        return isNew;
    }

    public int getError()
    {
        return error;
    }

    public int getItemCount()
    {
        return itemCount;
    }

    public String getResponse()
    {
        return response;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public double getUpdateSize()
    {
        return updateSize;
    }
}
