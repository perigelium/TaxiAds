package com.edisoninteractive.inrideads.Presenters;

import android.widget.FrameLayout;

import com.edisoninteractive.inrideads.Entities.Tab;

/**
 * Created by Alex Angan on 29.01.2018.
 */

public abstract class TabContentRenderer
{
    protected FrameLayout frameLayout;
    protected Tab tab;

    protected TabContentRenderer(FrameLayout frameLayout, Tab tab)
    {
        this.frameLayout = frameLayout;
        this.tab = tab;

    }

    abstract void startRendering();
    abstract void stopRendering();
}
