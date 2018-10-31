package com.edisoninteractive.inrideads.Interfaces;

import android.app.Fragment;

/**
 * Created by Alex Angan one fine day
 */

public interface Communicator
{
    void replaceFragment(String fragmentTag, Fragment newFragment);

    void reloadFragment(String fragmentTag);

    void closeApp();
}