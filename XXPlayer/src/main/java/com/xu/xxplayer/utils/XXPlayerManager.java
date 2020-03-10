package com.xu.xxplayer.utils;

import android.util.Log;

import com.xu.xxplayer.players.BasePlayerView;
import com.xu.xxplayer.players.MusicPlayerView;

public class XXPlayerManager {

    private static final String TAG = "XXPlayerManager";

    private BasePlayerView mPlayerView;

    private static XXPlayerManager instance;

    public static XXPlayerManager instance() {
        if (instance == null) {
            synchronized (XXPlayerManager.class) {
                instance = new XXPlayerManager();
            }
        }
        return instance;
    }

    public BasePlayerView getCurrentPlayer() {
        return mPlayerView;
    }

    public void setCurrentPlayer(BasePlayerView playerView) {
        if (mPlayerView != playerView) {
            if (mPlayerView instanceof MusicPlayerView && !(playerView instanceof MusicPlayerView)) {
                //从音乐播放器切换到其他播放器时
                ((MusicPlayerView) mPlayerView).stopService();
            }
            releasePlayer();
            mPlayerView = playerView;
        }
    }

    public void pausePlayer() {
        if (mPlayerView != null && (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying())) {
            mPlayerView.pause();
        }
    }

    public void repausePlayer() {
        if (mPlayerView != null && (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying())) {
            mPlayerView.repause();
        }
    }

    public void resumePlayer() {
        if (mPlayerView != null && (mPlayerView.isPaused() || mPlayerView.isBufferingPaused())) {
            mPlayerView.restart();
        }
    }

    public void restartPlayer() {
        if (mPlayerView != null && (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying())) {
            mPlayerView.restart();
        }
    }

    public void releasePlayer() {
        if (mPlayerView != null) {
            mPlayerView.release();
            mPlayerView = null;
        }
    }

    public boolean onBackPressd() {
        if (mPlayerView != null) {
            if (mPlayerView.isFullScreen()) {
                mPlayerView.exitFullScreen();
                return true;
            } else if (mPlayerView.isTinyScreen()) {
                mPlayerView.exitTinyScreen();
                return true;
            }
        }
        return false;
    }
}
