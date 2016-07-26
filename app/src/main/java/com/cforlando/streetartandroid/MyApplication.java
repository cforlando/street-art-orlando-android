package com.cforlando.streetartandroid;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.cforlando.streetartandroid.Models.Installation;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

import pl.tajchert.nammu.Nammu;

/**
 * Created by benba on 2/3/2016.
 */
public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize Nammu Permissions Helper
        Nammu.init(getApplicationContext());

        // Enable Parse Local Datastore
        Parse.enableLocalDatastore(this);

        // Register parse models
        ParseObject.registerSubclass(Installation.class);
        Parse.initialize(this);

        // Initialize ParseFacebookUtils
        ParseFacebookUtils.initialize(this);

    }
}
