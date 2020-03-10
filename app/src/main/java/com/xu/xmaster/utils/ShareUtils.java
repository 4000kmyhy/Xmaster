package com.xu.xmaster.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.xu.xxplayer.players.BasePlayerView;

public class ShareUtils {

    /**
     * 第一次打开应用，显示欢迎页
     *
     * @param context
     * @param isFirst
     */
    public static void setFirstLoading(Context context, boolean isFirst) {
        context.getSharedPreferences("splash", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(getVersionName(context), isFirst)
                .apply();
    }

    public static boolean isFirstLoading(Context context) {
        return context.getSharedPreferences("splash", Context.MODE_PRIVATE)
                .getBoolean(getVersionName(context), true);
    }

    /**
     * 获取应用版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("ShareUtils", "getVersionName: " + versionName);
        return versionName;
    }

    /**
     * 设置天气主题
     *
     * @param context
     * @param isNight
     */
    public static void setNight(Context context, boolean isNight) {
        context.getSharedPreferences("weather_theme", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("theme", isNight)
                .apply();
    }

    public static boolean isNight(Context context) {
        return context.getSharedPreferences("weather_theme", Context.MODE_PRIVATE)
                .getBoolean("theme", false);
    }

    /**
     * 设置当前城市
     *
     * @param context
     * @param cityname
     */
    public static void setCityName(Context context, String cityname) {
        context.getSharedPreferences("weather_cityname", Context.MODE_PRIVATE)
                .edit()
                .putString("cityname", cityname)
                .apply();
    }

    public static String getCityName(Context context) {
        return context.getSharedPreferences("weather_cityname", Context.MODE_PRIVATE)
                .getString("cityname", "auto_ip");
    }

    /**
     * 设置音乐模式
     *
     * @param context
     * @param mode
     */
    public static void setMusicMode(Context context, int mode) {
        context.getSharedPreferences("music_mode", Context.MODE_PRIVATE)
                .edit()
                .putInt("mode", mode)
                .apply();
    }

    public static int getMusicMode(Context context) {
        return context.getSharedPreferences("music_mode", Context.MODE_PRIVATE)
                .getInt("mode", BasePlayerView.PLAY_MODE_LIST_LOOP);
    }
}
