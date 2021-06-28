package com.jeffmony.testdexmerge.listener;

public class TestFunctionInvokeListener {

    public static void invokeFunction(String method, Object object) {
        //这儿需要在上层调用的时候指定CallbackListener 类型
        IFunctionInvokeListener listener = (IFunctionInvokeListener)object;
        if ("test1".equals(method)) {
            listener.test1();
        } else if ("test2".equals(method)) {
            listener.test2(100);
        } else if ("test3".equals(method)) {
            listener.test3("callFunction--->test3 is okay");
        }
    }
}
