package com.xu.xmaster.utils.net;

public class OkHttpUtil {

    private static OkHttpUtil sInstance = new OkHttpUtil();

    private INetManager mNetManager = new OkHttpNetManager();

    public INetManager getNetManager() {
        return mNetManager;
    }

    public static OkHttpUtil getInstance() {
        return sInstance;
    }
}
