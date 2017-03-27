package com.hm.okhttpdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by dumingwei on 2017/3/27.
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getInstance() {
        return context;
    }
}
