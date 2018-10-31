package com.edisoninteractive.inrideads.Interfaces;

import com.edisoninteractive.inrideads.Presenters.AolAdPlayer;

/**
 * Created by user on 19.02.2018.
 */

public interface AolPlayerStatesListener
{
    void onAolPlayerStateStopped(AolAdPlayer aolAdPlayer);

    void onAolPlayerStatsToSave(AolAdPlayer aolPlayer);
}
