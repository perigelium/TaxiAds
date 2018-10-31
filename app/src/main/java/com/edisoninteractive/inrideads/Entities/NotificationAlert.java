package com.edisoninteractive.inrideads.Entities;

/**
 * Created by Alex Angan on 22.08.2018.
 */

public class NotificationAlert
{
    public String unit_id;
    public String uuid;
    public Long unix_time_stamp;
    public String local_date_time;

    public Integer priority; // 0 - (i)nfo, 1 - (w)arning, 2 - (e)rror, 3 - (wtf) what a terrible failure
    public String message_body;
    public String description;

    public NotificationAlert(String unit_id, String uuid, Long unix_time_stamp, String local_date_time,
                             Integer priority, String message_body, String description)
    {
        this.unit_id = unit_id;
        this.uuid = uuid;
        this.unix_time_stamp = unix_time_stamp;
        this.local_date_time = local_date_time;
        this.priority = priority;
        this.message_body = message_body;
        this.description = description;
    }
}
