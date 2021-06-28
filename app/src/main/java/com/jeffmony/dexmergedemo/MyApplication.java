package com.jeffmony.dexmergedemo;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.jeffmony.dexmergelib.DexPluginHelper;
import com.jeffmony.dexmergelib.hook.HookHelper;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            HookHelper.hookInstrumentation(base);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public Resources getResources() {
        return DexPluginHelper.getPluginResources() == null ? super.getResources() : DexPluginHelper.getPluginResources();
    }
}
