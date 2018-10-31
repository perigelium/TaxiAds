package com.edisoninteractive.inrideads.EventHandlers;

import android.view.MotionEvent;
import android.view.View;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DEBOUNCED_TOUCH_EVENTS_ENABLED;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DEBOUNCED_TOUCH_THRESHOLD;

/**
 * Created by Alex Angan on 22.05.2018.
 */

public abstract class OnTouchDebouncedListener implements View.OnTouchListener
{
    private long lastTouchUnixTime;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        boolean result = true;

        if(DEBOUNCED_TOUCH_EVENTS_ENABLED)
        {
            long now = System.currentTimeMillis();

            if (now - lastTouchUnixTime > DEBOUNCED_TOUCH_THRESHOLD && (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_UP))
            {
                result = onTouched(view, motionEvent);
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                lastTouchUnixTime = now;
            }
        }

        return result;
    }

    public abstract boolean onTouched(View view, MotionEvent motionEvent);
}
