package com.xu.xmaster.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.utils.FileManager;
import com.xu.xxplayer.controllers.PlayerController;
import com.xu.xxplayer.players.IjkPlayerView;

import java.io.File;

public class VideoPlayerActivity extends BaseActivity {

    private static final String TAG = "VideoPlayerActivity";

    private IjkPlayerView mPlayerView;
    private PlayerController mController;

    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setSwipeBackEnable(false);

        Uri uri = getIntent().getData();
        if (uri != null) {
            String[] mediaColumns = {MediaStore.Video.Media.DATA};
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, mediaColumns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(0);
            } else {
                //4.4之后Uri发生了变化，得到的cursor为null
                File file = new File(uri.getPath());
                path = file.getPath();
            }
        } else {
            path = getIntent().getStringExtra("path");
        }
        Log.d(TAG, "xxonCreate: " + path);

        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            String[] mediaColumns = {MediaStore.Video.Media.DATA};
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, mediaColumns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(0);
            } else {
                //4.4之后Uri发生了变化，得到的cursor为null
                File file = new File(uri.getPath());
                path = file.getPath();
            }
            mPlayerView.playNext(path);
            mPlayerView.setBackground(path);
            mController.setThumb(path);
            mController.setTitle(new File(path).getName());
        }
    }

    private void initView() {
        mPlayerView = new IjkPlayerView(getContext());
        setContentView(mPlayerView);
        mController = new PlayerController(getContext());

        mController.setTitle(new File(path).getName());
        mController.setThumb(path);
        mController.setStatusbarHeight(QMUIStatusBarHelper.getStatusbarHeight(getContext()));

        mPlayerView.setController(mController);
        mPlayerView.setBackground(path);
//        mPlayerView.setUrl(path);
        mPlayerView.setUrl(path, FileManager.loadVideo(getContext()));
        mPlayerView.start();
//        mPlayerView.enterFullScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mController != null) {
            mController.onOrientationChanged(newConfig.orientation);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView.isFullScreen()) {
            if (mController.isShowList) {
                mController.hideList();
            } else if (mController.isShowMenu) {
                mController.hideMenu();
            } else {
                mPlayerView.exitFullScreen();
//                super.onBackPressed();
            }
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
