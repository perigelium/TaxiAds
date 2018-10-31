package com.edisoninteractive.inrideads.Entities;

public class Response
{
    public long adId;
    public long adType;
    public long campaignId;
    public String adFile;
    public long adOrder;
    public long duration;
    public String dateFrom;
    public String dateTo;
    public String timeFrom;
    public String timeTo;
    public String adTitle;
    public String adAddress;
    public String interactFile;
    public Integer interactiveId;
    public Boolean forFace;
    public GeoBinding geoBinding;

    public long nextTimePlayAllowed;
    public Boolean isPlaylist;
    public String playList;

    public void setNextTimePlayAllowed(long nextTimePlayAllowed)
    {
        this.nextTimePlayAllowed = nextTimePlayAllowed;
    }
}
