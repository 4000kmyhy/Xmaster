package com.xu.xxplayer.utils;

import java.util.List;

public interface IPlayer {

    void start();

    void restart();

    void pause();

    void repause();

    void release();

    void setUrl(String url);

    void setUrl(String url, List<String> urlList);

    String getUrl();

    List<String> getUrlList();

    boolean isIdle();

    boolean isPreparing();

    boolean isPlaying();

    boolean isPaused();

    boolean isBufferingPlaying();

    boolean isBufferingPaused();

    boolean isCompleted();

    boolean isError();

    boolean isFullScreen();

    boolean isTinyScreen();

    int getCurrentState();

    void setScreenScale(int screenScale);

    int getScreenScale();

    void setPlayMode(int playMode);

    int getPlayMode();

    void setCodecType(int codecType);

    void setContinuePlay(boolean isContinuePlay);

    long getDuration();

    long getCurrentPosition();

    void seekTo(long position);

    int getMaxVolume();

    int getVolume();

    void setVolume(int volume);

    void setPlaySpeed(float speed);

    float getPlaySpeed();

    long getTcpSpeed();

    void enterFullScreen();

    void exitFullScreen();

    void enterTinyScreen();

    void exitTinyScreen();

    void setOnPlayStateChanged(int state);

    void setOnScreenModeChanged(int mode);

    void playNext(String url);
}
