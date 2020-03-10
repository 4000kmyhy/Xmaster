package com.xu.xxplayer.players;

import android.content.Context;
import android.util.AttributeSet;

public abstract class MusicPlayerView extends BasePlayerView {

    public MusicPlayerView(Context context) {
        this(context, null);
    }

    public MusicPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void enterFullScreen() {

    }

    @Override
    public void exitFullScreen() {

    }

    @Override
    public void enterTinyScreen() {

    }

    @Override
    public void exitTinyScreen() {

    }

    public abstract void startService();

    public abstract void stopService();

    public interface OnServiceStateChangedListener {
        void onServiceStateChanged(int state);
    }

    private OnServiceStateChangedListener onServiceStateChangedListener;

    public void setOnServiceStateChangedListener(OnServiceStateChangedListener listener) {
        onServiceStateChangedListener = listener;
    }

    @Override
    public void setOnPlayStateChanged(int state) {
        super.setOnPlayStateChanged(state);

        if(onServiceStateChangedListener!=null){
            onServiceStateChangedListener.onServiceStateChanged(state);
        }
    }
}
