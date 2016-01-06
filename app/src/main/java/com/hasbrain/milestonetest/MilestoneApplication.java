package com.hasbrain.milestonetest;

import com.facebook.FacebookSdk;

import android.app.Application;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/5/16.
 */
public class MilestoneApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
    }
}
