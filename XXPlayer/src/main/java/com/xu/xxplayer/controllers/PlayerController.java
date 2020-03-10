package com.xu.xxplayer.controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xu.xxplayer.R;
import com.xu.xxplayer.adapters.MenuAdapter;
import com.xu.xxplayer.adapters.VideoAdapter;
import com.xu.xxplayer.players.BasePlayerView;
import com.xu.xxplayer.players.IjkPlayerView;
import com.xu.xxplayer.utils.ConnectivityStatus;
import com.xu.xxplayer.utils.ReactiveNetwork;
import com.xu.xxplayer.utils.XXPlayerUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PlayerController extends BaseController implements View.OnClickListener {

    private static final String TAG = "PlayerController";

    /* ui */
    private ImageView iv_thumb;
    //topbar
    private LinearLayout layout_topbar, layout_tobar_right;
    private ImageView iv_back, iv_net, iv_battery, iv_more;
    private TextView tv_title, tv_current_time;
    //bottombar
    private FrameLayout layout_bottombar;
    private ImageView iv_play, iv_next, iv_screen_rotation, iv_fullscreen;
    private TextView tv_current, tv_duration, tv_openlist;
    private SeekBar sb_play;
    //弹出框,改变进度、声音、亮度
    private FrameLayout layout_position, layout_brightness, layout_volume;
    private TextView tv_position, tv_brightness, tv_volume;
    private ProgressBar pb_position, pb_brightness, pb_volume;
    private ImageView iv_brightness, iv_volume;
    //中间的按钮
    private LinearLayout layout_playback, layout_float;
    private FrameLayout layout_screenshot;
    private Button btn_replay, btn_playnext;
    private ImageView iv_capture, iv_lock, iv_screenshot, iv_start;
    //加载
    private ProgressBar pb_loading;
    //左侧视频列表
    private RecyclerView rv_video;
    //右侧菜单
    private ScrollView sv_menu;
    private LinearLayout layout_mirror, layout_antirotate, layout_rotate;
    private ImageView iv_mirror;
    private TextView tv_mirror;
    private RecyclerView rv_speed, rv_scale, rv_mode;
    /* ui */

    private GestureDetector mDestector;//手势控制
    private boolean mChangePosition = false;
    private boolean mChangeVolume = false;
    private boolean mChangeBrightness = false;
    private long toPosition = 0l;
    private float mBrightness;
    private int mVolume;

    private boolean isLock = false;
    private boolean isMirror = false;
    private int mRotation = 0;
    private VideoAdapter videoAdapter;
    private MenuAdapter speedAdapter, scaleAdapter, modeAdapter;

    private int statusbarHeight = 0;//状态栏高度
    private Animation showMenu, hideMenu;

    //wifi监听器
    private CompositeDisposable disposables;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mPlayerView.isIdle()) {
            mDestector.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //松手时seekTo
            if (mChangePosition && !isLiveTV) {
                mPlayerView.seekTo(toPosition);
            }
        }
        return true;
    }

    public PlayerController(Context context) {
        this(context, null);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);

        initEvent();
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);
        iv_more.setOnClickListener(this);

        iv_play.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        tv_openlist.setOnClickListener(this);
        iv_screen_rotation.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);

        iv_start.setOnClickListener(this);
        iv_capture.setOnClickListener(this);
        iv_lock.setOnClickListener(this);
        btn_replay.setOnClickListener(this);
        btn_playnext.setOnClickListener(this);

        layout_mirror.setOnClickListener(this);
        layout_antirotate.setOnClickListener(this);
        layout_rotate.setOnClickListener(this);

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
                if (isShowBar) {
                    hideBar();
                } else {
                    showBar();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "onDoubleTap: 双击");
                if (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying()) {
                    mPlayerView.pause();
                } else if (mPlayerView.isPaused() || mPlayerView.isBufferingPaused() ||
                        mPlayerView.isCompleted() || mPlayerView.isError()) {
                    mPlayerView.restart();
                    hideBar();
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown: 按下");
                mChangePosition = false;
                mChangeVolume = false;
                mChangeBrightness = false;
                Window window = ((Activity) getContext()).getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                mBrightness = attributes.screenBrightness;
                mVolume = mPlayerView.getVolume();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float deltaX = e1.getX() - e2.getX();
                float deltaY = e1.getY() - e2.getY();
                Log.d(TAG, "onScroll: 滑动" + deltaX + " " + deltaY);
                if (!(mChangePosition || mChangeVolume || mChangeBrightness) &&
                        mPlayerView.isFullScreen() &&
                        !isShowMenu && !isShowList &&
                        !isLock) {
                    mChangePosition = Math.abs(distanceX) >= Math.abs(distanceY);
                    if (!mChangePosition) {
                        if (e1.getX() > 0.5f * getWidth()) {
                            mChangeVolume = true;
                        } else {
                            mChangeBrightness = true;
                        }
                    }
                }
                if (mChangePosition && !isLiveTV) {
                    changePosition(-deltaX);
                }
                if (mChangeBrightness) {
                    changeBrightness(deltaY);
                }
                if (mChangeVolume) {
                    changeVolume(deltaY);
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {//返回
            ((Activity) getContext()).onBackPressed();
        } else if (id == R.id.iv_more) {//菜单
            showMenu();
        } else if (id == R.id.iv_play) {//播放、暂停
            if (mPlayerView.isPlaying() || mPlayerView.isBufferingPlaying()) {
                mPlayerView.pause();
            } else if (mPlayerView.isPaused() || mPlayerView.isBufferingPaused()) {
                mPlayerView.restart();
            } else if (mPlayerView.isCompleted() || mPlayerView.isError()) {
                replay();
            }
        } else if (id == R.id.iv_next) {//播放下集
            if (urlPosition < mUrlList.size() - 1) {
                playNext(urlPosition + 1);
            } else {
                showToast("没有视频了");
            }
        } else if (id == R.id.iv_screen_rotation) {//旋转屏幕
            switch (getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    //隐藏状态栏
                    ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    //显示状态栏
                    ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    break;
            }
        } else if (id == R.id.iv_fullscreen) {//进入全屏、退出全屏
            if (mPlayerView.isFullScreen()) {
                mPlayerView.exitFullScreen();
            } else {
                mPlayerView.enterFullScreen();
            }
        } else if (id == R.id.tv_openlist) {//打开列表
            showList();
        } else if (id == R.id.iv_start) {//开始播放
            mPlayerView.start();
        } else if (id == R.id.iv_capture) {//截图
            getScreenshot();
        } else if (id == R.id.iv_lock) {//锁定屏幕
            if (isLock) {//解锁，先解锁再显示状态栏
                isLock = false;
                iv_lock.setImageResource(R.drawable.ic_unlock);
                layout_topbar.startAnimation(showTop);
                layout_topbar.setVisibility(VISIBLE);
                layout_bottombar.startAnimation(showBottom);
                layout_bottombar.setVisibility(VISIBLE);
            } else {//锁定，先隐藏状态栏再锁定
                isLock = true;
                iv_lock.setImageResource(R.drawable.ic_lock);
                layout_topbar.startAnimation(hideTop);
                layout_topbar.setVisibility(GONE);
                layout_bottombar.startAnimation(hideBottom);
                layout_bottombar.setVisibility(GONE);
            }
        } else if (id == R.id.btn_replay) {//重新播放
            replay();
        } else if (id == R.id.btn_playnext) {//播放下集
            if (urlPosition < mUrlList.size() - 1) {
                playNext(urlPosition + 1);
            } else {
                showToast("没有视频了");
            }
        } else if (id == R.id.layout_mirror) {//镜像翻转
            if (isMirror) {
                isMirror = false;
                iv_mirror.setImageResource(R.drawable.ic_mirror_off);
                tv_mirror.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
                ((IjkPlayerView) mPlayerView).setIsMirror(false);
            } else {
                isMirror = true;
                iv_mirror.setImageResource(R.drawable.ic_mirror_on);
                tv_mirror.setTextColor(getContext().getResources().getColor(R.color.colorPlayerBlue));
                ((IjkPlayerView) mPlayerView).setIsMirror(true);
            }
        } else if (id == R.id.layout_antirotate) {//逆时针旋转
            mRotation = mRotation - 90;
            ((IjkPlayerView) mPlayerView).rotateScreen(mRotation);
        } else if (id == R.id.layout_rotate) {//顺时针旋转
            mRotation = mRotation + 90;
            ((IjkPlayerView) mPlayerView).rotateScreen(mRotation);
        }
    }

    /**
     * 获取截图
     */
    private void getScreenshot() {
        Bitmap bitmap = ((IjkPlayerView) mPlayerView).getScreenShot();
        if (bitmap == null) {
            showToast("截图失败");
            return;
        }
        showToast("截图成功");

        layout_screenshot.setVisibility(VISIBLE);
        Glide.with(getContext())
                .load(((IjkPlayerView) mPlayerView).getScreenShot())
                .into(iv_screenshot);
        XXPlayerUtil.saveBitmap(getContext(), bitmap);
    }

    /**
     * 播放下一集
     *
     * @param position
     */
    private void playNext(int position) {
        urlPosition = position;
        videoAdapter.setSelectItem(position);
        mPlayerView.playNext(mUrlList.get(position));
        ((IjkPlayerView) mPlayerView).setBackground(mUrlList.get(position));
        setThumb(mUrlList.get(position));
        setTitle(new File(mUrlList.get(position)).getName());
    }

    /**
     * 改变进度条
     *
     * @param deltaX
     */
    public void changePosition(float deltaX) {
        long duration = mPlayerView.getDuration();
        long currentPositon = mPlayerView.getCurrentPosition();
        //滑动一格+10s
        int detlaPosition = (int) (deltaX / threshold * 10);
        toPosition = currentPositon + detlaPosition * 1000;
        toPosition = Math.max(0, Math.min(duration, toPosition));
        //滑动结束再seekTo
        //seekTo(toPosition);
        sb_play.setProgress((int) toPosition);

        int newPositionProgress = (int) (100f * toPosition / duration);
        layout_position.setVisibility(View.VISIBLE);
        layout_brightness.setVisibility(GONE);
        layout_volume.setVisibility(GONE);
        pb_position.setProgress(newPositionProgress);
        tv_position.setText(XXPlayerUtil.stringForTime(toPosition));
        tv_current.setText(XXPlayerUtil.stringForTime(toPosition));

        UIHandler.removeMessages(UPDATE_SEEKBAR);
        UIHandler.removeMessages(HIDE_POSITION_BAR);
        UIHandler.sendEmptyMessageDelayed(HIDE_POSITION_BAR, 1000);
    }

    /**
     * 改变亮度
     *
     * @param deltaY
     */
    public void changeBrightness(float deltaY) {
        if (mBrightness == -1.0f) mBrightness = 0.5f;
        float newBrightness = mBrightness + deltaY / getHeight() * 2;
        // 0 <= newBrightness <= 1
        newBrightness = Math.max(0f, Math.min(newBrightness, 1.0f));
        Window window = ((Activity) getContext()).getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.screenBrightness = newBrightness;
        window.setAttributes(attributes);

        int newBrightnessProgress = (int) (100f * newBrightness);
        layout_brightness.setVisibility(VISIBLE);
        layout_position.setVisibility(GONE);
        layout_volume.setVisibility(GONE);
        iv_brightness.setImageResource(R.drawable.ic_brightness);
        tv_brightness.setText(newBrightnessProgress + "%");
        pb_brightness.setProgress(newBrightnessProgress);

        UIHandler.removeMessages(HIDE_BRIGHTNESS_BAR);
        UIHandler.sendEmptyMessageDelayed(HIDE_BRIGHTNESS_BAR, 1000);
    }

    /**
     * 改变声音
     *
     * @param deltaY
     */
    public void changeVolume(float deltaY) {
        int maxVolume = mPlayerView.getMaxVolume();
        float newVolume = mVolume + (deltaY * maxVolume / getHeight() * 2);
        // 0 <= newVolume <= maxVolume
        newVolume = Math.max(0, Math.min(newVolume, maxVolume));
        mPlayerView.setVolume((int) newVolume);

        int newVolumeProgress = (int) (100f * newVolume / maxVolume);
        layout_volume.setVisibility(VISIBLE);
        layout_position.setVisibility(GONE);
        layout_brightness.setVisibility(GONE);
        if (newVolumeProgress > 0) {
            iv_volume.setImageResource(R.drawable.ic_volume);
        } else {
            iv_volume.setImageResource(R.drawable.ic_volume_mute);
        }
        tv_volume.setText(newVolumeProgress + "%");
        pb_volume.setProgress(newVolumeProgress);

        UIHandler.removeMessages(HIDE_VOLUME_BAR);
        UIHandler.sendEmptyMessageDelayed(HIDE_VOLUME_BAR, 1000);
    }

    public void showBar() {
        if (isShowMenu) {
            hideMenu();
        }
        if (isShowList) {
            hideList();
        }
        if (isShowBar) return;
        isShowBar = true;

        if (!isLock) {
            layout_topbar.startAnimation(showTop);
            layout_topbar.setVisibility(VISIBLE);
            layout_bottombar.startAnimation(showBottom);
            layout_bottombar.setVisibility(VISIBLE);
        }
        if (mPlayerView.isFullScreen()) {
            layout_float.setVisibility(VISIBLE);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                //显示状态栏
                Window window = ((Activity) getContext()).getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    public void hideBar() {
        if (!isShowBar) return;
        isShowBar = false;

        if (!isLock) {
            layout_topbar.startAnimation(hideTop);
            layout_topbar.setVisibility(GONE);
            layout_bottombar.startAnimation(hideBottom);
            layout_bottombar.setVisibility(GONE);
        }
        if (mPlayerView.isFullScreen()) {
            layout_float.setVisibility(GONE);
            layout_screenshot.setVisibility(GONE);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                //隐藏状态栏
                Window window = ((Activity) getContext()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    public void showList() {
        if (isShowList) return;
        isShowList = true;

        hideBar();
        rv_video.startAnimation(showMenu);
        rv_video.setVisibility(VISIBLE);

        rv_video.scrollToPosition(urlPosition);
    }

    public void hideList() {
        if (!isShowList) return;
        isShowList = false;

        rv_video.startAnimation(hideMenu);
        rv_video.setVisibility(GONE);
    }

    public void showMenu() {
        if (isShowMenu) return;
        isShowMenu = true;

        hideBar();
        sv_menu.startAnimation(showMenu);
        sv_menu.setVisibility(VISIBLE);
    }

    public void hideMenu() {
        if (!isShowMenu) return;
        isShowMenu = false;

        sv_menu.startAnimation(hideMenu);
        sv_menu.setVisibility(GONE);
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
                case UPDATE_TIME:
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                    String formattedDate = df.format(calendar.getTime());
                    //更新系统时间
                    tv_current_time.setText(formattedDate);
                    UIHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
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
                case HIDE_POSITION_BAR:
                    layout_position.setVisibility(GONE);
                    UIHandler.sendEmptyMessage(UPDATE_SEEKBAR);
                    break;
                case HIDE_BRIGHTNESS_BAR:
                    layout_brightness.setVisibility(GONE);
                    break;
                case HIDE_VOLUME_BAR:
                    layout_volume.setVisibility(GONE);
                    break;
            }
        }
    };

    @Override
    protected void initView() {
        super.initView();
        iv_thumb = mView.findViewById(R.id.iv_thumb);
        //topbar
        layout_topbar = mView.findViewById(R.id.layout_topbar);
        layout_tobar_right = mView.findViewById(R.id.layout_tobar_right);
        iv_back = mView.findViewById(R.id.iv_back);
        iv_net = mView.findViewById(R.id.iv_net);
        iv_battery = mView.findViewById(R.id.iv_battery);
        iv_more = mView.findViewById(R.id.iv_more);
        tv_title = mView.findViewById(R.id.tv_title);
        tv_current_time = mView.findViewById(R.id.tv_current_time);
        //bottombar
        layout_bottombar = mView.findViewById(R.id.layout_bottombar);
        iv_play = mView.findViewById(R.id.iv_play);
        iv_next = mView.findViewById(R.id.iv_next);
        iv_screen_rotation = mView.findViewById(R.id.iv_screen_rotation);
        iv_fullscreen = mView.findViewById(R.id.iv_fullscreen);
        tv_current = mView.findViewById(R.id.tv_current);
        tv_duration = mView.findViewById(R.id.tv_duration);
        tv_openlist = mView.findViewById(R.id.tv_openlist);
        sb_play = mView.findViewById(R.id.sb_play);
        //弹出框,改变进度、声音、亮度
        layout_position = mView.findViewById(R.id.layout_position);
        layout_brightness = mView.findViewById(R.id.layout_brightness);
        layout_volume = mView.findViewById(R.id.layout_volume);
        tv_position = mView.findViewById(R.id.tv_position);
        tv_brightness = mView.findViewById(R.id.tv_brightness);
        tv_volume = mView.findViewById(R.id.tv_volume);
        pb_position = mView.findViewById(R.id.pb_position);
        pb_brightness = mView.findViewById(R.id.pb_brightness);
        pb_volume = mView.findViewById(R.id.pb_volume);
        iv_brightness = mView.findViewById(R.id.iv_brightness);
        iv_volume = mView.findViewById(R.id.iv_volume);
        //中间的按钮
        layout_playback = mView.findViewById(R.id.layout_playback);
        layout_float = mView.findViewById(R.id.layout_float);
        layout_screenshot = mView.findViewById(R.id.layout_screenshot);
        btn_replay = mView.findViewById(R.id.btn_replay);
        btn_playnext = mView.findViewById(R.id.btn_playnext);
        iv_capture = mView.findViewById(R.id.iv_capture);
        iv_lock = mView.findViewById(R.id.iv_lock);
        iv_screenshot = mView.findViewById(R.id.iv_screenshot);
        iv_start = mView.findViewById(R.id.iv_start);
        //加载
        pb_loading = mView.findViewById(R.id.pb_loading);
        //左侧视频列表
        rv_video = mView.findViewById(R.id.rv_video);
        //右侧菜单
        sv_menu = mView.findViewById(R.id.sv_menu);
        layout_mirror = mView.findViewById(R.id.layout_mirror);
        layout_antirotate = mView.findViewById(R.id.layout_antirotate);
        layout_rotate = mView.findViewById(R.id.layout_rotate);
        iv_mirror = mView.findViewById(R.id.iv_mirror);
        tv_mirror = mView.findViewById(R.id.tv_mirror);
        rv_speed = mView.findViewById(R.id.rv_speed);
        rv_scale = mView.findViewById(R.id.rv_scale);
        rv_mode = mView.findViewById(R.id.rv_mode);
        initMenuList();

        showMenu = AnimationUtils.loadAnimation(getContext(), R.anim.show_right);
        hideMenu = AnimationUtils.loadAnimation(getContext(), R.anim.hide_right);
    }

    /**
     * 初始化菜单列表
     */
    public void initMenuList() {
        speedAdapter = new MenuAdapter(getContext(), "speed");
        rv_speed.setAdapter(speedAdapter);
        speedAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                mPlayerView.setPlaySpeed(speedAdapter.getSpeed(position));
            }
        });

        scaleAdapter = new MenuAdapter(getContext(), "scale");
        rv_scale.setAdapter(scaleAdapter);
        scaleAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                mPlayerView.setScreenScale(scaleAdapter.getScale(position));
            }
        });

        modeAdapter = new MenuAdapter(getContext(), "mode");
        rv_mode.setAdapter(modeAdapter);
        modeAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                mPlayerView.setPlayMode(modeAdapter.getMode(position));
            }
        });
    }

    /**
     * 初始化菜单数据
     */
    private void initMenuData() {
        //初始化镜像
        isMirror = false;
        iv_mirror.setImageResource(R.drawable.ic_mirror_off);
        tv_mirror.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
        ((IjkPlayerView) mPlayerView).setIsMirror(false);
        //初始化旋转角度
        mRotation = 0;
        ((IjkPlayerView) mPlayerView).rotateScreen(0);
        //初始化播放速度
        speedAdapter.setSelectItem(speedAdapter.getSpeedPosition(mPlayerView.getPlaySpeed() + ""));
        //初始化画面尺寸
        scaleAdapter.setSelectItem(mPlayerView.getScreenScale());
        //初始化播放模式
        modeAdapter.setSelectItem(mPlayerView.getPlayMode());
    }

    @Override
    public void setUrl(String url, final List<String> urlList) {
        super.setUrl(url, urlList);
        if (mUrlList == null) return;
        iv_next.setVisibility(VISIBLE);

        videoAdapter = new VideoAdapter(getContext(), urlList);
        rv_video.setAdapter(videoAdapter);
        videoAdapter.setSelectItem(urlPosition);

        videoAdapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                playNext(position);
            }
        });
    }

    @Override
    public void setPlayer(BasePlayerView basePlayerView) {
        super.setPlayer(basePlayerView);

        ((IjkPlayerView) mPlayerView).setOnCompletionListener(new IjkPlayerView.onCompletionListener() {
            @Override
            public void onCompletion() {
                Log.d(TAG, "onCompletion: " + mPlayerView.getPlayMode());
                switch (mPlayerView.getPlayMode()) {
                    case BasePlayerView.PLAY_MODE_NORMAL:
                        break;
                    case BasePlayerView.PLAY_MODE_LOOP:
                        mPlayerView.restart();
                        break;
                    case BasePlayerView.PLAY_MODE_LIST:
                        if (urlPosition < mUrlList.size() - 1) {
                            playNext(urlPosition + 1);
                        } else {
                            showToast("没有视频了");
                        }
                        break;
                    case BasePlayerView.PLAY_MODE_LIST_LOOP:
                        if (urlPosition < mUrlList.size() - 1) {
                            playNext(urlPosition + 1);
                        } else {
                            playNext(0);
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void replay() {
        super.replay();
        //设置镜像
        ((IjkPlayerView) mPlayerView).setIsMirror(isMirror);
        //设置旋转角度
        ((IjkPlayerView) mPlayerView).rotateScreen(mRotation);
        //设置播放速度
        mPlayerView.setPlaySpeed(speedAdapter.getSpeed(speedAdapter.getSelect()));
        //设置画面尺寸
        mPlayerView.setScreenScale(scaleAdapter.getScale(scaleAdapter.getSelect()));
        //设置播放模式
        mPlayerView.setPlayMode(modeAdapter.getMode(modeAdapter.getSelect()));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.player_controller;
    }

    @Override
    public void reset() {
        UIHandler.removeMessages(UPDATE_TIME);
        UIHandler.sendEmptyMessage(UPDATE_TIME);

        isShowBar = false;
        isShowMenu = false;
        isShowList = false;

        sb_play.setProgress(0);
        sb_play.setSecondaryProgress(0);

        layout_topbar.setVisibility(GONE);
        layout_tobar_right.setVisibility(GONE);
        layout_bottombar.setVisibility(GONE);
        iv_next.setVisibility(GONE);
        tv_openlist.setVisibility(GONE);
        iv_screen_rotation.setVisibility(GONE);

        layout_position.setVisibility(GONE);
        layout_brightness.setVisibility(GONE);
        layout_volume.setVisibility(GONE);

        layout_playback.setVisibility(GONE);
        layout_float.setVisibility(GONE);

        pb_loading.setVisibility(GONE);

        rv_video.setVisibility(GONE);
        sv_menu.setVisibility(GONE);

        iv_start.setVisibility(VISIBLE);
        showThumb();

        initMenuData();

        onOrientationChanged(Configuration.ORIENTATION_PORTRAIT);
    }

    private void showThumb() {
        ViewGroup.LayoutParams params = iv_thumb.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        iv_thumb.setLayoutParams(params);
        iv_thumb.setVisibility(View.VISIBLE);
    }

    /**
     * 设置缩略图
     *
     * @param resId
     */
    public void setThumb(@DrawableRes int resId) {
        iv_thumb.setImageResource(resId);
    }

    /**
     * 设置缩略图
     *
     * @param url
     */
    public void setThumb(String url) {
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .into(iv_thumb);
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        tv_title.setText(title);
        tv_title.setSelected(true);
    }

    public void setStatusbarHeight(int height) {
        statusbarHeight = height;
        layout_topbar.setPadding(0, statusbarHeight, 0, 0);
    }

    @Override
    public void onPlayStateChanged(int state) {
        Log.d(TAG, "onPlayStateChanged: " + state);
        switch (state) {
            case BasePlayerView.STATE_IDLE:
                break;
            case BasePlayerView.STATE_PREPARING:
                hideBar();

                layout_position.setVisibility(GONE);
                layout_brightness.setVisibility(GONE);
                layout_volume.setVisibility(GONE);

                layout_playback.setVisibility(GONE);
                layout_float.setVisibility(GONE);

                iv_start.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                break;
            case BasePlayerView.STATE_PLAYING:
                iv_thumb.setVisibility(GONE);
                iv_start.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                layout_playback.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_pause);

                UIHandler.sendEmptyMessage(START_PLAYING);
                UIHandler.sendEmptyMessage(UPDATE_PLAYER);
                UIHandler.sendEmptyMessage(UPDATE_SEEKBAR);
                break;
            case BasePlayerView.STATE_PAUSED:
                iv_thumb.setVisibility(GONE);
                iv_start.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                layout_playback.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_play);
                break;
            case BasePlayerView.STATE_BUFFERING_PLAYING:
                iv_thumb.setVisibility(GONE);
                iv_start.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                layout_playback.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_pause);
                break;
            case BasePlayerView.STATE_BUFFERING_PAUSED:
                iv_thumb.setVisibility(GONE);
                iv_start.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                layout_playback.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_play);
                break;
            case BasePlayerView.STATE_COMPLETED:
            case BasePlayerView.STATE_ERROR:
                iv_start.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                iv_play.setImageResource(R.drawable.ic_refresh);
                layout_playback.setVisibility(VISIBLE);
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
                iv_fullscreen.setImageResource(R.drawable.ic_fullscreen);
                tv_openlist.setVisibility(GONE);
                layout_float.setVisibility(GONE);
                if (isLock) {
                    hideBar();
                    iv_lock.setImageResource(R.drawable.ic_unlock);
                    isLock = false;
                }
                iv_screen_rotation.setVisibility(GONE);
                iv_fullscreen.setVisibility(VISIBLE);
                break;
            case BasePlayerView.SCREEN_MODE_FULLSCREEN:
                iv_fullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
                if (mUrlList != null) {
                    tv_openlist.setVisibility(VISIBLE);
                }
                if (isShowBar) {
                    layout_float.setVisibility(VISIBLE);
                }
                iv_screen_rotation.setVisibility(VISIBLE);
                iv_fullscreen.setVisibility(GONE);
                break;
            case BasePlayerView.SCREEN_MODE_TINYSCREEN:
                break;
        }
    }

    public void onOrientationChanged(int orientation) {
        ViewGroup.LayoutParams menuParams = sv_menu.getLayoutParams();
        ViewGroup.LayoutParams videolistParams = rv_video.getLayoutParams();
        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                layout_topbar.setPadding(0, statusbarHeight, 0, 0);
                layout_tobar_right.setVisibility(GONE);
                //关闭wifi、电池监听
                closeWifiReceiver();
                closeBatteryReceiver();
                layout_playback.setOrientation(LinearLayout.VERTICAL);
                showMenu = AnimationUtils.loadAnimation(getContext(), R.anim.show_bottom);
                hideMenu = AnimationUtils.loadAnimation(getContext(), R.anim.hide_bottom);
                menuParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                menuParams.height = XXPlayerUtil.dp2px(getContext(), 400);
                sv_menu.setLayoutParams(menuParams);
                videolistParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                videolistParams.height = XXPlayerUtil.dp2px(getContext(), 400);
                rv_video.setLayoutParams(videolistParams);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                layout_topbar.setPadding(0, 0, 0, 0);
                layout_tobar_right.setVisibility(VISIBLE);
                //开启wifi、电池监听
                openWifiReceiver();
                openBatteryReceiver();
                layout_playback.setOrientation(LinearLayout.HORIZONTAL);
                showMenu = AnimationUtils.loadAnimation(getContext(), R.anim.show_right);
                hideMenu = AnimationUtils.loadAnimation(getContext(), R.anim.hide_right);
                menuParams.width = XXPlayerUtil.dp2px(getContext(), 320);
                menuParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                sv_menu.setLayoutParams(menuParams);
                videolistParams.width = XXPlayerUtil.dp2px(getContext(), 250);
                videolistParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                rv_video.setLayoutParams(videolistParams);
                break;
        }
    }

    /**
     * 开启wifi监听
     */
    private void openWifiReceiver() {
        ReactiveNetwork reactiveNetwork = new ReactiveNetwork();
        if (null == disposables) {
            disposables = new CompositeDisposable();
        }

        //监听网络连接类型的 （数据流量 、wifi 、断线）
        Disposable nc = reactiveNetwork.observeNetworkConnectivity(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ConnectivityStatus>() {
                    @Override
                    public void accept(final ConnectivityStatus status) throws Exception {
                        Log.d(TAG, "accept: " + status.toString());
                        if (status.description.equals("WiFi")) {
                            iv_net.setImageResource(R.drawable.ic_wifi_4);
                        } else if (status.description.equals("Mobile")) {
                            iv_net.setImageResource(R.drawable.ic_mobile_net);
                        } else if (status.description.equals("No Network")) {
                            iv_net.setImageResource(R.drawable.ic_no_net);
                        }
                    }
                });
        disposables.add(nc);

        //监听wifi强度
        Disposable wi = reactiveNetwork.observeWifiInfo(getContext())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WifiInfo>() {
                    @Override
                    public void accept(WifiInfo info) {
                        int strength = 100 + info.getRssi();
                        if (strength >= 0 && strength < 10) {
                            //wifi信号0~10
                            iv_net.setImageResource(R.drawable.ic_wifi_0);
                        } else if (strength >= 10 && strength < 20) {
                            //wifi信号10~20
                            iv_net.setImageResource(R.drawable.ic_wifi_1);
                        } else if (strength >= 20 && strength < 30) {
                            //wifi信号20~30
                            iv_net.setImageResource(R.drawable.ic_wifi_2);
                        } else if (strength >= 30 && strength < 40) {
                            //wifi信号30~40
                            iv_net.setImageResource(R.drawable.ic_wifi_3);
                        } else if (strength >= 40 && strength <= 50) {
                            //wifi信号40~50
                            iv_net.setImageResource(R.drawable.ic_wifi_4);
                        }
                    }
                });
        disposables.add(wi);
    }

    /**
     * 关闭wifi监听
     */
    private void closeWifiReceiver() {
        if (null != disposables) {
            disposables.dispose();
        }
    }

    /**
     * 开启电池监听
     */
    private void openBatteryReceiver() {
        getContext().registerReceiver(mBatterReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /**
     * 关闭电池监听
     */
    private void closeBatteryReceiver() {
        try {
            getContext().unregisterReceiver(mBatterReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 电池广播接收器
     */
    private BroadcastReceiver mBatterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                iv_battery.setImageResource(R.drawable.ic_battery_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                iv_battery.setImageResource(R.drawable.ic_battery_full);
            } else {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int percentage = (int) (((float) level / scale) * 100);
                if (percentage <= 10) {
                    iv_battery.setImageResource(R.drawable.ic_battery_10);
                } else if (percentage <= 20) {
                    iv_battery.setImageResource(R.drawable.ic_battery_20);
                } else if (percentage <= 50) {
                    iv_battery.setImageResource(R.drawable.ic_battery_50);
                } else if (percentage <= 80) {
                    iv_battery.setImageResource(R.drawable.ic_battery_80);
                } else if (percentage <= 100) {
                    iv_battery.setImageResource(R.drawable.ic_battery_100);
                }
            }
        }
    };
}
