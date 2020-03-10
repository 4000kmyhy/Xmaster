package com.xu.xmaster.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xu.xmaster.R;
import com.xu.xmaster.utils.ShareUtils;
import com.xu.xmaster.utils.WeatherManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WeatherBackgroundView extends FrameLayout {

    private View mView;
    private ImageView bg_weather;

    private Animator animator;
    private boolean isNight = false;

    public WeatherBackgroundView(@NonNull Context context) {
        super(context);
        initView();
    }

    public WeatherBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.weather_background, this);
        bg_weather = mView.findViewById(R.id.bg_weather);

        animator = AnimatorInflater.loadAnimator(getContext(), R.animator.translate_repeat);
        animator.setTarget(bg_weather);
        animator.start();

        if (ShareUtils.isNight(getContext())) {
            setNight(true);
        } else {
            setNight(false);
        }
    }

    public void initTheme() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (WeatherManager.isNight(dateFormat.format(new Date()))) {
            setNight(true);
        } else {
            setNight(false);
        }
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
        ShareUtils.setNight(getContext(), isNight);
        if (isNight) {
            bg_weather.setImageResource(R.drawable.pic_night);
        } else {
            bg_weather.setImageResource(R.drawable.pic_day);
        }
    }

    public void changeTheme() {
        setNight(!isNight);
    }

    public boolean isNight() {
        return isNight;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.end();
    }
}
