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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.czp.library.ArcProgress;
import com.xu.xmaster.R;
import com.xu.xmaster.beans.ForecastBean;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.PixelUtils;
import com.xu.xmaster.utils.WeatherManager;
import com.xu.xmaster.views.KeyValueView;

public class DayFragment extends Fragment {

    private static final String TAG = "DayFragment";

    private ImageView iv_code_d, iv_code_n;
    private TextView tv_txt_d, tv_txt_n, tv_tmp;
    //舒适度
    private ArcProgress ap_hum;
    private KeyValueView kvv_fl, kvv_pcpn, kvv_pres;
    //风力风速
    private ImageView iv_wind;
    private KeyValueView kvv_wind_dir, kvv_wind_deg, kvv_wind_sc, kvv_wind_spd;

    private ForecastBean forecastBean;

    private ObjectAnimator animator;

    public static DayFragment newInstance(ForecastBean forecastBean) {
        DayFragment fragment = new DayFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("forecastBean", forecastBean);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            forecastBean = (ForecastBean) bundle.getSerializable("forecastBean");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv_code_d = view.findViewById(R.id.iv_code_d);
        iv_code_n = view.findViewById(R.id.iv_code_n);
        tv_txt_d = view.findViewById(R.id.tv_txt_d);
        tv_txt_n = view.findViewById(R.id.tv_txt_n);
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
        GlideUtils.loadImage(getContext(),
                "file:///android_asset/" + forecastBean.getCond_code_d() + ".png",
                iv_code_d);
        if (WeatherManager.hasNight(forecastBean.getCond_code_n())) {//存在晚上icon
            GlideUtils.loadImage(getContext(),
                    "file:///android_asset/" + forecastBean.getCond_code_n() + "n.png",
                    iv_code_n);
        } else {
            GlideUtils.loadImage(getContext(),
                    "file:///android_asset/" + forecastBean.getCond_code_n() + ".png",
                    iv_code_n);
        }
        tv_txt_d.setText(forecastBean.getCond_txt_d());
        tv_txt_n.setText(forecastBean.getCond_txt_n());
        String tmp_max = forecastBean.getTmp_max() + "°C";
        String tmp_min = forecastBean.getTmp_min() + "°C";
        String tmpStr = "<big><font color='#ffffff'>" + tmp_max +
                "</font></big><font color='#c0c0c0'> / " + tmp_min + "</font>";
        tv_tmp.setText(Html.fromHtml(tmpStr));

        int hum = Integer.parseInt(forecastBean.getHum());
        ap_hum.setProgress(hum);
        kvv_fl.getTvKey().setText("降水量");
        kvv_fl.getTvValue().setText(forecastBean.getPcpn() + "mm");
        kvv_pcpn.getTvKey().setText("降水概率");
        kvv_pcpn.getTvValue().setText(forecastBean.getPop() + "%");
        kvv_pres.getTvValue().setText(forecastBean.getPres() + "hpa");

        kvv_wind_dir.getTvValue().setText(forecastBean.getWind_dir());
        kvv_wind_deg.getTvValue().setText(forecastBean.getWind_deg() + "°");
        kvv_wind_sc.getTvValue().setText(forecastBean.getWind_sc() + "级");
        kvv_wind_spd.getTvValue().setText(forecastBean.getWind_spd() + "km/h");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animator.end();
    }
}
