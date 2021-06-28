package com.jeffmony.dexmergedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jeffmony.dexmergelib.DexPluginHelper;
import com.jeffmony.dexmergelib.utils.LogUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoadDexBtn;
    private Button mLaunchDexActivityBtn;
    private Button mInvokeDexFuncBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadDexBtn = findViewById(R.id.loadDexBtn);
        mLaunchDexActivityBtn = findViewById(R.id.launchDexActivityBtn);
        mInvokeDexFuncBtn = findViewById(R.id.invokeDexFunBtn);

        mLoadDexBtn.setOnClickListener(this);
        mLaunchDexActivityBtn.setOnClickListener(this);
        mInvokeDexFuncBtn.setOnClickListener(this);

    }

    private void loadDexPlugin() {
        try {
            DexPluginHelper.loadPlugin(this, getClassLoader());
        } catch (Exception e) {
            Toast.makeText(this, "加载插件失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchDexActivity() {
        String classPath = "com.jeffmony.testdexmerge.MainActivity";
        Class dexPluginActivityClass = null;

        try {
            dexPluginActivityClass = Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            LogUtils.w("Class is not found, exception=" + e.getMessage());
        }
        if (dexPluginActivityClass == null) {
            Toast.makeText(this, "找不到 : " +classPath, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, dexPluginActivityClass);
        startActivity(intent);
    }

    private void invokeDexPluginFunction() {
        String interfacePath = "com.jeffmony.testdexmerge.listener.IFunctionInvokeListener";
        Class interfaceClass = null;

        try {
            interfaceClass = Class.forName(interfacePath);
        } catch (ClassNotFoundException e) {
            LogUtils.w("Interface is not found, exception="+e.getMessage());
        }

        if (interfaceClass == null) {
            return;
        }

        Object object = Proxy.newProxyInstance(getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                LogUtils.i("Method is : " + methodName);
                for (int i = 0; i < args.length; i++) {
                    LogUtils.i("Method args ===> [i="+i+", args="+args[i] +"]");
                }
                return null;
            }
        });

        String invokeClassPath = "com.jeffmony.testdexmerge.listener.TestFunctionInvokeListener";

        Class invokeClass = null;

        try {
            invokeClass = Class.forName(invokeClassPath);
        } catch (ClassNotFoundException e) {
            LogUtils.w("Class is not found, exception=" + e.getMessage());
        }

        if (invokeClass == null) {
            return;
        }

        Method method = null;

        try {
            method = invokeClass.getMethod("invokeFunction", String.class, Object.class);
        } catch (Exception e) {
            LogUtils.w("Invoke Function is nont found, exception="+e.getMessage());
        }

        if (method != null) {
            method.setAccessible(true);

            try {
                method.invoke(invokeClass, "test3", object);
            } catch (Exception e) {
                LogUtils.w("Method invoke failed, exception="+e.getMessage());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mLoadDexBtn) {
            loadDexPlugin();
        } else if (v == mLaunchDexActivityBtn) {
            launchDexActivity();
        } else if (v == mInvokeDexFuncBtn) {
            invokeDexPluginFunction();
        }
    }
}