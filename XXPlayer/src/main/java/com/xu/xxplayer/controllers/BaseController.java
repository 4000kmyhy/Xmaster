package com.xu.xxplayer.controllers;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.xu.xxplayer.R;
import com.xu.xxplayer.players.BasePlayerView;

import java.util.List;

public abstract class BaseController extends FrameLayout {

    private static final String TAG = "BaseController";
    public static final int START_PLAYING = 0;
    public static final int UPDATE_TIME = 1;
    public static final int UPDATE_PLAYER = 2;
    public static final int UPDATE_SEEKBAR = 3;
    public static final int HIDE_POSITION_BAR = 4;
    public static final int HIDE_BRIGHTNESS_BAR = 5;
    public static final int HIDE_VOLUME_BAR = 6;

    protected View mView;
    protected BasePlayerView mPlayerView;

    protected String mUrl;
    protected List<String> mUrlList;
    protected int urlPosition = -1;

    protected Animation showTop, showBottom, hideTop, hideBottom;

    public boolean isShowBar = false;
    public boolean isShowMenu = false;
    public boolean isShowList = false;
    public boolean isLiveTV = false;

    public static final int threshold = 80;

    public BaseController(Context context) {
        this(context, null);
    }

    public BaseController(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    protected void initView() {
        mView = LayoutInflater.from(getContext()).inflate(getLayoutId(), this);
        setClickable(true);

        showTop = AnimationUtils.loadAnimation(getContext(), R.anim.show_top);
        showBottom = AnimationUtils.loadAnimation(getContext(), R.anim.show_bottom);
        hideTop = AnimationUtils.loadAnimation(getContext(), R.anim.hide_top);
        hideBottom = AnimationUtils.loadAnimation(getContext(), R.anim.hide_bottom);
    }

    protected abstract int getLayoutId();

    public abstract void reset();

    public abstract void onPlayStateChanged(int state);

    public abstract void onScreenModeChanged(int mode);

    public void setPlayer(BasePlayerView basePlayerView) {
        mPlayerView = basePlayerView;
    }

    public void setUrl(String url, List<String> urlList) {
        Log.d(TAG, "setUrl: " + url + " " + urlList);
        mUrl = url;
        mUrlList = urlList;

        if (mUrlList == null) return;
        for (int i = 0; i < mUrlList.size(); i++) {
            if (TextUtils.equals(mUrl, mUrlList.get(i))) {
                urlPosition = i;
                break;
            }
        }
    }

    public void replay() {
        mPlayerView.restart();
    }

    protected void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
