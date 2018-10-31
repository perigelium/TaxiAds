
package com.edisoninteractive.inrideads.Presenters;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Alex Angan one fine day
 */

public abstract class BasePresenter<V> {

    private V mView;
    private CompositeSubscription mCompositeSubscription;


    public BasePresenter() {
        mCompositeSubscription = new CompositeSubscription();
    }

    /**
     * Executes request by subscribing to the Observable.
     * Also adding the subscription into CompositeSubscription.
     * @param observable Observable created from ApiObservable.create()
     * @param subscriber Subscriber to subscribe
     */
    protected <T> void sendRequest(Observable<T> observable, Subscriber<T> subscriber) {

        Subscription subscription = observable.subscribe(subscriber);
        mCompositeSubscription.add(subscription);
    }

    public void attachView (V view) {
        mView = view;
    }

    /**
     * Called to unsubscribe form all "live" subscriptions.
     */
    public void releaseView() {
        mView = null;
        mCompositeSubscription.unsubscribe();
    }

    public boolean isViewAttached() {
        return mView != null;
    }

    public V getView() {
        return mView;
    }
}
