package com.xu.xmaster.utils;

import android.text.TextUtils;

import com.xu.xmaster.Constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WeatherManager {

    public static final String[] weather_night_code = {
            "100", "103", "104",
            "300", "301",
            "406", "407",
    };

    public static boolean hasNight(String code) {
        for (String nightCode : weather_night_code) {
            if (TextUtils.equals(code, nightCode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNight(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 18) {
            return false;
        } else {
            return true;
        }
    }
}
