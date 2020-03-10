package com.xu.xmaster.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.xu.xmaster.R;
import com.xu.xmaster.activities.WeatherActivity;
import com.xu.xmaster.services.TimerService;
import com.xu.xmaster.utils.ShareUtils;
import com.xu.xmaster.utils.WeatherManager;

import java.util.concurrent.ExecutionException;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeatherWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WeatherWidgetProvider";
    public static final String ACTION_WIDGET_UPDATE = "com.xu.xmaster.widget.weather.update";
    private static final int WIDGET_CODE = 3;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled: ");
        //第一个被添加
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(TAG, "onUpdate: ");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setOnClickPendingIntent(R.id.ww_layout, startActivity(context));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        initWea(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "onDisabled: ");
        //最后一个被移除
        context.getApplicationContext().stopService(new Intent(context.getApplicationContext(), TimerService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive: " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_WIDGET_UPDATE:
                initWea(context);
                break;
        }
    }

    private void initWea(Context context) {
        context.getApplicationContext().startService(new Intent(context.getApplicationContext(), TimerService.class));
        String cityname = ShareUtils.getCityName(context);
        HeWeather.getWeatherNow(context,
                cityname,
                Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC,
                new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(Now now) {
                        if (now.getStatus().equals(Code.OK.getCode())) {
                            NowBase nowBase = now.getNow();

                            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                            views.setTextViewText(R.id.ww_location, now.getBasic().getLocation());
                            views.setTextViewText(R.id.ww_cond_txt, nowBase.getCond_txt());
                            views.setTextViewText(R.id.ww_tmp, nowBase.getTmp() + "°C");
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            ComponentName componentName = new ComponentName(context, WeatherWidgetProvider.class);
                            appWidgetManager.updateAppWidget(componentName, views);

                            String condUrl;
                            if (WeatherManager.isNight(now.getUpdate().getLoc())
                                    && WeatherManager.hasNight(nowBase.getCond_code())) {//当前时间是晚上且存在晚上icon
                                condUrl = "file:///android_asset/" + nowBase.getCond_code() + "n.png";
                            } else {
                                condUrl = "file:///android_asset/" + nowBase.getCond_code() + ".png";
                            }
                            new GetBitmapAsyncTask(context).execute(condUrl);
                        }
                    }
                });
    }

    private class GetBitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private Context context;

        public GetBitmapAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(context)
                        .asBitmap()
                        .load(strings[0])
                        .placeholder(R.drawable.h999)
                        .submit()
                        .get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                views.setImageViewBitmap(R.id.ww_cond, bitmap);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, WeatherWidgetProvider.class);
                appWidgetManager.updateAppWidget(componentName, views);
            }
        }
    }

    private PendingIntent startActivity(Context context) {
        return PendingIntent.getActivity(context,
                WIDGET_CODE,
                new Intent(context, WeatherActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
