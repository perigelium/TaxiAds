package com.edisoninteractive.inrideads.Entities;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.edisoninteractive.inrideads.BR;


public class Counters extends BaseObservable
{
    @Bindable
    public int mMBytesInStorageAvailable;
    @Bindable
    public int objectsToDownload;
    @Bindable
    public int objectsSkipped;
    @Bindable
    public int objectsFailed;
    @Bindable
    public int objectsReceived;
    @Bindable
    public int objectsWithContent;
    @Bindable
    public int objectsAlreadyExist;
    @Bindable
    public int commandsQuant;
    @Bindable
    public int eventsSentSuccesfully;
    @Bindable
    public int eventsSendFailed;
    @Bindable
    public int callsSent;
    @Bindable
    public int activeCalls;
    @Bindable
    public int getConfigTime;
    @Bindable
    public int getFilesTime;
    @Bindable
    public int totalObjectCount;
    @Bindable
    public String unitId;
    @Bindable
    public int stateRequestingContent;
    @Bindable
    public int stateDownloadingContent;
    @Bindable
    public int stateContentUpdateFailed;

    public Counters()
    {
        this.mMBytesInStorageAvailable = 0;
        this.objectsToDownload = 0;
        this.objectsSkipped = 0;
        this.objectsFailed = 0;
        this.objectsReceived = 0;
        this.objectsWithContent = 0;
        this.objectsAlreadyExist = 0;
        this.commandsQuant = 0;
        this.eventsSentSuccesfully = 0;
        this.eventsSendFailed = 0;
        this.callsSent = 0;
        this.getConfigTime = 0;
        this.getFilesTime = 0;
        this.totalObjectCount = 0;
        setStateRequestingContent(false);
        setSyncRequestSucceeded(true);
        setStateDownloadingContent(false);
    }

    public void reset()
    {
        mMBytesInStorageAvailable = 0;
        objectsToDownload = 0;
        objectsSkipped = 0;
        objectsFailed = 0;
        objectsReceived = 0;
        objectsWithContent = 0;
        objectsAlreadyExist = 0;
        commandsQuant = 0;
        callsSent = 0;
        eventsSentSuccesfully = 0;
        eventsSendFailed = 0;
        activeCalls = 0;
        getConfigTime = 0;
        getFilesTime = 0;
        totalObjectCount = 0;
        setStateRequestingContent(false);
        setSyncRequestSucceeded(true);
        setStateDownloadingContent(false);
    }

/*    @Bindable
    public String getUnitId()
    {
        return unitId;
    }*/


/*    public String getOnlineStatusString()
    {
        return strOnlineStatus;
    }*/

/*    @Bindable
    public int getTotalObjectCount()
    {
        return totalObjectCount;
    }

    @Bindable
    public int getGetConfigTime()
    {
        return getConfigTime;
    }

    @Bindable
    public int getGetFilesTime()
    {
        return getFilesTime;
    }


    public int getmBytesInStorageAvailable()
    {
        return mMBytesInStorageAvailable;
    }

    @Bindable
    public int getObjectsToDownload()
    {
        return this.objectsToDownload;
    }

    @Bindable
    public int getObjectsSkipped()
    {
        return objectsSkipped;
    }
    @Bindable
    public int getObjectsFailed()
    {
        return objectsFailed;
    }
    @Bindable
    public int getObjectsReceived()
    {
        return objectsReceived;
    }
    @Bindable
    public int getObjectsWithContent()
    {
        return objectsWithContent;
    }
    @Bindable
    public int getObjectsAlreadyExist()
    {
        return objectsAlreadyExist;
    }
    @Bindable
    public int getCommandsQuant()
    {
        return commandsQuant;
    }
    @Bindable
    public int getEventsSentSuccesfully()
    {
        return eventsSentSuccesfully;
    }
    @Bindable
    public int getEventsSendFailed()
    {
        return eventsSendFailed;
    }
    @Bindable
    public int getCallsSent()
    {
        return callsSent;
    }*/

    public void setTotalObjectCount(int totalObjectCount)
    {
        this.totalObjectCount = totalObjectCount;
        notifyPropertyChanged(BR.totalObjectCount);
    }

    public void setmBytesInStorageAvailable(int mMBytesInStorageAvailable)
    {
        this.mMBytesInStorageAvailable = mMBytesInStorageAvailable;
        notifyPropertyChanged(BR.mBytesInStorageAvailable);
    }

    public void incObjectsSkipped()
    {
        this.objectsSkipped++;
        notifyPropertyChanged(BR.objectsSkipped);
    }

    public void incObjectsFailed()
    {
        this.objectsFailed++;
        notifyPropertyChanged(BR.objectsFailed);
    }

    public void incObjectsReceived()
    {
        this.objectsReceived++;
        notifyPropertyChanged(BR.objectsReceived);
    }

    public void incObjectsWithContent()
    {
        this.objectsWithContent++;
        notifyPropertyChanged(BR.objectsWithContent);
    }

    public void incObjectsAlreadyExist()
    {
        this.objectsAlreadyExist++;
        notifyPropertyChanged(BR.objectsAlreadyExist);
    }

    public void incCommandsQuant()
    {
        this.commandsQuant++;
        notifyPropertyChanged(BR.commandsQuant);
    }

    public void incEventsSentSuccesfully()
    {
        this.eventsSentSuccesfully++;
        notifyPropertyChanged(BR.eventsSentSuccesfully);
    }

    public void incEventsSendFailed()
    {
        this.eventsSendFailed++;
        notifyPropertyChanged(BR.eventsSendFailed);
    }

    public void incCallsSent()
    {
        this.callsSent++;
        notifyPropertyChanged(BR.callsSent);
    }

    public void decObjectsToDownload()
    {
        this.objectsToDownload--;
        notifyPropertyChanged(BR.objectsToDownload);
    }

    public void setObjectsToDownload(int objectsToDownload)
    {
        this.objectsToDownload = objectsToDownload;
        notifyPropertyChanged(BR.objectsToDownload);
    }

    public void incGetConfigTime()
    {
        this.getConfigTime++;
        notifyPropertyChanged(BR.getConfigTime);
    }

    public void incGetFilesTime()
    {
        this.getFilesTime++;
        notifyPropertyChanged(BR.getFilesTime);
    }

    public void setUnitId(String unitId)
    {
        this.unitId = unitId;
        notifyPropertyChanged(BR.unitId);
    }

    public void setSyncRequestSucceeded(boolean syncRequestSucceeded)
    {
        this.stateContentUpdateFailed = syncRequestSucceeded ? 8 : 0;
        notifyPropertyChanged(BR.stateContentUpdateFailed);
    }

    public void setStateRequestingContent(boolean stateRequestingContent)
    {
        this.stateRequestingContent = stateRequestingContent ? 0 : 8;
        notifyPropertyChanged(BR.stateRequestingContent);
    }

    public void setStateDownloadingContent(boolean stateDownloadingContent)
    {
        this.stateDownloadingContent = stateDownloadingContent ? 0 : 8;
        notifyPropertyChanged(BR.stateDownloadingContent);
    }

/*    0 is for VISIBLE
    4 is for INVISIBLE
    8 is for GONE*/
}
