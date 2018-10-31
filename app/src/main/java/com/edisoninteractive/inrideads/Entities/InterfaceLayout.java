package com.edisoninteractive.inrideads.Entities;

import java.util.List;

public class InterfaceLayout {

    public String id;
    public String type;
    public long statsId;
    public String x;
    public String y;
    public long width;
    public long height;
    public long depthLevel;
    public String contentScaleMode;
    public String backgroundScaleMode;
    public boolean canInteract;
    public boolean visible;
    public String backgroundColor;
    public List<Child> children = null;
    public long adTypeId;
    public boolean autoStart;
    public OnLongPress onLongPress;
    public String backgroundImage;
    public String upStateImageURL;
    public String downStateImageURL;
    public OnPress onPress;
    public OnSelect onSelect;
    public OnUnselect onUnselect;
    public String upStateColor;
    public String downStateColor;

    public boolean toggled;

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public long getStatsId()
    {
        return statsId;
    }

    public String getX()
    {
        return x;
    }

    public String getY()
    {
        return y;
    }

    public long getWidth()
    {
        return width;
    }

    public long getHeight()
    {
        return height;
    }

    public long getDepthLevel()
    {
        return depthLevel;
    }

    public String getContentScaleMode()
    {
        return contentScaleMode;
    }

    public String getBackgroundScaleMode()
    {
        return backgroundScaleMode;
    }

    public boolean isCanInteract()
    {
        return canInteract;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public String getBackgroundColor()
    {
        return backgroundColor;
    }

    public List<Child> getChildren()
    {
        return children;
    }

    public long getAdTypeId()
    {
        return adTypeId;
    }

    public boolean isAutoStart()
    {
        return autoStart;
    }

    public OnLongPress getOnLongPress()
    {
        return onLongPress;
    }

    public String getBackgroundImage()
    {
        return backgroundImage;
    }

    public String getUpStateImageURL()
    {
        return upStateImageURL;
    }

    public String getDownStateImageURL()
    {
        return downStateImageURL;
    }

    public OnPress getOnPress()
    {
        return onPress;
    }

    public OnSelect getOnSelect()
    {
        return onSelect;
    }

    public OnUnselect getOnUnselect()
    {
        return onUnselect;
    }
}
