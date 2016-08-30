package com.squareup.picasso;

import android.app.Activity;
import android.os.Handler;
import android.support.test.espresso.IdlingResource;

import java.lang.ref.WeakReference;

/**
 * Utility to wait for images loaded using Picasso on espresso test.
 * Adapted from https://gist.github.com/sebaslogen/0b2fdea3f322c730e04b0af7285fcd28,
 * via http://stackoverflow.com/questions/32779549/monitoring-picasso-for-idlingresource-in-espresso
 */
public class PicassoIdlingResource implements IdlingResource {

    private static final int IDLE_POLL_DELAY_MILLIS = 100;

    protected ResourceCallback callback;

    WeakReference<Picasso> picassoWeakReference;

    public PicassoIdlingResource(Activity activity) {
        picassoWeakReference = new WeakReference<>(Picasso.with(activity));
    }

    @Override
    public String getName() {
        return "PicassoIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
        if (isIdle()) {
            notifyDone();
            return true;
        } else {
      /* Force a re-check of the idle state in a little while.
       * If isIdleNow() returns false, Espresso only polls it every few seconds which can slow down our tests.
       */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isIdleNow();
                }
            }, IDLE_POLL_DELAY_MILLIS);
            return false;
        }
    }

    public boolean isIdle() {
        return picassoWeakReference == null
                || picassoWeakReference.get() == null
                || picassoWeakReference.get().targetToAction.isEmpty();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }

    void notifyDone() {
        if (callback != null) {
            callback.onTransitionToIdle();
        }
    }
}

