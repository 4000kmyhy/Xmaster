package com.xu.xmaster.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.AttributeSet;

import com.xu.xmaster.Constant;
import com.xu.xmaster.beans.MusicBean;
import com.xu.xmaster.beans.MusicModel;
import com.xu.xmaster.services.MusicService;
import com.xu.xmaster.utils.net.INetCallBack;
import com.xu.xmaster.utils.net.OkHttpUtil;
import com.xu.xxplayer.players.BasePlayerView;
import com.xu.xxplayer.players.MusicPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyMusicPlayerView extends MusicPlayerView {

    private static final String TAG = "MusicPlayerView";

    private MusicModel mMusicModel;
    private MusicModel newMusicModel;
    private boolean isUrlFinish, isLyricFinish;

    private MusicService.MusicBind mMusicBinder;
    private Intent mServiceIntent;
    private boolean isBindService;

    public interface OnMusicPlayerListener {
        void setMusicModel(MusicModel musicModel);
    }

    private OnMusicPlayerListener onMusicPlayerListener;

    public void setOnMusicPlayerListener(OnMusicPlayerListener listener) {
        onMusicPlayerListener = listener;
    }

    public interface OnMusicChangedListener {
        void setSongmid(String songmid);
    }

    private OnMusicChangedListener onMusicChangedListener;

    public void setOnMusicChangedListener(OnMusicChangedListener listener) {
        onMusicChangedListener = listener;
    }

    public MyMusicPlayerView(Context context) {
        this(context, null);
    }

    public MyMusicPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMusicModel(MusicModel musicModel) {
        mMusicModel = musicModel;
    }

    public MusicModel getMusicModel() {
        return mMusicModel;
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

    @Override
    public void start() {
        super.start();
        startService();
    }

    @Override
    public void startService() {
        //管理service需要使用Application的Context
        if (mServiceIntent == null) {
            mServiceIntent = new Intent(getContext().getApplicationContext(), MusicService.class);
            getContext().getApplicationContext().startService(mServiceIntent);
        } else {
            mMusicBinder.playMusic(MyMusicPlayerView.this);
        }

        //当前未绑定，绑定服务，同时修改绑定状态
        if (!isBindService) {
            isBindService = true;
            getContext().getApplicationContext().bindService(mServiceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void stopService() {
        //释放音乐
        release();
        //解绑服务
        unbindService();
        //停止服务
        if (mServiceIntent != null) {
            getContext().getApplicationContext().stopService(mServiceIntent);
            mServiceIntent = null;
        }
    }

    public void unbindService() {
        if (isBindService) {
            isBindService = false;
            try {
                getContext().getApplicationContext().unbindService(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicBinder = (MusicService.MusicBind) service;
            mMusicBinder.playMusic(MyMusicPlayerView.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void mOnCompletion() {
        super.mOnCompletion();
        switch (mCurrentPlayMode) {
            case BasePlayerView.PLAY_MODE_LIST_LOOP:
                playNext(true);
                break;
            case BasePlayerView.PLAY_MODE_LOOP:
                restart();
                break;
            case BasePlayerView.PLAY_MODE_RANDOM:
                int position = (int) (Math.random() * (mMusicModel.getMusicList().size() - 1));
                playNext(position);
                break;
        }
    }

    public void playNext(boolean isNext) {
        if (isNext) {
            if (mMusicModel.getPosition() < mMusicModel.getMusicList().size() - 1) {
                playNext(mMusicModel.getPosition() + 1);
            } else {
                playNext(0);
            }
        } else {
            if (mMusicModel.getPosition() > 0) {
                playNext(mMusicModel.getPosition() - 1);
            } else {
                playNext(mMusicModel.getMusicList().size() - 1);
            }
        }
    }

    public void playNext(int position) {
        newMusicModel = new MusicModel();
        //音乐列表
        newMusicModel.setPosition(position);
        newMusicModel.setMusicList(mMusicModel.getMusicList());
        MusicBean musicBean = mMusicModel.getMusicList().get(position);

        //专辑图片、名称
        newMusicModel.setImgUrl(musicBean.getAlbumImg());
        newMusicModel.setAlbumname(musicBean.getAlbumname());

        //歌曲名称、mid
        newMusicModel.setSongname(musicBean.getSongname());
        newMusicModel.setSongmid(musicBean.getSongmid());

        //歌手名字
        String singerName = musicBean.getSinger();
        newMusicModel.setSinger(singerName);

        //播放链接
        String url = Constant.getQQmusicKey(musicBean.getSongmid());
        OkHttpUtil.getInstance().getNetManager().get(url, new INetCallBack() {
            @Override
            public void success(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject req_0 = jsonObject.getJSONObject("req_0");
                    JSONObject data = req_0.getJSONObject("data");
                    JSONArray midurlinfo = data.getJSONArray("midurlinfo");
                    JSONObject info = midurlinfo.getJSONObject(0);
                    String purl = info.optString("purl", "");
                    newMusicModel.setUrl(Constant.qqmusicUrl + purl);

                    isUrlFinish = true;
                    checkAllFinish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        //歌词
        String lyricUrl = Constant.qqmusicLyric + musicBean.getSongmid();
        OkHttpUtil.getInstance().getNetManager().get(lyricUrl, Constant.qqmusicHeadName, Constant.qqmusicHeadValue, new INetCallBack() {
            @Override
            public void success(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String lyric = jsonObject.optString("lyric", "");
                    newMusicModel.setLyric(lyric);

                    isLyricFinish = true;
                    checkAllFinish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        if (onMusicChangedListener != null) {
            onMusicChangedListener.setSongmid(musicBean.getSongmid());
        }
    }

    private void checkAllFinish() {
        if (isUrlFinish && isLyricFinish) {
            isUrlFinish = false;
            isLyricFinish = false;

            if (onMusicPlayerListener != null) {
                onMusicPlayerListener.setMusicModel(newMusicModel);
            }
        }
    }
}
