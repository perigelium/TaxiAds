package com.edisoninteractive.inrideads.Entities;


public class REST_ConfigSubItem
{
    private int error;
    private String event_id;
    //private String allContent;
    private int content_type_id;
    private long created_timestamp;
    private long modified_timestamp;
    private String file_name;
    private String path;
    private String action;
    private String command;
    private long file_id;
    private Object content;
    private String hash;

    public REST_ConfigSubItem()
    {
    }

    public REST_ConfigSubItem(String file_name, String hash, String action, String event_id )
    {
        this.hash = hash;
        this.action = action;
        this.event_id = event_id;
        this.file_name = file_name;
    }

    public String getEvent_id()
    {
        return event_id;
    }

    public String getFile_name()
    {
        return file_name;
    }

    public String getPath()
    {
        return path;
    }

    public String getAction()
    {
        return action;
    }

    public String getHash()
    {
        return hash;
    }

    public Object getContent()
    {
        return content;
    }

    public String getCommand()
    {
        return command;
    }

    public int getError()
    {
        return error;
    }

    public int getContent_type_id()
    {
        return content_type_id;
    }

    public long getCreated_timestamp()
    {
        return created_timestamp;
    }

    public long getModified_timestamp()
    {
        return modified_timestamp;
    }

    public long getFile_id()
    {
        return file_id;
    }
}
