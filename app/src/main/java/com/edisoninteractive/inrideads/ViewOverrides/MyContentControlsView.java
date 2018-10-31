package com.edisoninteractive.inrideads.ViewOverrides;

import android.content.Context;
import android.support.annotation.NonNull;

import com.aol.mobile.sdk.controls.ContentControls;
import com.aol.mobile.sdk.controls.view.ContentControlsView;

/**
 * Created by Alex Angan on 23.04.2018.
 */

public class MyContentControlsView extends ContentControlsView
{
    public MyContentControlsView(@NonNull Context context)
    {
        super(context);
    }

    @Override
    public void render(@NonNull ContentControls.ViewModel vm)
    {
        vm.isSeekForwardButtonVisible = false;
        vm.isSeekBackButtonVisible = false;

        super.render(vm);
    }
}
