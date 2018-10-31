package com.edisoninteractive.inrideads.Entities;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.edisoninteractive.inrideads.BR;


public class DisplayDataCounters extends BaseObservable
{
    @Bindable
    private String facesStatus;

    public DisplayDataCounters(String facesStatus)
    {
        this.facesStatus = facesStatus;

    }

    public String getFacesStatus()
    {
        return this.facesStatus;
    }

    public void setFacesStatus(boolean facePresent)
    {
        if(facePresent)
        {
            this.facesStatus = "Face";
        }
        else
        {
            this.facesStatus = "Nobody";
        }
        notifyPropertyChanged(BR.facesStatus);
    }
}
