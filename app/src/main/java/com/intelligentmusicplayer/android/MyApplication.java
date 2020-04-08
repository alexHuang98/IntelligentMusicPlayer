package com.intelligentmusicplayer.android;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Utils.initUtils();
    }

    public static Context getContext(){
        return context;
    }
}
