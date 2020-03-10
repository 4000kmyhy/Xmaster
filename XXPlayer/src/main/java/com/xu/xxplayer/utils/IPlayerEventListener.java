package com.xu.xxplayer.utils;

public interface IPlayerEventListener {

    void mOnPrepared();

    void mOnInfo(int what, int extra);

    void mOnCompletion();

    void mOnError(int what, int extra);

    void mOnVideoSizeChanged(int width, int height);

    void mOnBufferingUpdate(int what);
}
