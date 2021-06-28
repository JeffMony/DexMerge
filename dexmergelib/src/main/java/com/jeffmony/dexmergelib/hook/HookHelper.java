package com.jeffmony.dexmergelib.hook;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;

import com.jeffmony.dexmergelib.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookHelper {
    public static final String TARGET_INTENT = "target_intent";

    public static void hookAMS() throws Exception {
        Object singleton;
        if (Build.VERSION.SDK_INT >= 26) {
            Class<?> clazz = Class.forName("android.app.ActivityManager");
            singleton =
                    ReflectUtils.getField(clazz, null, "IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            singleton = ReflectUtils.getField(activityManagerNativeClass, null, "gDefault");
        }
        Class<?> singletonClass = Class.forName("android.util.Singleton");
        Method getMethod = singletonClass.getMethod("get");
        Object iActivityManager = getMethod.invoke(singleton);
        Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[] {iActivityManagerClass}, new IActivityManagerProxy(iActivityManager));
        ReflectUtils.setField(singletonClass, singleton, "mInstance", proxy);
    }

    public static void hookInstrumentation(Context context) throws Exception {
        Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
        Object activityThread = ReflectUtils.getField(contextImplClass, context, "mMainThread");
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Object mInstrumentation = ReflectUtils.getField(activityThreadClass, activityThread, "mInstrumentation");
        ReflectUtils.setField(activityThreadClass, activityThread, "mInstrumentation",
                new InstrumentationProxy((Instrumentation)mInstrumentation, context.getPackageManager()));
    }
}
