package com.edisoninteractive.inrideads.Entities;

import okhttp3.Call;

/**
 * Created by user on 23.06.2017.
 */

public class ObjCallAttrs
{
    Call call;
    String pathSuffix;
    String fileName;
    String fileHash;
    String eventId;
    boolean downloadToTmp;

    public ObjCallAttrs(Call call, String pathSuffix, String fileName, String fileHash, String eventId, boolean downloadToTmp)
    {
        this.call = call;
        this.pathSuffix = pathSuffix;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.eventId = eventId;
        this.downloadToTmp = downloadToTmp;
    }

    public Call getCall()
    {
        return call;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getPathSuffix()
    {
        return pathSuffix;
    }

    public String getEventId()
    {
        return eventId;
    }

    public boolean isDownloadToTmp()
    {
        return downloadToTmp;
    }

    public String getFileHash()
    {
        return fileHash;
    }
}
