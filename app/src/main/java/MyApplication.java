package com.cforlando.streetartandroid;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.cforlando.streetartandroid.Models.Installation;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.parse.Parse;
import com.parse.ParseObject;

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

        // Initialize Dexter Permissions Library
        Dexter.initialize(getApplicationContext());

        if (!Dexter.isRequestOngoing()) {
            MultiplePermissionsListener dialogMultiplePermissionsListener =
                    DialogOnAnyDeniedMultiplePermissionsListener.Builder
                            .withContext(getApplicationContext())
                            .withButtonText(android.R.string.ok)
                            .build();
            Dexter.checkPermissions(dialogMultiplePermissionsListener,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION);
        }



        // Enable Parse Local Datastore
        // See https://parse.com/docs/android/guide#local-datastore
        Parse.enableLocalDatastore(this);

        // Register parse models
        ParseObject.registerSubclass(Installation.class);
        Parse.initialize(this);
    }
}
