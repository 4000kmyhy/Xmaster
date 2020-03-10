package com.xu.xmaster.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.ShareUtils;
import com.xu.xmaster.utils.WeatherManager;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class SimpleToolbar extends FrameLayout {

    private static final String TAG = "SimpleToolbar";

    private View mView;
    private ImageView mLeftBtn, mRightBtn1, mRightBtn2, mWeaImg;
    private TextView mTitle, mSubtitle, mWeaTmp, mWeaTxt;
    private LinearLayout mTitleLayout, mWeaLayout;

    public SimpleToolbar(Context context) {
        this(context, null);
    }

    public SimpleToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
        initAttr(attrs);
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.simple_toolbar, this, false);
        mTitleLayout = mView.findViewById(R.id.toolbar_title_layout);
        mTitle = mView.findViewById(R.id.toolbar_title);
        mSubtitle = mView.findViewById(R.id.toolbar_subtitle);
        mLeftBtn = mView.findViewById(R.id.toolbar_left_btn);
        mRightBtn1 = mView.findViewById(R.id.toolbar_right_btn1);
        mRightBtn2 = mView.findViewById(R.id.toolbar_right_btn2);
        mWeaLayout = mView.findViewById(R.id.toolbar_wea_layout);
        mWeaImg = mView.findViewById(R.id.toolbar_wea_img);
        mWeaTmp = mView.findViewById(R.id.toolbar_wea_tmp);
        mWeaTxt = mView.findViewById(R.id.toolbar_wea_txt);
        addView(mView);
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.toolbar);
        String title = typedArray.getString(R.styleable.toolbar_title);
        String subtitle = typedArray.getString(R.styleable.toolbar_subtitle);
        int leftBtnRes = typedArray.getResourceId(R.styleable.toolbar_left_btn, R.mipmap.ic_launcher);
        int rightBtn1Res = typedArray.getResourceId(R.styleable.toolbar_right_btn1, R.mipmap.ic_launcher);
        int rightBtn2Res = typedArray.getResourceId(R.styleable.toolbar_right_btn2, R.mipmap.ic_launcher);
        boolean isShowWea = typedArray.getBoolean(R.styleable.toolbar_is_show_wea, false);
        typedArray.recycle();

        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
            mTitle.setSelected(true);
        }
        if (!TextUtils.isEmpty(subtitle)) {
            mSubtitle.setVisibility(VISIBLE);
            mSubtitle.setText(subtitle);
            mSubtitle.setSelected(true);
        }
        if (leftBtnRes != R.mipmap.ic_launcher) {
            mLeftBtn.setVisibility(VISIBLE);
            mLeftBtn.setImageResource(leftBtnRes);
        }
        if (rightBtn1Res != R.mipmap.ic_launcher) {
            mRightBtn1.setVisibility(VISIBLE);
            mRightBtn1.setImageResource(rightBtn1Res);
        }
        if (rightBtn2Res != R.mipmap.ic_launcher) {
            mRightBtn2.setVisibility(VISIBLE);
            mRightBtn2.setImageResource(rightBtn2Res);
        }
        if (isShowWea) {
            mWeaLayout.setVisibility(VISIBLE);
            initWea();
        }
    }

    public void setPaddingTop() {
        setPadding(0, QMUIStatusBarHelper.getStatusbarHeight(getContext()), 0, 0);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
        mTitle.setSelected(true);
    }

    public void setSubtitle(String subtitle) {
        mSubtitle.setVisibility(VISIBLE);
        mSubtitle.setText(subtitle);
        mSubtitle.setSelected(true);
    }

    public void removeSubtitle(){
        mSubtitle.setVisibility(GONE);
    }

    public TextView getTitleView() {
        return mTitle;
    }

    public void setTitleGravity(int gravity) {
        mTitleLayout.setGravity(gravity);
    }

    public void setLeftBtn(int res) {
        mLeftBtn.setVisibility(VISIBLE);
        mLeftBtn.setImageResource(res);
    }

    public ImageView getLeftBtn() {
        return mLeftBtn;
    }

    public void setRightBtn1(int res) {
        mRightBtn1.setVisibility(VISIBLE);
        mRightBtn1.setImageResource(res);
    }

    public ImageView getRightBtn1() {
        return mRightBtn1;
    }

    public void setRightBtn2(int res) {
        mRightBtn2.setVisibility(VISIBLE);
        mRightBtn2.setImageResource(res);
    }

    public ImageView getRightBtn2() {
        return mRightBtn2;
    }

    public void showWea() {
        mWeaLayout.setVisibility(VISIBLE);
        initWea();
    }

    private void initWea() {
        String cityname = ShareUtils.getCityName(getContext());
        HeWeather.getWeatherNow(getContext(),
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
                            mWeaTmp.setText(nowBase.getTmp() + "°C");
                            mWeaTxt.setText(nowBase.getCond_txt());

                            if (WeatherManager.isNight(now.getUpdate().getLoc())
                                    && WeatherManager.hasNight(nowBase.getCond_code())) {//当前时间是晚上且存在晚上icon
                                GlideUtils.loadImage(getContext(),
                                        "file:///android_asset/" + nowBase.getCond_code() + "n.png",
                                        mWeaImg);
                            } else {
                                GlideUtils.loadImage(getContext(),
                                        "file:///android_asset/" + nowBase.getCond_code() + ".png",
                                        mWeaImg);
                            }
                        }
                    }
                });
    }

    public void setLeftBtnOnClickListener(OnClickListener onClickListener) {
        mLeftBtn.setOnClickListener(onClickListener);
    }

    public void setRightBtn1OnClickListener(OnClickListener onClickListener) {
        mRightBtn1.setOnClickListener(onClickListener);
    }

    public void setRightBtn2OnClickListener(OnClickListener onClickListener) {
        mRightBtn2.setOnClickListener(onClickListener);
    }

    public void setWeaLayoutOnClickListener(OnClickListener onClickListener) {
        mWeaLayout.setOnClickListener(onClickListener);
    }
}
