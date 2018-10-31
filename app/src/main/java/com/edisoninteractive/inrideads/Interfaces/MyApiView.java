package com.edisoninteractive.inrideads.Interfaces;

import com.edisoninteractive.inrideads.Api.GetConfigResponse;

/**
 * Created by Alex Angan one fine day
 */

public interface MyApiView {

    void onConfigReceived(GetConfigResponse getConfigResponse, boolean forcedUpdate);

    void onError(Throwable e);

    void onCompleted();

    //void showLoading(boolean show);
}
