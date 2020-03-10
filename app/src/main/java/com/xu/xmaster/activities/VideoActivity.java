package com.xu.xmaster.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.VideoAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.utils.FileManager;
import com.xu.xmaster.views.SimpleToolbar;
import com.xu.xxplayer.controllers.BaseController;
import com.xu.xxplayer.controllers.NormalController;
import com.xu.xxplayer.controllers.PlayerController;
import com.xu.xxplayer.players.VideoPlayerView;

public class VideoActivity extends BaseActivity {

    private static final String TAG = "VideoActivity";

    private AppBarLayout app_bar;
    private VideoPlayerView mPlayerView;
    private Toolbar toolbar;
    private SimpleToolbar simpleToolbar;
    private RecyclerView rv_video;

    private VideoAdapter videoAdapter;

    private String mUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_video);

        mUrl = getIntent().getStringExtra("path");

        initView();
        initEvent();
    }

    private void initView() {
        app_bar = findViewById(R.id.app_bar);
        mPlayerView = findViewById(R.id.mPlayerView);
        toolbar = findViewById(R.id.toolbar);
        simpleToolbar = findViewById(R.id.simpleToolbar);
        rv_video = findViewById(R.id.rv_video);

        videoAdapter = new VideoAdapter(getContext(), FileManager.loadVideoBean(getContext()));
        rv_video.setAdapter(videoAdapter);
        setPosition();

        initToolbar();
        initPlayer();
    }

    private void setPosition() {
        int position = videoAdapter.getPosition(mUrl);
        videoAdapter.setSelect(position);
        rv_video.scrollToPosition(position);
    }

    private void initToolbar() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        params.topMargin = QMUIStatusBarHelper.getStatusbarHeight(getContext());
        toolbar.setLayoutParams(params);

        simpleToolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        simpleToolbar.setRightBtn1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPlayerView.isTinyScreen()) {
                    mPlayerView.enterTinyScreen();
                    mPlayerView.restart();
                }
            }
        });

        app_bar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                int height = app_bar.getHeight() -
                        toolbar.getHeight() -
                        QMUIStatusBarHelper.getStatusbarHeight(getContext());
                float alpha = (float) (1 - 1.0 * (height + i) / height);
                Log.d(TAG, "onOffsetChanged: " + alpha);
                if (alpha > 0.6) {
                    simpleToolbar.setVisibility(View.VISIBLE);
                    simpleToolbar.setAlpha(alpha);
                    if (!mPlayerView.isTinyScreen() && !mPlayerView.isFullScreen()) {
                        mPlayerView.pause();
                    }
                    if (!mPlayerView.isFullScreen()) {
                        //退出全屏
                        Window window = getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    }
                } else {
                    simpleToolbar.setVisibility(View.GONE);
                    if (mPlayerView.isTinyScreen()) {
                        mPlayerView.exitTinyScreen();
                    }
                    if (!mPlayerView.getController().isShowBar) {
                        //进入全屏
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    }
                }
            }
        });
    }

    private void initPlayer() {
        mPlayerView.setStatusbarHeight(QMUIStatusBarHelper.getStatusbarHeight(getContext()));
        mPlayerView.setController(new NormalController(getContext()));
        mPlayerView.setBackground(mUrl);
        mPlayerView.setUrl(mUrl, FileManager.loadVideo(getContext()));
        mPlayerView.start();
    }

    private void initEvent() {
        videoAdapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (videoAdapter.getSelect() != position) {
                    videoAdapter.setSelect(position);
                    mPlayerView.setBackground(videoAdapter.getUrl(position));
                    mPlayerView.playNext(videoAdapter.getUrl(position));
                }
                if (simpleToolbar.getVisibility() == View.VISIBLE &&
                        !mPlayerView.isTinyScreen()) {
                    mPlayerView.enterTinyScreen();
                    mPlayerView.restart();
                }
            }
        });

        mPlayerView.setOnPlayerViewListener(new VideoPlayerView.OnPlayerViewListener() {
            @Override
            public void onPlayNext(String url) {
                mUrl = url;
                setPosition();
            }

            @Override
            public void enterFullScreen() {
                setSwipeBackEnable(false);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: " + newConfig.orientation);
        BaseController controller = mPlayerView.getController();
        if (controller != null && controller instanceof PlayerController) {
            ((PlayerController) controller).onOrientationChanged(newConfig.orientation);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView.isFullScreen()) {
            BaseController controller = mPlayerView.getController();
            if (controller != null && controller instanceof PlayerController) {
                if (controller.isShowMenu) {
                    ((PlayerController) controller).hideMenu();
                } else if (controller.isShowList) {
                    ((PlayerController) controller).hideList();
                } else {
                    mPlayerView.exitFullScreen();
                    setSwipeBackEnable(true);
                }
            } else {
                mPlayerView.exitFullScreen();
                setSwipeBackEnable(true);
            }
        } else if (mPlayerView.isTinyScreen()) {
            mPlayerView.exitTinyScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerView.repause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayerView.isPlaying()) {
            mPlayerView.restart();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.release();
    }
}
