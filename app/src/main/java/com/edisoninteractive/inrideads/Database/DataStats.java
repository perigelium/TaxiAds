package com.edisoninteractive.inrideads.Database;

/**
 * Created by mdumik on 18.12.2017.
 */

public class DataStats {

    private int id;
    private String statsType;
    private String statsId;
    private int count;
    private String date;
    private int sent;
    private String unitId;
    private String campaignId;
    private String details;
    private String time;
    private String lat;
    private String lon;
    private String timeStamp;
    private String timeFull;

    public DataStats(int id, String statsType, String statsId, int count, String date, int sent,
                     String unitId, String campaignId, String details, String time, String lat, String lon, String timeStamp, String timeFull) {
        this.id = id;
        this.statsType = statsType;
        this.statsId = statsId;
        this.count = count;
        this.date = date;
        this.sent = sent;
        this.unitId = unitId;
        this.campaignId = campaignId;
        this.details = details;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
        this.timeStamp = timeStamp;
        this.timeFull = timeFull;
    }

    public DataStats(String statsType, String statsId, int count, String date, int sent,
                     String unitId, String campaignId, String details, String time, String lat,
                     String lon, String timeStamp, String timeFull) {
        this.statsType = statsType;
        this.statsId = statsId;
        this.count = count;
        this.date = date;
        this.sent = sent;
        this.unitId = unitId;
        this.campaignId = campaignId;
        this.details = details;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
        this.timeStamp = timeStamp;
        this.timeFull = timeFull;
    }

    public DataStats(String statsType, String statsId, String campaignId, String details) {
        this.statsType = statsType;
        this.statsId = statsId;
        this.campaignId = campaignId;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatsType() {
        return statsType;
    }

    public void setStatsType(String statsType) {
        this.statsType = statsType;
    }

    public String getStatsId() {
        return statsId;
    }

    public void setStatsId(String statsId) {
        this.statsId = statsId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTimeFull() {
        return timeFull;
    }

    public void setTimeFull(String timeFull) {
        this.timeFull = timeFull;
    }
}
