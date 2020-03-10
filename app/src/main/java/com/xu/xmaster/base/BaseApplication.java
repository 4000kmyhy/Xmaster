package com.xu.xmaster.base;

import android.app.Application;
import android.content.Intent;

import com.xu.xmaster.Constant;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        HeConfig.init(Constant.weatherUsername, Constant.weatherKey);
        HeConfig.switchToFreeServerNode();//切换到免费服务域名
    }
}
