package com.edisoninteractive.inrideads.Entities;

import java.util.List;

public class Child {

    public String id;
    public int statsId;
    public String type;
    public boolean canInteract;
    public long x;
    public long y;
    public String topImageURL;
    public String upStateImageURL;
    public String downStateImageURL;
    public List<Child_> children = null;
    public OnPress onPress;
    public int campaignId;
    public String upStateColor;
    public String downStateColor;
    public long hitAreaWidth;
    public long hitAreaHeight;

    public long buttonHeight;
    public long buttonWidth;

    public long height;
    public long width;
}
