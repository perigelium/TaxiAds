
package com.edisoninteractive.inrideads.Presenters;

import com.edisoninteractive.inrideads.Api.GetConfigRequest;
import com.edisoninteractive.inrideads.Api.GetConfigResponse;
import com.edisoninteractive.inrideads.Api.MyApiObservable;
import com.edisoninteractive.inrideads.Entities.ConfigDataArguments;
import com.edisoninteractive.inrideads.Interfaces.MyApiView;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Alex Angan one fine day
 */

public class MyApiPresenter extends BasePresenter<MyApiView>
{
    // Simple request: Get Config based on username
    public void getConfigObject(ConfigDataArguments configDataArguments, final boolean forcedUpdate)
    {
        if (isViewAttached())
        {
            //getView().showLoading(true);
        }

        Observable<GetConfigResponse> o = MyApiObservable.create(new GetConfigRequest(configDataArguments),
                 GetConfigResponse.class);

        sendRequest(o, new Subscriber<GetConfigResponse>()
        {
            @Override
            public void onCompleted()
            {
                if (isViewAttached())
                {
                    //getView().showLoading(false);
                    getView().onCompleted();
                }
            }

            @Override
            public void onError(Throwable e)
            {
                if (isViewAttached())
                {
                    //getView().showLoading(false);
                    getView().onError(e);
                }
            }

            @Override
            public void onNext(GetConfigResponse getConfigResponse)
            {
                if (isViewAttached())
                {
                    getView().onConfigReceived(getConfigResponse, forcedUpdate);
                }
            }
        });
    }
}
