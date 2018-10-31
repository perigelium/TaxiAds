package com.edisoninteractive.inrideads.Interfaces;

/**
 * Created by user on 18.03.2018.
 */

public interface BoundServiceListener
{
    public void sendProgress(double progress);

    public void finishedDownloading();
}
