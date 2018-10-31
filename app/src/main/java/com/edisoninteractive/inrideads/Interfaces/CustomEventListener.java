package com.edisoninteractive.inrideads.Interfaces;


import com.edisoninteractive.inrideads.Entities.Command;

import java.util.List;

/**
 * Created by Alex Angan one fine day
 */

public interface CustomEventListener
{
    void processEvent(String eventType, List<Command> command);
}
