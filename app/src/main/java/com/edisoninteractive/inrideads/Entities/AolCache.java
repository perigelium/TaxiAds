package com.edisoninteractive.inrideads.Entities;

/**
 * Created by Alex Angan on 20.03.2018.
 */

public class AolCache
{
    public Long tab_id;
    public Long ad_id;
    public String video_id;
    public String url;

    public AolCache(Long tab_id, Long ad_id, String video_id, String url)
    {
        this.tab_id = tab_id;
        this.ad_id = ad_id;
        this.video_id = video_id;
        this.url = url;
    }
}
