package com.xu.xxplayer.views;

import android.content.Context;
import android.util.Log;
import android.view.TextureView;

import com.xu.xxplayer.players.BasePlayerView;

public class XXTextureView extends TextureView {

    private static final String TAG = "XXTextureView";

    private int mVideoWidth;
    private int mVideoHeight;
    private int screenType;

    public XXTextureView(Context context) {
        super(context);
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        requestLayout();
    }

    public void setScreenScale(int type) {
        screenType = type;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int orientation = (int) (Math.abs((getRotation())) / 90 % 2);
        Log.d(TAG, "onMeasure: " + getRotation() + " , " + orientation);
        if (orientation == 1) {//交换宽高
            widthMeasureSpec = widthMeasureSpec + heightMeasureSpec;
            heightMeasureSpec = widthMeasureSpec - heightMeasureSpec;
            widthMeasureSpec = widthMeasureSpec - heightMeasureSpec;
        }

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        switch (screenType) {
            case BasePlayerView.SCREEN_SCALE_ADAPT:
                //适应：视频等比缩放，保持视频比例的同时最大化显示视频
                if (height > (int) (1.0f * mVideoHeight / mVideoWidth * width)) {
                    height = (int) (1.0f * mVideoHeight / mVideoWidth * width);
                } else {
                    width = (int) (1.0f * mVideoWidth / mVideoHeight * height);
                }
                break;
            case BasePlayerView.SCREEN_SCALE_STRETCH:
                //拉伸：视频不按比例缩放，而是根据屏幕显示分辨率拉伸
                break;
            case BasePlayerView.SCREEN_SCALE_FILL:
                //填充：视频等比缩放，最小边适应最大边以达到填充全屏，可能部分显示不了
                if (height > width) {//竖屏
                    width = (int) (1.0f * mVideoWidth / mVideoHeight * height);
                } else {//横屏
                    height = (int) (1.0f * mVideoHeight / mVideoWidth * width);
                }
                break;
            case BasePlayerView.SCREEN_SCALE_16_9:
                if (height > (int) (1.0f * width / 16 * 9)) {
                    height = (int) (1.0f * width / 16 * 9);
                } else {
                    width = (int) (1.0f * height / 9 * 16);
                }
                break;
            case BasePlayerView.SCREEN_SCALE_4_3:
                if (height > (int) (1.0f * width / 4 * 3)) {
                    height = (int) (1.0f * width / 4 * 3);
                } else {
                    width = (int) (1.0f * height / 3 * 4);
                }
                break;
        }
        setMeasuredDimension(width, height);
    }
}
