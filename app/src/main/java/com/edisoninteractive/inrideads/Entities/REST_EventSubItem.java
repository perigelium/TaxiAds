package com.edisoninteractive.inrideads.Entities;


public class REST_EventSubItem
{
    private String id;
    private String status;
    private String unit_id;
    private String uuid;
    private String wifi_mac;

    public REST_EventSubItem(String id, String status, String unit_id, String uuid, String wifi_mac)
    {
        this.id = id;
        this.status = status;
        this.unit_id = unit_id;
        this.uuid = uuid;
        this.wifi_mac = wifi_mac;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUnit_id()
    {
        return unit_id;
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getWifi_mac()
    {
        return wifi_mac;
    }

    public String getId()
    {
        return id;
    }
}
