package com.jeffmony.dexmergelib.utils;

import android.util.Log;

public class LogUtils {
    private static final boolean isDebug = true;
    private static final String TAG = "DexMergeLib";
    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, msg);
        }
    }
    public static void w(String msg) {
        if (isDebug) {
            Log.w(TAG, msg);
        }
    }
    public static void i(String msg) {
        if (isDebug) {
            Log.i(TAG, msg);
        }
    }
    public static void v(String msg) {
        if (isDebug) {
            Log.v(TAG, msg);
        }
    }
}