package com.xu.xxplayer.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xu.xxplayer.R;
import com.xu.xxplayer.players.BasePlayerView;
import com.xu.xxplayer.players.IjkPlayerView;
import com.xu.xxplayer.utils.XXPlayerUtil;

import java.io.File;
import java.util.List;

public class NormalController extends BaseController implements View.OnClickListener {

    private static final String TAG = "NormalController";

    /* ui */
    private ImageView iv_thumb;
    //tinybar
    private FrameLayout layout_tinybar;
    private ImageView iv_tiny_back, iv_tiny_fullscreen;
    //bottombar
    private LinearLayout layout_bottombar;
    private ImageView iv_play, iv_fullscreen;
    private TextView tv_current, tv_duration;
    private SeekBar sb_play;
    //中间的按钮
    private ImageView iv_start, iv_next;
    //加载
    private ProgressBar pb_loading;
    /* ui */

    private GestureDetector mDestector;//手势控制

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mPlayerView.isIdle()) {
            mDestector.onTouchEvent(event);
        }
        return true;
    }

    public NormalController(Context context) {
        this(context, null);
    }

    public NormalController(Context context, AttributeSet attrs) {
        super(context, attrs);

        initEvent();
    }

    private void initEvent() {
        iv_play.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        iv_start.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_tiny_back.setOnClickListener(this);
        iv_tiny_fullscreen.setOnClickListener(this);

        sb_play.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                UIHandler.removeMessages(UPDATE_SEEKBAR);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (!mPlayerView.isIdle() && !isLiveTV) {//直播视频不能改变进度条
                    mPlayerView.seekTo(progress);
                    tv_current.setText(XXPlayerUtil.stringForTime(progress));
                }
                UIHandler.sendEmptyMessage(UPDATE_SEEKBAR);
            }
        });

        mDestector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed: 单击" + isShowBar);
                if (!mPlayerView.isTinyScreen()) {
                    if (isShowBar) {
                        hideBar();
                    } else {
                        showBar();
                    }
                } else {
                    if (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying()) {
                        mPlayerView.pause();
                    } else if (mPlayerView.isPaused() || mPlayerView.isBufferingPaused() ||
                            mPlayerView.isCompleted() || mPlayerView.isError()) {
                        mPlayerView.restart();
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "onDoubleTap: 双击");
                if (!mPlayerView.isTinyScreen()) {
                    if (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying()) {
                        mPlayerView.pause();
                    } else if (mPlayerView.isPaused() || mPlayerView.isBufferingPaused() ||
                            mPlayerView.isCompleted() || mPlayerView.isError()) {
                        mPlayerView.restart();
                        hideBar();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_start) {//中间播放、暂停
            if (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying()) {
                mPlayerView.pause();
            } else if (mPlayerView.isPaused() || mPlayerView.isBufferingPaused() ||
                    mPlayerView.isCompleted() || mPlayerView.isError()) {
                hideBar();
                mPlayerView.restart();
            }
        } else if (id == R.id.iv_next) {//播放下集
            if (urlPosition < mUrlList.size() - 1) {
                playNext(urlPosition + 1);
            } else {
                showToast("没有视频了");
            }
        } else if (id == R.id.iv_play) {//播放、暂停
            if (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying()) {
                hideBar();
                mPlayerView.pause();
            } else if (mPlayerView.isPaused() || mPlayerView.isBufferingPaused() ||
                    mPlayerView.isCompleted() || mPlayerView.isError()) {
                hideBar();
                mPlayerView.restart();
            }
        } else if (id == R.id.iv_fullscreen) {//进入全屏、退出全屏
            if (mPlayerView.isFullScreen()) {
                mPlayerView.exitFullScreen();
            } else {
                mPlayerView.enterFullScreen();
            }
        } else if (id == R.id.iv_tiny_back) {//退出小窗
            mPlayerView.exitTinyScreen();
        } else if (id == R.id.iv_tiny_fullscreen) {//从小窗进入全屏
            mPlayerView.enterFullScreen();
        }
    }

    /**
     * 播放下一集
     *
     * @param position
     */
    private void playNext(int position) {
        urlPosition = position;
        mPlayerView.playNext(mUrlList.get(position));
        ((IjkPlayerView) mPlayerView).setBackground(mUrlList.get(position));
        setThumb(mUrlList.get(position));
    }

    public void showBar() {
        if (isShowBar) return;
        isShowBar = true;

        layout_bottombar.startAnimation(showBottom);
        layout_bottombar.setVisibility(VISIBLE);

        //显示状态栏
        Window window = ((Activity) getContext()).getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void hideBar() {
        if (!isShowBar) return;
        isShowBar = false;

        layout_bottombar.startAnimation(hideBottom);
        layout_bottombar.setVisibility(GONE);

        //隐藏状态栏
        Window window = ((Activity) getContext()).getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_PLAYING:
                    //获取视频长度，设置播放时长，最大进度条
                    long duration = mPlayerView.getDuration();
                    sb_play.setMax((int) duration);
                    tv_duration.setText(XXPlayerUtil.stringForTime(duration));
                    if (duration > 0) {
                        isLiveTV = false;
                    } else {
                        isLiveTV = true;
                    }
                    break;
                case UPDATE_PLAYER:
                    //更新播放时间
                    tv_current.setText(XXPlayerUtil.stringForTime(mPlayerView.getCurrentPosition()));
                    if (mPlayerView.isPlaying()) {
                        UIHandler.sendEmptyMessageDelayed(UPDATE_PLAYER, 1000);
                    }
                    break;
                case UPDATE_SEEKBAR:
                    //更新进度条
                    sb_play.setProgress((int) mPlayerView.getCurrentPosition());
                    sb_play.setSecondaryProgress(mPlayerView.getBufferPosition());
                    if (mPlayerView.isPlaying()) {
                        UIHandler.sendEmptyMessageDelayed(UPDATE_SEEKBAR, 1000);
                    }
                    break;
            }
        }
    };

    @Override
    protected void initView() {
        super.initView();
        iv_thumb = mView.findViewById(R.id.iv_thumb);
        //tinybar
        layout_tinybar = mView.findViewById(R.id.layout_tinybar);
        iv_tiny_back = mView.findViewById(R.id.iv_tiny_back);
        iv_tiny_fullscreen = mView.findViewById(R.id.iv_tiny_fullscreen);
        //bottombar
        layout_bottombar = mView.findViewById(R.id.layout_bottombar);
        iv_play = mView.findViewById(R.id.iv_play);
        iv_fullscreen = mView.findViewById(R.id.iv_fullscreen);
        tv_current = mView.findViewById(R.id.tv_current);
        tv_duration = mView.findViewById(R.id.tv_duration);
        sb_play = mView.findViewById(R.id.sb_play);
        //中间的按钮
        iv_start = mView.findViewById(R.id.iv_start);
        iv_next = mView.findViewById(R.id.iv_next);
        //加载
        pb_loading = mView.findViewById(R.id.pb_loading);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.normal_controller;
    }

    @Override
    public void reset() {
        isShowBar = false;

        sb_play.setProgress(0);
        sb_play.setSecondaryProgress(0);

        layout_tinybar.setVisibility(GONE);
        layout_bottombar.setVisibility(GONE);

        pb_loading.setVisibility(GONE);

        iv_start.setVisibility(VISIBLE);
        iv_next.setVisibility(GONE);
        showThumb();

        //进入全屏
        Window window = ((Activity) getContext()).getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void showThumb() {
        ViewGroup.LayoutParams params = iv_thumb.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        iv_thumb.setLayoutParams(params);
        iv_thumb.setVisibility(View.VISIBLE);
    }

    public void setThumb(String url) {
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .into(iv_thumb);
    }

    @Override
    public void onPlayStateChanged(int state) {
        switch (state) {
            case BasePlayerView.STATE_IDLE:
                break;
            case BasePlayerView.STATE_PREPARING:
                hideBar();
                iv_start.setVisibility(GONE);
                iv_next.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                break;
            case BasePlayerView.STATE_PLAYING:
                iv_thumb.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_pause);
                iv_start.setVisibility(GONE);
                iv_next.setVisibility(GONE);

                UIHandler.sendEmptyMessage(START_PLAYING);
                UIHandler.sendEmptyMessage(UPDATE_PLAYER);
                UIHandler.sendEmptyMessage(UPDATE_SEEKBAR);
                break;
            case BasePlayerView.STATE_PAUSED:
                iv_thumb.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_play);
                iv_start.setVisibility(VISIBLE);
                iv_start.setImageResource(R.drawable.ic_play);
                iv_next.setVisibility(GONE);
                break;
            case BasePlayerView.STATE_BUFFERING_PLAYING:
                iv_thumb.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                iv_play.setImageResource(R.drawable.ic_pause);
                break;
            case BasePlayerView.STATE_BUFFERING_PAUSED:
                iv_thumb.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                iv_play.setImageResource(R.drawable.ic_play);
                break;
            case BasePlayerView.STATE_COMPLETED:
            case BasePlayerView.STATE_ERROR:
                pb_loading.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_refresh);
                iv_start.setVisibility(VISIBLE);
                iv_start.setImageResource(R.drawable.ic_refresh);
                iv_next.setVisibility(VISIBLE);
                showBar();
                //进度条跳到最后
                tv_current.setText(XXPlayerUtil.stringForTime(mPlayerView.getDuration()));
                sb_play.setProgress(sb_play.getMax());
                break;
        }
    }

    @Override
    public void onScreenModeChanged(int mode) {
        switch (mode) {
            case BasePlayerView.SCREEN_MODE_NORMAL:
                layout_tinybar.setVisibility(GONE);
                break;
            case BasePlayerView.SCREEN_MODE_FULLSCREEN:
                break;
            case BasePlayerView.SCREEN_MODE_TINYSCREEN:
                layout_tinybar.setVisibility(VISIBLE);
                hideBar();
                break;
        }
    }
}
