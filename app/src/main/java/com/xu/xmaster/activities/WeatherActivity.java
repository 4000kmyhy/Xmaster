package com.xu.xmaster.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czp.library.ArcProgress;
import com.czp.library.OnTextCenter;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.ForecastAdapter;
import com.xu.xmaster.adapters.HourlyAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.beans.ForecastBean;
import com.xu.xmaster.beans.HourlyBean;
import com.xu.xmaster.fragments.CityFragment;
import com.xu.xmaster.fragments.ForecastFragment;
import com.xu.xmaster.fragments.HourlyFragment;
import com.xu.xmaster.providers.WeatherWidgetProvider;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.PixelUtils;
import com.xu.xmaster.utils.ShareUtils;
import com.xu.xmaster.utils.WeatherManager;
import com.xu.xmaster.views.KeyValueView;
import com.xu.xmaster.views.SimpleToolbar;
import com.xu.xmaster.views.WeatherBackgroundView;

import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNowCity;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.Hourly;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeatherActivity extends BaseActivity {

    private static final String TAG = "WeatherActivity";

    private WeatherBackgroundView wbv;
    private SimpleToolbar toolbar;
    private SmartRefreshLayout refreshLayout;
    private RelativeLayout layout_now;
    private LinearLayout layout_theme;
    private TextView tv_theme;
    //实况天气
    private ImageView iv_cond;
    private TextView tv_tmp, tv_cond;
    //逐小时
    private RecyclerView rv_hourly;
    //七天预报
    private RecyclerView rv_forecast;
    //空气质量
    private ArcProgress ap_aqi;
    private KeyValueView kvv_qlty, kvv_pm10, kvv_pm25, kvv_no2, kvv_so2, kvv_co, kvv_o3;
    //舒适度
    private ArcProgress ap_hum;
    private KeyValueView kvv_fl, kvv_pcpn, kvv_pres;
    //风力风速
    private ImageView iv_wind;
    private KeyValueView kvv_wind_dir, kvv_wind_deg, kvv_wind_sc, kvv_wind_spd;

    private String cityname = "auto_ip";
    private HourlyAdapter hourlyAdapter;
    private ForecastAdapter forecastAdapter;
    private boolean isFinishNow, isFinishAir, isFinishHourly, isFinishForecast;//接口请求完成
    private boolean isFailed, isFailedNow, isFailedAir, isFailedHourly, isFailedForecast;//接口请求失败

    //序列化列表
    private List<HourlyBean> hourlyList;
    private List<ForecastBean> forecastList;

    private ObjectAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_weather);

        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        hourlyAdapter.setOnItemClickListener(new HourlyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                        .replace(R.id.container, HourlyFragment.newInstance(hourlyList, position))
                        .addToBackStack(null)
                        .commit();
            }
        });

        forecastAdapter.setOnItemClickListener(new ForecastAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                        .replace(R.id.container, ForecastFragment.newInstance(forecastList, position))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void initView() {
        wbv = findViewById(R.id.wbv);
        toolbar = findViewById(R.id.toolbar);
        refreshLayout = findViewById(R.id.refreshLayout);
        layout_now = findViewById(R.id.layout_now);
        layout_theme = findViewById(R.id.layout_theme);
        tv_theme = findViewById(R.id.tv_theme);

        //实况天气
        iv_cond = findViewById(R.id.iv_cond);
        tv_tmp = findViewById(R.id.tv_tmp);
        tv_cond = findViewById(R.id.tv_cond);

        //空气质量
        ap_aqi = findViewById(R.id.ap_aqi);
        ap_aqi.setOnCenterDraw(new OnTextCenter(Color.parseColor("#ffffff"), PixelUtils.sp2px(getContext(), 20)));
        kvv_qlty = findViewById(R.id.kvv_qlty);
        kvv_pm10 = findViewById(R.id.kvv_pm10);
        kvv_pm25 = findViewById(R.id.kvv_pm25);
        kvv_no2 = findViewById(R.id.kvv_no2);
        kvv_so2 = findViewById(R.id.kvv_so2);
        kvv_co = findViewById(R.id.kvv_co);
        kvv_o3 = findViewById(R.id.kvv_o3);

        //逐小时
        rv_hourly = findViewById(R.id.rv_hourly);
        hourlyAdapter = new HourlyAdapter(getContext());
        rv_hourly.setAdapter(hourlyAdapter);

        //七天预报
        rv_forecast = findViewById(R.id.rv_forecast);
        forecastAdapter = new ForecastAdapter(getContext());
        rv_forecast.setAdapter(forecastAdapter);

        //舒适度
        ap_hum = findViewById(R.id.ap_hum);
        ap_hum.setOnCenterDraw(new ArcProgress.OnCenterDraw() {
            @Override
            public void draw(Canvas canvas, RectF rectF, float x, float y, float storkeWidth, int progress) {
                Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setStrokeWidth(35);
                textPaint.setTextSize(PixelUtils.sp2px(getContext(), 20));
                textPaint.setColor(Color.parseColor("#ffffff"));
                String progressStr = String.valueOf(progress) + "%";
                float textX = x - (textPaint.measureText(progressStr) / 2);
                float textY = y - ((textPaint.descent() + textPaint.ascent()) / 2);
                canvas.drawText(progressStr, textX, textY, textPaint);
            }
        });
        kvv_fl = findViewById(R.id.kvv_fl);
        kvv_pcpn = findViewById(R.id.kvv_pcpn);
        kvv_pres = findViewById(R.id.kvv_pres);

        //风力风速
        iv_wind = findViewById(R.id.iv_wind);
        kvv_wind_dir = findViewById(R.id.kvv_wind_dir);
        kvv_wind_deg = findViewById(R.id.kvv_wind_deg);
        kvv_wind_sc = findViewById(R.id.kvv_wind_sc);
        kvv_wind_spd = findViewById(R.id.kvv_wind_spd);

        initToolbar();
        initRefresh();
        initTheme();

        animator = ObjectAnimator.ofFloat(iv_wind, "rotation", 0f, 360f);
        animator.setDuration(5000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();
        toolbar.setTitleGravity(Gravity.CENTER);

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.setRightBtn1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout.finishRefresh();
                CityFragment cityFragment = CityFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                        .replace(R.id.container, cityFragment)
                        .addToBackStack(null)
                        .commit();

                cityFragment.setOnCitySelectedListener(new CityFragment.OnCitySelectedListener() {
                    @Override
                    public void setCityName(String s) {
                        cityname = s;
                        refreshLayout.autoRefresh();
                    }
                });
            }
        });
    }

    private void initRefresh() {
        refreshLayout.setRefreshHeader(new MaterialHeader(getContext()));

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
            }
        });
    }

    private void initTheme() {
        wbv.initTheme();
        if (wbv.isNight()) {
            tv_theme.setText("夜间");
        } else {
            tv_theme.setText("白天");
        }

        layout_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wbv.changeTheme();
                if (wbv.isNight()) {
                    tv_theme.setText("夜间");
                } else {
                    tv_theme.setText("白天");
                }
            }
        });

    }

    private void initData() {
        ShareUtils.setCityName(getContext(), cityname);
        isFailed = false;
        initNowData();
        initAirData();
        initHourlyData();
        initForecastData();

        Intent intent = new Intent(getContext(), WeatherWidgetProvider.class);
        intent.setAction(WeatherWidgetProvider.ACTION_WIDGET_UPDATE);
        sendBroadcast(intent);
    }

    private void initNowData() {
        HeWeather.getWeatherNow(getContext(),
                cityname,
                Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC,
                new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();

                        isFailedNow = true;
                        checkOneFailed();
                    }

                    @Override
                    public void onSuccess(Now now) {
                        if (now.getStatus().equals(Code.OK.getCode())) {
                            NowBase nowBase = now.getNow();
                            if (WeatherManager.isNight(now.getUpdate().getLoc())
                                    && WeatherManager.hasNight(nowBase.getCond_code())) {//当前时间是晚上且存在晚上icon
                                GlideUtils.loadImage(getContext(),
                                        "file:///android_asset/" + nowBase.getCond_code() + "n.png",
                                        iv_cond);
                            } else {
                                GlideUtils.loadImage(getContext(),
                                        "file:///android_asset/" + nowBase.getCond_code() + ".png",
                                        iv_cond);
                            }
                            tv_tmp.setText(nowBase.getTmp() + "°C");
                            tv_cond.setText(nowBase.getCond_txt());
                            toolbar.setTitle(now.getBasic().getLocation());
                            String time = now.getUpdate().getLoc();
                            if (time.split(" ").length > 1) {
                                time = time.split(" ")[1];
                            }
                            toolbar.setSubtitle("上次更新时间：" + time);

                            int hum = Integer.parseInt(nowBase.getHum());
                            ap_hum.setProgress(hum);
                            kvv_fl.getTvValue().setText(nowBase.getFl() + "°C");
                            kvv_pcpn.getTvValue().setText(nowBase.getPcpn() + "mm");
                            kvv_pres.getTvValue().setText(nowBase.getPres() + "hpa");

                            kvv_wind_dir.getTvValue().setText(nowBase.getWind_dir());
                            kvv_wind_deg.getTvValue().setText(nowBase.getWind_deg() + "°");
                            kvv_wind_sc.getTvValue().setText(nowBase.getWind_sc() + "级");
                            kvv_wind_spd.getTvValue().setText(nowBase.getWind_spd() + "km/h");

                            isFinishNow = true;
                            checkAllFinish();
                        } else {
                            showToast(Code.toEnum(now.getStatus()).getTxt());

                            cityname = "auto_ip";
                            initData();
                        }
                    }
                });
    }

    private void initAirData() {
        HeWeather.getAirNow(getContext(),
                cityname,
                Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC,
                new HeWeather.OnResultAirNowBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();

                        isFailedAir = true;
                        checkOneFailed();
                    }

                    @Override
                    public void onSuccess(AirNow airNow) {
                        if (airNow.getStatus().equals(Code.OK.getCode())) {
                            AirNowCity airBase = airNow.getAir_now_city();
                            int aqi = Integer.parseInt(airBase.getAqi());
                            ap_aqi.setProgress(aqi);
                            kvv_qlty.getTvValue().setText(airBase.getQlty());
                            kvv_pm10.getTvValue().setText(airBase.getPm10());
                            kvv_pm25.getTvValue().setText(airBase.getPm25());
                            kvv_no2.getTvValue().setText(airBase.getNo2());
                            kvv_so2.getTvValue().setText(airBase.getSo2());
                            kvv_co.getTvValue().setText(airBase.getCo());
                            kvv_o3.getTvValue().setText(airBase.getO3());

                            isFinishAir = true;
                            checkAllFinish();
                        } else {
//                            showToast(Code.toEnum(airNow.getStatus()).getTxt());
                        }
                    }
                });
    }

    private void initHourlyData() {
        HeWeather.getWeatherHourly(getContext(),
                cityname,
                Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC,
                new HeWeather.OnResultWeatherHourlyBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();

                        isFailedHourly = true;
                        checkOneFailed();
                    }

                    @Override
                    public void onSuccess(Hourly hourly) {
                        if (hourly.getStatus().equals(Code.OK.getCode())) {
                            hourlyAdapter.setList(hourly.getHourly());
                            hourlyList = new ArrayList<>();
                            for (int i = 0; i < hourly.getHourly().size(); i++) {
                                hourlyList.add(new HourlyBean(hourly.getHourly().get(i)));
                            }

                            isFinishHourly = true;
                            checkAllFinish();
                        } else {
//                            showToast(Code.toEnum(hourly.getStatus()).getTxt());
                        }
                    }
                });
    }

    private void initForecastData() {
        HeWeather.getWeatherForecast(getContext(),
                cityname,
                Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC,
                new HeWeather.OnResultWeatherForecastBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();

                        isFailedForecast = true;
                        checkOneFailed();
                    }

                    @Override
                    public void onSuccess(Forecast forecast) {
                        if (forecast.getStatus().equals(Code.OK.getCode())) {
                            forecastAdapter.setList(forecast.getDaily_forecast());
                            forecastList = new ArrayList<>();
                            for (int i = 0; i < forecast.getDaily_forecast().size(); i++) {
                                forecastList.add(new ForecastBean(forecast.getDaily_forecast().get(i)));
                            }

                            isFinishForecast = true;
                            checkAllFinish();
                        } else {
//                            showToast(Code.toEnum(forecast.getStatus()).getTxt());
                        }
                    }
                });
    }

    /**
     * 检查请求完成，全部完成即为完成，刷新成功
     */
    private void checkAllFinish() {
        Log.d(TAG, "checkAllFinish: " + isFinishNow + " " + isFinishAir + " " + isFinishHourly + " " + isFinishForecast);
        if (isFinishNow && isFinishAir && isFinishHourly && isFinishForecast) {
            isFinishNow = false;
            isFinishAir = false;
            isFinishHourly = false;
            isFinishForecast = false;

            refreshLayout.finishRefresh();
            showToast("刷新成功");
        }
    }

    /**
     * 检查请求失败，一个失败即为失败，刷新失败
     */
    private void checkOneFailed() {
        if (isFailed) return;
        Log.d(TAG, "checkOneFailed: " + isFailedNow + " " + isFailedAir + " " + isFailedHourly + " " + isFailedForecast);
        if (isFailedNow || isFailedAir || isFailedHourly || isFailedForecast) {
            isFailedNow = false;
            isFailedAir = false;
            isFailedHourly = false;
            isFailedForecast = false;
            isFailed = true;

            refreshLayout.finishRefresh(false);
            showToast("数据获取错误，请检查网络");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animator.end();
    }
}
