package com.example.charlie.test;

import android.app.Application;

// What is wrong with Android??
public class MyApplication extends Application {
    private static MyApplication _instance;

    public MyApplication() {
        _instance = this;
    }

    public static MyApplication get_instance() {
        return _instance;
    }
}
