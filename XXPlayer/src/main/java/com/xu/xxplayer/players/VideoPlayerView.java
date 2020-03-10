package com.xu.xxplayer.players;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.xu.xxplayer.controllers.NormalController;
import com.xu.xxplayer.controllers.PlayerController;

import java.io.File;

public class VideoPlayerView extends IjkPlayerView {

    private static final String TAG = "VideoPlayerView";

    private int statusbarHeight = 0;

    public interface OnPlayerViewListener {
        void onPlayNext(String url);

        void enterFullScreen();
    }

    private OnPlayerViewListener onPlayerViewListener;

    public void setOnPlayerViewListener(OnPlayerViewListener listener) {
        onPlayerViewListener = listener;
    }

    public VideoPlayerView(Context context) {
        this(context, null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStatusbarHeight(int height) {
        statusbarHeight = height;
    }

    @Override
    public void enterFullScreen() {
        if (isFullScreen()) return;
        if (isTinyScreen()) exitTinyScreen();

        //切换控制器
        setController(new PlayerController(getContext()));
        int state = getCurrentState();
        setOnPlayStateChanged(BasePlayerView.STATE_PLAYING);
        setOnPlayStateChanged(state);
        mController.setUrl(mUrl, mUrlList);
        ((PlayerController) mController).setThumb(mUrl);
        ((PlayerController) mController).setTitle(new File(mUrl).getName());
        ((PlayerController) mController).setStatusbarHeight(statusbarHeight);

        ViewGroup contentView = ((Activity) getContext()).findViewById(android.R.id.content);
        this.removeView(mContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mContainer, params);

        if (mVideoWidth > mVideoHeight) {
            //变为横屏
            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            //进入全屏
            Window window = ((Activity) getContext()).getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setOnScreenModeChanged(SCREEN_MODE_FULLSCREEN);

        if (onPlayerViewListener != null) {
            onPlayerViewListener.enterFullScreen();
        }
    }

    @Override
    public void exitFullScreen() {
        if (!isFullScreen()) return;

        //切换控制器
        setController(new NormalController(getContext()));
        int state = getCurrentState();
        setOnPlayStateChanged(BasePlayerView.STATE_PLAYING);
        setOnPlayStateChanged(state);
        mController.setUrl(mUrl, mUrlList);

        ViewGroup contentView = ((Activity) getContext()).findViewById(android.R.id.content);
        contentView.removeView(mContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);

        if (getResources().getConfiguration().orientation != 1) {
            //当前屏幕为横屏，则退出全屏变回竖屏
            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        //退出全屏
        Window window = ((Activity) getContext()).getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setOnScreenModeChanged(SCREEN_MODE_NORMAL);
    }

    @Override
    public void playNext(final String url) {
        super.playNext(url);
        if (onPlayerViewListener != null) {
            onPlayerViewListener.onPlayNext(url);
        }
        if (mController != null) {
            mController.setUrl(mUrl, mUrlList);
        }
    }
}
