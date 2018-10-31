package com.edisoninteractive.inrideads.Api;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan one fine day
 */

public class MyApiObservable<T extends ApiResponse> implements Observable.OnSubscribe<T>
{
    private static OkHttpClient.Builder defaultHttpClient;
    private final Class<T> mApiResponse;
    private final MyApiRequest mApiRequest;
    private Call mCall;
    private Observable<T> mObservable;
    private String strResponseMessage;

    public static <E extends ApiResponse> Observable<E> create(final MyApiRequest apiRequest, Class<E> apiResponse)
    {
        MyApiObservable<E> apiObservable = new MyApiObservable<>(apiRequest, apiResponse);
        return apiObservable.getObservable();
    }

    private MyApiObservable(final MyApiRequest apiRequest, Class<T> apiResponse)
    {
        if (defaultHttpClient == null)
        {
            defaultHttpClient = new OkHttpClient.Builder();
        }

        defaultHttpClient.connectTimeout(60, TimeUnit.SECONDS);
        defaultHttpClient.readTimeout(120, TimeUnit.SECONDS);
        defaultHttpClient.writeTimeout(120, TimeUnit.SECONDS);

        mApiRequest = apiRequest;
        mApiResponse = apiResponse;
        mObservable = buildObservable();
    }


    private Observable<T> buildObservable()
    {

        return Observable.create(this).doOnUnsubscribe(unsubscribeAction).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public void call(final Subscriber<? super T> subscriber)
    {
        try
        {
            // Building
            Request request = mApiRequest.buildRequest();

            if (request == null)
            {
                Log.i(APP_LOG_TAG, getClass().getSimpleName() + "Build request failed, request == null ");
                return;
            }

            // Sending
            OkHttpClient mOkHttpClient = defaultHttpClient.build();

            mCall = mOkHttpClient.newCall(request);

            if (mCall == null)
            {
                Log.i(APP_LOG_TAG, getClass().getSimpleName() + "Build request failed, Call == null ");
                return;
            }

            mCall.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e)
                {
                    Log.i(APP_LOG_TAG, getClass().getSimpleName() + "OkHTTP onFailure: ");
                    subscriber.onError(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                {
                    strResponseMessage = response.message();
                    // Parsing
                    if (response.isSuccessful())
                    {
                        String responseStr = null;

                        try
                        {
                            responseStr = response.body().string();
                            response.body().close();

                        } catch (Exception e)
                        {
                            Log.e(APP_LOG_TAG, getClass().getSimpleName() + "onResponse: mResponse.body().string) - error");
                            e.printStackTrace();
                        }

                        T responseObj = null;

                        try
                        {
                            responseObj = mApiResponse.newInstance();
                        } catch (InstantiationException e)
                        {
                            Log.e(APP_LOG_TAG, getClass().getSimpleName() + " - onResponse: InstantiationException error");
                            e.printStackTrace();
                        } catch (IllegalAccessException e)
                        {
                            Log.e(APP_LOG_TAG, getClass().getSimpleName() + " - onResponse: IllegalAccessException error");
                            e.printStackTrace();
                        }
                        boolean parsedSuccessfully = false;
                        try
                        {
                            parsedSuccessfully = responseObj.parseResponse(responseStr);
                        } catch (Exception e)
                        {
                            Log.e(APP_LOG_TAG, getClass().getSimpleName() + " - onResponse: parseResponse error");
                            e.printStackTrace();
                        }

                        if (parsedSuccessfully)
                        {
                            subscriber.onNext(responseObj);
                            subscriber.onCompleted();

                        } else
                        {
                            Log.i(APP_LOG_TAG, getClass().getSimpleName() + " - onResponse: parseResponse not successfull");
                            subscriber.onNext(responseObj);
                            subscriber.onCompleted();
                        }

                    } else
                    {
                        Log.e(APP_LOG_TAG, getClass().getSimpleName() + " - onResponse not successful, " + strResponseMessage);
                    }
                }
            });
        } catch (Exception e)
        {
            // Catch everything
            Log.i(APP_LOG_TAG, getClass().getSimpleName() + "OkHTTP call Failure: " + e.getMessage());
        }
    }

    // Called when unsubscribing.
    Action0 unsubscribeAction = new Action0()
    {
        @Override
        public void call()
        {
            // Check if we have a running Call to cancel
            if (mCall != null && strResponseMessage == null)
            {
                Log.i(APP_LOG_TAG, "Cancelling: " + mApiRequest);
                mCall.cancel();
            }
        }
    };

    private Observable<T> getObservable()
    {
        return mObservable;
    }
}
