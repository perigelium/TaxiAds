package com.edisoninteractive.inrideads.Entities;

/**
 * Created by Alex Angan on 16.11.2017.
 */

public class ConfigDataArguments
{
    public String force;
    public String unit_id;
    public String uuid;
    public String rnd;
    public String wifi_mac;
    public String sw_version;

    public String latitude;
    public String longitude;
    public String location_accuracy;
    public String location_provider_name;

    public ConfigDataArguments(String force, String unit_id, String uuid, String rnd, String wifi_mac, String sw_version,
                               String latitude, String longitude, String location_accuracy, String location_provider_name)
    {
        this.force = force != null ? force : "";
        this.unit_id = unit_id != null ? unit_id : "";
        this.uuid = uuid != null ? uuid : "";
        this.rnd = rnd != null ? rnd : "";
        this.wifi_mac = wifi_mac != null ? wifi_mac : "";
        this.sw_version = sw_version != null ? sw_version : "";
        this.latitude = latitude != null ? latitude : "";
        this.longitude = longitude != null ? longitude : "";
        this.location_accuracy = location_accuracy!=null ? location_accuracy : "";
        this.location_provider_name = location_provider_name!=null ? location_provider_name : "";
    }
}
