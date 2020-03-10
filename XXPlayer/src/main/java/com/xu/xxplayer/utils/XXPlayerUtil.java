package com.xu.xxplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;

import com.xu.xxplayer.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class XXPlayerUtil {

    public static String stringForTime(long millisecond) {
        long second = millisecond / 1000;
        long hh = second / 3600;
        long mm = second % 3600 / 60;
        long ss = second % 60;
        String str = "00:00";
        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        return str;
    }

    public static String getSpeedString(double speed) {
        String speedString;
        DecimalFormat showFloatFormat = new DecimalFormat("0.0");
        if (speed >= 1048576d) {
            speedString = showFloatFormat.format(speed / 1048576d) + " MB/s";
        } else if (speed >= 1024d) {
            speedString = showFloatFormat.format(speed / 1024d) + " KB/s";
        } else {
            speedString = showFloatFormat.format(speed) + " B/s";
        }
        return speedString;
    }

    public static String getMemoryString(long memory) {
        String memoryString;
        DecimalFormat showFloatFormat = new DecimalFormat("0.0");
        if (memory >= 1073741824d) {
            memoryString = showFloatFormat.format(memory / 1073741824d) + " GB";
        } else if (memory >= 1048576d) {
            memoryString = showFloatFormat.format(memory / 1048576d) + " MB";
        } else if (memory >= 1024d) {
            memoryString = showFloatFormat.format(memory / 1024d) + " KB";
        } else {
            memoryString = showFloatFormat.format(memory) + " B";
        }
        return memoryString;
    }

    public static void saveBitmap(final Context context, final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File mDir = new File(Constant.FileSavePath);
                if (!mDir.exists()) {
                    mDir.mkdir();
                }
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String fileName = "xxplayer-" + sdf.format(calendar.getTime()) + ".jpg";
                try {
                    File file = new File(Constant.FileSavePath, fileName);
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    //发送广播通知更新数据库
                    Uri uri = Uri.fromFile(file);
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)  。
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 。
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素)  。
     */
    public static int sp2px(Context context, float value) {
        Resources r;
        if (context == null) {
            r = Resources.getSystem();
        } else {
            r = context.getResources();
        }
        float spvalue = value * r.getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp 。
     */
    public static int px2sp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }
}
