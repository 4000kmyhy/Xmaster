package com.xu.xxplayer.players;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.xu.xxplayer.controllers.BaseController;
import com.xu.xxplayer.views.XXTextureView;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class IjkPlayerView extends BasePlayerView {

    private static final String TAG = "IjkPlayerView";

    protected FrameLayout mContainer;
    private XXTextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    protected int mVideoWidth, mVideoHeight;
    protected int mOrientation = 1;//初始屏幕状态，1为竖屏，2为横屏.

    public interface onCompletionListener {
        void onCompletion();
    }

    protected onCompletionListener mListener;

    public void setOnCompletionListener(onCompletionListener listener) {
        mListener = listener;
    }

    public IjkPlayerView(Context context) {
        this(context, null);
    }

    public IjkPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mOrientation = getResources().getConfiguration().orientation;
        initView();
    }

    protected void initView() {
        mContainer = new FrameLayout(getContext());
        mContainer.setClickable(true);
        mContainer.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    public void setBackground(String url) {
        Glide.with(getContext())
                .load(url)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 10)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        mContainer.setBackground(resource);
                    }
                });
    }

    public void setController(BaseController controller) {
        mContainer.removeView(mController);
        mController = controller;
        mController.setPlayer(this);
        mController.reset();
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mController, params);
    }

    @Override
    public void enterFullScreen() {
        if (isFullScreen()) return;
        if (isTinyScreen()) exitTinyScreen();

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
    }

    @Override
    public void exitFullScreen() {
        if (!isFullScreen()) return;

        ViewGroup contentView = ((Activity) getContext()).findViewById(android.R.id.content);
        contentView.removeView(mContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);

        if (mOrientation == 1) {
            //初始屏幕为竖屏，则退出全屏变回竖屏
            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        //退出全屏
        Window window = ((Activity) getContext()).getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setOnScreenModeChanged(SCREEN_MODE_NORMAL);
    }

    @Override
    public void enterTinyScreen() {
        if (isTinyScreen()) return;
        if (isFullScreen()) exitFullScreen();

        ViewGroup contentView = ((Activity) getContext()).findViewById(android.R.id.content);
        this.removeView(mContainer);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.7);//宽度为屏幕宽度
        int height = (int) (width * 10f / 16f);//高度为宽度的9/16
        LayoutParams params = new LayoutParams(
                width, height);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        contentView.addView(mContainer, params);

        setOnScreenModeChanged(SCREEN_MODE_TINYSCREEN);
    }

    @Override
    public void exitTinyScreen() {
        if (!isTinyScreen()) return;

        ViewGroup contentView = ((Activity) getContext()).findViewById(android.R.id.content);
        contentView.removeView(mContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);

        setOnScreenModeChanged(SCREEN_MODE_NORMAL);
    }

    @Override
    protected void initPlayer() {
        super.initPlayer();
        addDispley();
    }

    private void addDispley() {
        mContainer.removeView(mTextureView);
        mSurfaceTexture = null;
        mTextureView = new XXTextureView(getContext());
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable: " + surfaceTexture);
                if (mSurfaceTexture == null) {
                    mSurfaceTexture = surfaceTexture;
                    mMediaPlayer.setSurface(new Surface(surfaceTexture));
                } else {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return mSurfaceTexture == null;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mContainer.addView(mTextureView, 0, params);
    }

    @Override
    public void setScreenScale(int screenScale) {
        super.setScreenScale(screenScale);
        if (mTextureView != null) {
            mTextureView.setScreenScale(screenScale);
        }
    }

    public void setVideoSize(int width, int height) {
        if (mTextureView != null) {
            mTextureView.setVideoSize(width, height);
        }
    }

    /*
     * 设置镜像
     */
    public void setIsMirror(boolean b) {
        if (mTextureView != null) {
            mTextureView.setScaleX(b ? -1 : 1);
        }
    }

    /*
     * 获取截图
     */
    public Bitmap getScreenShot() {
        if (mTextureView != null) {
            return mTextureView.getBitmap();
        }
        return null;
    }

    /*
     * 旋转屏幕
     */
    public void rotateScreen(float rotation) {
        if (mTextureView != null) {
            mTextureView.setRotation(rotation);
            mTextureView.requestLayout();
        }
    }

    @Override
    public void mOnCompletion() {
        super.mOnCompletion();
        if (mListener != null) {
            mListener.onCompletion();
            return;
        }

        if (mCurrentPlayMode == PLAY_MODE_NORMAL) {//播放完成

        } else if (mCurrentPlayMode == PLAY_MODE_LOOP) {//单集循环
            restart();
        } else {
            showToast("没有视频了");
        }
    }

    @Override
    public void mOnVideoSizeChanged(int width, int height) {
        super.mOnVideoSizeChanged(width, height);
        mTextureView.setScreenScale(mCurrentScreenScale);
        mTextureView.setVideoSize(width, height);
        mVideoWidth = width;
        mVideoHeight = height;
    }
}
