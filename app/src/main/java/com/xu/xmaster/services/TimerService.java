package com.xu.xmaster.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.xu.xmaster.R;
import com.xu.xmaster.providers.WeatherWidgetProvider;
import com.xu.xmaster.utils.WeatherManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    private static final String TAG = "TimerService";

    private Timer myTimer;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 E");

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + myTimer);
        if (myTimer == null) {
            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateViews();
                }
            }, 0, 1000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateViews() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.weather_widget);
        views.setTextViewText(R.id.ww_time, timeFormat.format(new Date()));
        views.setTextViewText(R.id.ww_date, dateFormat.format(new Date()));
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName thisName = new ComponentName(getApplicationContext(), WeatherWidgetProvider.class);
        manager.updateAppWidget(thisName, views);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        myTimer = null;
    }
}
