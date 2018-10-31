package com.edisoninteractive.inrideads.Entities;

import java.util.List;

public class Params {

    public long idleInterval;
    public long revealDelay;
    public long gpsRefreshInterval;
    public boolean startInIdleMode;
    public String mainMenuBlockId;
    public String tickerText;
    public long adminPassword;
    public String interfaceWidth;
    public String interfaceHeight;
    public boolean taxiMode;
    public boolean sendScreenshotOnContact;
    public String screenshotMethod;
    public boolean checkAdTimeValidity;
    public boolean reportPositionOnContact;
    //public List<WifiConnection> wifiConnections;
    public List<StatsType> statsTypes;
    public OnEnterIdleMode onEnterIdleMode;
    public OnPowerOn onPowerOn;
    public OnPowerOff onPowerOff;
    public OnStartInPowerOffMode onStartInPowerOffMode;
    public OnShowInteractive onShowInteractive;
    public OnInterfaceCreated onInterfaceCreated;
    public boolean faceDetectionEnabled;
    public long humanPresenceThreshold;

    public Double widthRatio;
    public Double heightRatio;
}
