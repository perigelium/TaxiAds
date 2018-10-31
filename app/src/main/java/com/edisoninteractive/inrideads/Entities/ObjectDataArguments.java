package com.edisoninteractive.inrideads.Entities;

public class ObjectDataArguments
{
    public String unit_id;
    public String uuid;
    public String wifi_mac;
    public String file; // file name
    public String event_id;

    public ObjectDataArguments(String unit_id,  String uuid, String wifi_mac, String file, String event_id)
    {
        this.unit_id = unit_id;
        this.uuid = uuid;
        this.wifi_mac = wifi_mac;
        this.file = file;
        this.event_id = event_id;
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

    public String getFile()
    {
        return file;
    }

    public String getEvent_id()
    {
        return event_id;
    }

    public void setEvent_id(String event_id)
    {
        this.event_id = event_id;
    }

    public void setFile(String file)
    {
        this.file = file;
    }
}
