package com.jeffmony.dexmergelib;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.jeffmony.dexmergelib.utils.LogUtils;
import com.jeffmony.dexmergelib.utils.ReflectUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

public class DexPluginHelper {
    private static final String TAG = "DexPluginHelper";
    private static final String CLASS_DEX_PATH_LIST = "dalvik.system.DexPathList";
    private static final String FIELD_PATH_LIST = "pathList";
    private static final String FIELD_DEX_ELEMENTS = "dexElements";
    private static String apkFilePath = "";
    private static Resources sPluginResources;

    public static void loadPlugin(Context context, ClassLoader hostClassLoader)
            throws Exception {
        loadPluginClass(context, hostClassLoader);
        initPluginResource(context);
        Toast.makeText(context, "插件加载成功", Toast.LENGTH_SHORT).show();
    }

    private static void loadPluginClass(Context context,
                                        ClassLoader hostClassLoader)
            throws Exception {
        // Step1. 获取到插件apk，通常都是从网络上下载，这里为了演示，直接将插件apk
        // push到手机
        File pluginFile = context.getExternalFilesDir("plugin");
        String pluginFilePath = "";
        LogUtils.i("Plugin path : " + pluginFile.getAbsolutePath());
        if (pluginFile == null || !pluginFile.exists() || pluginFile.listFiles().length == 0) {
            Toast.makeText(context, "插件文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        pluginFilePath = pluginFile.getAbsolutePath();
        for (File file : pluginFile.listFiles()) {
            String filePath = file.getAbsolutePath();
            if (filePath.endsWith(".apk")) {
                apkFilePath = filePath;
            }
        }
        LogUtils.i("Plugin apk path : " + apkFilePath);
        if (TextUtils.isEmpty(apkFilePath)) {
            return;
        }
        pluginFile = new File(apkFilePath);
        // Step2. 创建插件的DexClassLoader
        /**
         * Note:
         * 1. 8.0.0 源码中不能将optimizedDirectory = null,自定义oat file
         * 2. 8.0.0 版本之上 optimizedDirectory 已经没有意义,可以定义为null
         */
        LogUtils.i("sdk int : " + Build.VERSION.SDK_INT);
        DexClassLoader pluginClassLoader = null;
        if (Build.VERSION.SDK_INT <= 26) {
            File optimizedFile = new File(pluginFilePath + File.separator + "opt");
            if (!optimizedFile.exists()) {
                optimizedFile.mkdir();
            }
            pluginClassLoader = new DexClassLoader(pluginFile.getAbsolutePath(),
                    optimizedFile.getAbsolutePath(),
                    null, hostClassLoader);
        } else {
            pluginClassLoader = new DexClassLoader(pluginFile.getAbsolutePath(), null,
                    null, hostClassLoader);
        }
        // Step3. 通过反射获取到pluginClassLoader中的pathList字段
        Object pluginDexPathList = ReflectUtils.getField(BaseDexClassLoader.class, pluginClassLoader, FIELD_PATH_LIST);
        // Step4. 通过反射获取到DexPathList的dexElements字段
        Object pluginElements = ReflectUtils.getField(Class.forName(CLASS_DEX_PATH_LIST), pluginDexPathList, FIELD_DEX_ELEMENTS);
        // Step5. 通过反射获取到宿主工程中ClassLoader的pathList字段
        Object hostDexPathList = ReflectUtils.getField(BaseDexClassLoader.class, hostClassLoader, FIELD_PATH_LIST);
        // Step6. 通过反射获取到宿主工程中DexPathList的dexElements字段
        Object hostElements = ReflectUtils.getField(Class.forName(CLASS_DEX_PATH_LIST), hostDexPathList, FIELD_DEX_ELEMENTS);
        // Step7. 将插件ClassLoader中的dexElements合并到宿主ClassLoader的dexElements
        Object array = combineArray(hostElements, pluginElements);
        // Step8. 将合并的dexElements设置到宿主ClassLoader
        ReflectUtils.setField(Class.forName(CLASS_DEX_PATH_LIST), hostDexPathList, FIELD_DEX_ELEMENTS, array);
    }

    private static Object combineArray(Object hostElements, Object pluginElements) {
        Class<?> componentType = hostElements.getClass().getComponentType();
        int i = Array.getLength(hostElements);
        int j = Array.getLength(pluginElements);
        int k = i + j;
        Object result = Array.newInstance(componentType, k);
        System.arraycopy(pluginElements, 0, result, 0, j);
        System.arraycopy(hostElements, 0, result, j, i);
        return result;
    }

    public static void initPluginResource(Context context) throws Exception {
        Class<AssetManager> clazz = AssetManager.class;
        AssetManager assetManager = clazz.newInstance();
        Method method = clazz.getMethod("addAssetPath", String.class);
        if (TextUtils.isEmpty(apkFilePath)) {
            return;
        }
        method.invoke(assetManager, apkFilePath);
        sPluginResources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

    public static Resources getPluginResources() { return sPluginResources; }
}
