package com.xu.xxplayer.players;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.xu.xxplayer.controllers.BaseController;
import com.xu.xxplayer.utils.IPlayer;
import com.xu.xxplayer.utils.IPlayerEventListener;
import com.xu.xxplayer.utils.XXPlayerManager;

import java.io.IOException;
import java.util.List;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.content.Context.AUDIO_SERVICE;

public abstract class BasePlayerView extends FrameLayout implements IPlayer, IPlayerEventListener {

    private static final String TAG = "BasePlayerView";

    //播放状态
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_BUFFERING_PLAYING = 4;
    public static final int STATE_BUFFERING_PAUSED = 5;
    public static final int STATE_COMPLETED = 6;
    public static final int STATE_ERROR = 7;
    protected int mCurrentState = STATE_IDLE;

    //播放窗口
    public static final int SCREEN_MODE_NORMAL = 0;
    public static final int SCREEN_MODE_FULLSCREEN = 1;
    public static final int SCREEN_MODE_TINYSCREEN = 2;
    protected int mCurrentScreenMode = SCREEN_MODE_NORMAL;

    //画面尺寸
    public static final int SCREEN_SCALE_ADAPT = 0;//适应
    public static final int SCREEN_SCALE_STRETCH = 1;//拉伸
    public static final int SCREEN_SCALE_FILL = 2;//填充
    public static final int SCREEN_SCALE_16_9 = 3;
    public static final int SCREEN_SCALE_4_3 = 4;
    protected int mCurrentScreenScale = SCREEN_SCALE_ADAPT;

    //播放方式
    public static final int PLAY_MODE_NORMAL = 0;
    public static final int PLAY_MODE_LOOP = 1;//单集循环
    public static final int PLAY_MODE_LIST = 2;//顺序播放
    public static final int PLAY_MODE_LIST_LOOP = 3;//列表循环
    public static final int PLAY_MODE_RANDOM = 4;//随机播放
    protected int mCurrentPlayMode = PLAY_MODE_NORMAL;

    //播放器类型
    public static final int CODEC_IJK = 0;//ijkmediaplayer
    public static final int CODEC_ANDROID = 1;//mediaplayer
    protected int mCodecType = CODEC_IJK;

    protected IMediaPlayer mMediaPlayer;
    protected BaseController mController;
    protected AudioManager mAudioManager;

    protected String mUrl;
    protected List<String> mUrlList;

    protected Boolean isContinuePlay = false;

    private int mBufferPosition;

    public interface OnPlayStateChangedListener {
        void onPlayStateChanged(int state);
    }

    private OnPlayStateChangedListener onPlayStateChangedListener;

    public void setOnPlayStateChangedListener(OnPlayStateChangedListener listener) {
        onPlayStateChangedListener = listener;
    }

    public BasePlayerView(Context context) {
        this(context, null);
    }

    public BasePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        XXPlayerManager.instance().setCurrentPlayer(this);
        mAudioManager = (AudioManager) getContext().getSystemService(AUDIO_SERVICE);
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isFullScreen() {
        return mCurrentScreenMode == SCREEN_MODE_FULLSCREEN;
    }

    @Override
    public boolean isTinyScreen() {
        return mCurrentScreenMode == SCREEN_MODE_TINYSCREEN;
    }

    @Override
    public void setUrl(String url) {
        setUrl(url, null);
    }

    @Override
    public void setUrl(String url, List<String> urlList) {
        mUrl = url;
        mUrlList = urlList;
        if (mController != null) {
            mController.setUrl(url, urlList);
        }
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public List<String> getUrlList() {
        return mUrlList;
    }

    @Override
    public int getCurrentState() {
        return mCurrentState;
    }

    @Override
    public void setScreenScale(int screenScale) {
        mCurrentScreenScale = screenScale;
    }

    @Override
    public int getScreenScale() {
        return mCurrentScreenScale;
    }

    @Override
    public void setPlayMode(int playMode) {
        mCurrentPlayMode = playMode;
    }

    @Override
    public int getPlayMode() {
        return mCurrentPlayMode;
    }

    @Override
    public void setCodecType(int codecType) {
        mCodecType = codecType;
    }

    @Override
    public void setContinuePlay(boolean isContinuePlay) {
        this.isContinuePlay = isContinuePlay;
    }

    @Override
    public void start() {
        Log.d(TAG, "start: ");
        if (isIdle()) {
            initPlayer();
            startPrepare();
            setKeepScreenOn(true);
        }
    }

    @Override
    public void restart() {
        Log.d(TAG, "restart: ");
        if (mMediaPlayer != null) {
            if (isPaused()) {
                mMediaPlayer.start();
                setOnPlayStateChanged(STATE_PLAYING);
            } else if (isBufferingPaused()) {
                mMediaPlayer.start();
                setOnPlayStateChanged(STATE_BUFFERING_PLAYING);
            } else if (isCompleted() || isError()) {
                mMediaPlayer.reset();
                initPlayer();
                startPrepare();
                if (mController != null) {
                    mController.replay();
                }
            } else if (isPlaying() || isBufferingPlaying()) {
                mMediaPlayer.start();
            }
            setKeepScreenOn(true);
        }
    }

    @Override
    public void pause() {
        Log.d(TAG, "pause: ");
        if (mMediaPlayer != null) {
            if (isPlaying()) {
                mMediaPlayer.pause();
                setOnPlayStateChanged(STATE_PAUSED);
            } else if (isBufferingPlaying()) {
                mMediaPlayer.pause();
                setOnPlayStateChanged(STATE_BUFFERING_PAUSED);
            }
            setKeepScreenOn(false);
        }
    }

    @Override
    public void repause() {
        Log.d(TAG, "repause: ");
        if (mMediaPlayer != null) {
            //只暂停，不改变状态
            if (isPlaying() || isBufferingPlaying()) {
                mMediaPlayer.pause();
            }
            setKeepScreenOn(false);
        }
    }

    @Override
    public void release() {
        Log.d(TAG, "release: ");
        if (mMediaPlayer != null) {
            if (isPlaying() || isBufferingPlaying() || isBufferingPaused() || isPaused()) {
                saveCurrentPosition(getCurrentPosition());
            } else if (isCompleted()) {
                saveCurrentPosition(0);
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
            setOnPlayStateChanged(STATE_IDLE);
            setKeepScreenOn(false);
        }
        if (mController != null) {
            mController.reset();
        }
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public BaseController getController() {
        return mController;
    }

    public int getBufferPosition() {
        return mBufferPosition;
    }

    /**
     * 保存当前进度
     *
     * @param position
     */
    public void saveCurrentPosition(long position) {
        getContext().getSharedPreferences("XXPLAYER_POSITION",
                Context.MODE_PRIVATE)
                .edit()
                .putLong(mUrl, position)
                .apply();
    }

    /**
     * @return 获取保存进度
     */
    public long getSavePostion() {
        return getContext().getSharedPreferences("XXPLAYER_POSITION",
                Context.MODE_PRIVATE)
                .getLong(mUrl, 0);
    }

    @Override
    public void seekTo(long position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public int getMaxVolume() {
        if (mAudioManager != null) {
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    @Override
    public int getVolume() {
        if (mAudioManager != null) {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    @Override
    public void setVolume(int volume) {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
    }

    @Override
    public void setPlaySpeed(float speed) {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            ((IjkMediaPlayer) mMediaPlayer).setSpeed(speed);
        }
    }

    @Override
    public float getPlaySpeed() {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            return ((IjkMediaPlayer) mMediaPlayer).getSpeed(1.0f);
        }
        return 0;
    }

    @Override
    public long getTcpSpeed() {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            return ((IjkMediaPlayer) mMediaPlayer).getTcpSpeed();
        }
        return 0;
    }

    @Override
    public void setOnPlayStateChanged(int state) {
        mCurrentState = state;
        if (mController != null) {
            mController.onPlayStateChanged(state);
        }

        if (onPlayStateChangedListener != null) {
            onPlayStateChangedListener.onPlayStateChanged(state);
        }
    }

    @Override
    public void setOnScreenModeChanged(int mode) {
        mCurrentScreenMode = mode;
        if (mController != null) {
            mController.onScreenModeChanged(mode);
        }
    }

    @Override
    public void playNext(String url) {
        //完成上一个播放
        saveCurrentPosition(0);
        setOnPlayStateChanged(STATE_COMPLETED);
        //播放下一个
        mUrl = url;
        restart();
    }

    @Override
    public void mOnPrepared() {
        Log.d(TAG, "mOnPrepared: ");
        setOnPlayStateChanged(STATE_PLAYING);
        mMediaPlayer.start();

        if (isContinuePlay && getSavePostion() != 0) {
            Log.d(TAG, "mOnPrepared continue: " + getSavePostion());
            mMediaPlayer.seekTo(getSavePostion());
        }
    }

    @Override
    public void mOnInfo(int what, int extra) {
        Log.d(TAG, "mOnInfo: " + what + " " + extra);
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                // 播放器开始渲染
                setOnPlayStateChanged(STATE_PLAYING);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                // MediaPlayer暂时不播放，以缓冲更多的数据
                if (isPaused() || isBufferingPaused() || isCompleted()) {
                    setOnPlayStateChanged(STATE_BUFFERING_PAUSED);
                } else {
                    setOnPlayStateChanged(STATE_BUFFERING_PLAYING);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                if (isBufferingPlaying()) {
                    setOnPlayStateChanged(STATE_PLAYING);
                }
                if (isBufferingPaused()) {
                    setOnPlayStateChanged(STATE_PAUSED);
                }
                break;
        }
    }

    @Override
    public void mOnCompletion() {
        Log.d(TAG, "mOnCompletion: ");
        saveCurrentPosition(0);
        setOnPlayStateChanged(STATE_COMPLETED);
        setKeepScreenOn(false);
    }

    @Override
    public void mOnError(int what, int extra) {
        Log.d(TAG, "mOnError: " + what + " " + extra);
        setOnPlayStateChanged(STATE_ERROR);
        setKeepScreenOn(false);
    }

    @Override
    public void mOnVideoSizeChanged(int width, int height) {
        Log.d(TAG, "mOnVideoSizeChanged: " + width + " " + height);
    }

    @Override
    public void mOnBufferingUpdate(int what) {
        Log.d(TAG, "mOnBufferingUpdate: " + what);
        mBufferPosition = what;
    }

    protected void initPlayer() {
        switch (mCodecType) {
            case CODEC_ANDROID:
                mMediaPlayer = new AndroidMediaPlayer();
                break;
            case CODEC_IJK:
                mMediaPlayer = new IjkMediaPlayer();
                setOptions();
                break;
        }
        //音频类型
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //屏幕常亮
        mMediaPlayer.setScreenOnWhilePlaying(true);
        //加载完成
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        //视频信息
        mMediaPlayer.setOnInfoListener(onInfoListener);
        //视频宽高
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        //播放完成
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        //播放错误
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
    }

    protected void startPrepare() {
        Log.d(TAG, "startPrepare: ");
        if (TextUtils.isEmpty(mUrl)) {
            showToast("找不到播放链接");
            return;
        }
        try {
            mMediaPlayer.setDataSource(mUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开始加载
        mMediaPlayer.prepareAsync();
        setOnPlayStateChanged(STATE_PREPARING);
    }

    public void setOptions() {
//        //先硬解码，再软解码
//        ((IjkMediaPlayer) mMediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//        ((IjkMediaPlayer) mMediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
//        ((IjkMediaPlayer) mMediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
//        //变速不变调
//        ((IjkMediaPlayer) mMediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
//        //跳到关键帧
//        ((IjkMediaPlayer) mMediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//        //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L);
//        //播放前的最大探测时间
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
//        //播放前的探测时间1,达到首屏秒开效果
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);
//        //播放前的探测Size，默认是1M, 改小一点会出画面更快
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10);
//        //设置seekTo能够快速seek到指定位置并播放
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
//        //播放重连次数
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5);
//        //跳帧处理,放CPU处理较慢时，进行跳帧处理，保证播放流程，画面和声音同步
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);
//        //最大fps
//        ((IjkMediaPlayer) mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);
    }

    private IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mOnPrepared();
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            mOnInfo(i, i1);
            return true;
        }
    };

    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mOnCompletion();
        }
    };

    private IMediaPlayer.OnErrorListener onErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            mOnError(i, i1);
            return true;
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
            mOnVideoSizeChanged(i, i1);
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            mOnBufferingUpdate(i);
        }
    };

    protected void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
