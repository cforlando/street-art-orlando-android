package com.cforlando.streetartandroid;

import android.os.Bundle;
import android.os.PersistableBundle;

import com.parse.ui.ParseLoginDispatchActivity;


/**
 * Created by benba on 3/23/2016.
 */
public class LoginActivity extends ParseLoginDispatchActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }
}
