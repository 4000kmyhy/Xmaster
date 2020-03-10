package com.xu.xxplayer.controllers;

import android.content.Context;
import android.util.AttributeSet;

import com.xu.xxplayer.players.BasePlayerView;

public class SimpleController extends BaseController {

    private static final String TAG = "SimpleController";

    public SimpleController(Context context) {
        this(context, null);
    }

    public SimpleController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void onPlayStateChanged(int state) {
        switch (state) {
            case BasePlayerView.STATE_IDLE:
                break;
            case BasePlayerView.STATE_PREPARING:
                break;
            case BasePlayerView.STATE_PLAYING:
                break;
            case BasePlayerView.STATE_PAUSED:
                break;
            case BasePlayerView.STATE_BUFFERING_PLAYING:
                break;
            case BasePlayerView.STATE_BUFFERING_PAUSED:
                break;
            case BasePlayerView.STATE_COMPLETED:
                break;
            case BasePlayerView.STATE_ERROR:
                break;
        }
    }

    @Override
    public void onScreenModeChanged(int mode) {
        switch (mode) {
            case BasePlayerView.SCREEN_MODE_NORMAL:
                break;
            case BasePlayerView.SCREEN_MODE_FULLSCREEN:
                break;
            case BasePlayerView.SCREEN_MODE_TINYSCREEN:
                break;
        }
    }
}
