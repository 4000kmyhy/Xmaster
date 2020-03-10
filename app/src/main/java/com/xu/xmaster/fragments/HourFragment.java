package com.xu.xmaster.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.czp.library.ArcProgress;
import com.xu.xmaster.R;
import com.xu.xmaster.beans.HourlyBean;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.PixelUtils;
import com.xu.xmaster.utils.WeatherManager;
import com.xu.xmaster.views.KeyValueView;

public class HourFragment extends Fragment {

    private static final String TAG = "HourFragment";

    private ImageView iv_cond;
    private TextView tv_cond, tv_tmp;
    //舒适度
    private ArcProgress ap_hum;
    private KeyValueView kvv_fl, kvv_pcpn, kvv_pres;
    //风力风速
    private ImageView iv_wind;
    private KeyValueView kvv_wind_dir, kvv_wind_deg, kvv_wind_sc, kvv_wind_spd;

    private HourlyBean hourlyBean;

    private ObjectAnimator animator;

    public static HourFragment newInstance(HourlyBean hourlyBean) {
        HourFragment fragment = new HourFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hourlyBean", hourlyBean);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            hourlyBean = (HourlyBean) bundle.getSerializable("hourlyBean");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_hour, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv_cond = view.findViewById(R.id.iv_cond);
        tv_cond = view.findViewById(R.id.tv_cond);
        tv_tmp = view.findViewById(R.id.tv_tmp);
        //舒适度
        ap_hum = view.findViewById(R.id.ap_hum);
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
        kvv_fl = view.findViewById(R.id.kvv_fl);
        kvv_pcpn = view.findViewById(R.id.kvv_pcpn);
        kvv_pres = view.findViewById(R.id.kvv_pres);

        //风力风速
        iv_wind = view.findViewById(R.id.iv_wind);
        kvv_wind_dir = view.findViewById(R.id.kvv_wind_dir);
        kvv_wind_deg = view.findViewById(R.id.kvv_wind_deg);
        kvv_wind_sc = view.findViewById(R.id.kvv_wind_sc);
        kvv_wind_spd = view.findViewById(R.id.kvv_wind_spd);

        initData();

        animator = ObjectAnimator.ofFloat(iv_wind, "rotation", 0f, 360f);
        animator.setDuration(5000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    private void initData() {
        if (WeatherManager.isNight(hourlyBean.getTime()) &&
                WeatherManager.hasNight(hourlyBean.getCond_code())) {//当前时间是晚上且存在晚上icon
            GlideUtils.loadImage(getContext(),
                    "file:///android_asset/" + hourlyBean.getCond_code() + "n.png",
                    iv_cond);
        } else {
            GlideUtils.loadImage(getContext(),
                    "file:///android_asset/" + hourlyBean.getCond_code() + ".png",
                    iv_cond);
        }
        tv_tmp.setText(hourlyBean.getTmp() + "°C");
        tv_cond.setText(hourlyBean.getCond_txt());

        int hum = Integer.parseInt(hourlyBean.getHum());
        ap_hum.setProgress(hum);
        kvv_fl.getTvKey().setText("露点温度");
        kvv_fl.getTvValue().setText(hourlyBean.getDew() + "°C");
        kvv_pcpn.getTvKey().setText("降水概率");
        kvv_pcpn.getTvValue().setText(hourlyBean.getPop() + "%");
        kvv_pres.getTvValue().setText(hourlyBean.getPres() + "hpa");

        kvv_wind_dir.getTvValue().setText(hourlyBean.getWind_dir());
        kvv_wind_deg.getTvValue().setText(hourlyBean.getWind_deg() + "°");
        kvv_wind_sc.getTvValue().setText(hourlyBean.getWind_sc() + "级");
        kvv_wind_spd.getTvValue().setText(hourlyBean.getWind_spd() + "km/h");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animator.end();
    }
}
