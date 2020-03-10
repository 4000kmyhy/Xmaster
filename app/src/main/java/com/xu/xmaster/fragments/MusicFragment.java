package com.xu.xmaster.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.MusicAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.beans.MusicBean;
import com.xu.xmaster.beans.MusicModel;
import com.xu.xmaster.beans.MusicListBean;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.net.INetCallBack;
import com.xu.xmaster.utils.net.OkHttpUtil;
import com.xu.xmaster.views.MyMusicPlayerView;
import com.xu.xmaster.views.SimpleToolbar;
import com.xu.xxplayer.utils.XXPlayerManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.yokeyword.swipebackfragment.SwipeBackFragment;

public class MusicFragment extends SwipeBackFragment {

    private static final String TAG = "MusicFragment";

    private ImageView iv_pic;
    private AppBarLayout app_bar;
    private Toolbar toolbar;
    private SimpleToolbar simpleToolbar;
    private RecyclerView rv_music;
    private TextView tv_nomusic;

    private MusicAdapter musicAdapter;
    private List<MusicBean> musicList = new ArrayList<>();
    private String listName;
    private String picUrl;

    private int mPosition = -1;
    private MusicModel musicModel;
    private boolean isUrlFinish, isLyricFinish;

    public interface OnMusicFragmentListener {
        void setMusicModel(MusicModel musicModel);
    }

    private OnMusicFragmentListener onMusicFragmentListener;

    public void setOnMusicFragmentListener(OnMusicFragmentListener listener) {
        onMusicFragmentListener = listener;
    }

    public static MusicFragment newInstance(MusicListBean songListBean) {
        MusicFragment fragment = new MusicFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", (Serializable) songListBean.getMusicList());
        bundle.putString("list_name", songListBean.getListName());
        bundle.putString("pic_url", songListBean.getPicUrl());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            musicList = (List<MusicBean>) bundle.getSerializable("list");
            listName = bundle.getString("list_name", "");
            picUrl = bundle.getString("pic_url", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv_pic = view.findViewById(R.id.iv_pic);
        app_bar = view.findViewById(R.id.app_bar);
        toolbar = view.findViewById(R.id.toolbar);
        simpleToolbar = view.findViewById(R.id.simpleToolbar);
        rv_music = view.findViewById(R.id.rv_music);
        tv_nomusic = view.findViewById(R.id.tv_nomusic);

        musicAdapter = new MusicAdapter(getContext(), musicList);
        rv_music.setAdapter(musicAdapter);

        if (musicList == null || musicList.size() == 0) {
            tv_nomusic.setVisibility(View.VISIBLE);
        }

        initToolbar();
        initEvent();
        initPlayer();
    }

    private void initToolbar() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        params.topMargin = QMUIStatusBarHelper.getStatusbarHeight(getContext());
        toolbar.setLayoutParams(params);

        GlideUtils.loadImage(getContext(), picUrl, R.drawable.pic_cat, iv_pic);
        simpleToolbar.setTitle(listName);
        simpleToolbar.getTitleView().setAlpha(0);
        simpleToolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        app_bar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                int height = app_bar.getHeight() -
                        toolbar.getHeight() -
                        QMUIStatusBarHelper.getStatusbarHeight(getContext());
                float alpha = (float) (1 - 1.0 * (height + i) / height);
                simpleToolbar.getTitleView().setAlpha(alpha);
            }
        });
    }

    private void initEvent() {
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                MusicBean musicBean = (MusicBean) musicAdapter.getItemObject(position);
                if (mPosition == position) {//如果当前当前音乐正在播放
                    if (XXPlayerManager.instance().getCurrentPlayer() != null &&
                            XXPlayerManager.instance().getCurrentPlayer() instanceof MyMusicPlayerView) {
                        if (XXPlayerManager.instance().getCurrentPlayer().isIdle()) {
                            XXPlayerManager.instance().getCurrentPlayer().start();
                        } else {
                            XXPlayerManager.instance().getCurrentPlayer().restart();
                        }
                    }
                    return;
                }
                if (musicBean.getPayplay() == 1) {//vip音乐
                    ((BaseActivity) getContext()).showToast("没钱。");
                    return;
                }
                mPosition = position;
                musicAdapter.setSelect(position);
                getMusicInfo(musicBean);
            }
        });
    }

    private void initPlayer() {
        if (XXPlayerManager.instance().getCurrentPlayer() != null &&
                XXPlayerManager.instance().getCurrentPlayer() instanceof MyMusicPlayerView) {
            if (((MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer()).getMusicModel() != null) {
                setPosition(((MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer()).getMusicModel().getSongmid());
            }
            ((MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer()).setOnMusicChangedListener(new MyMusicPlayerView.OnMusicChangedListener() {
                @Override
                public void setSongmid(String songmid) {
                    setPosition(songmid);
                }
            });
        }
    }

    private void getMusicInfo(MusicBean musicBean) {
        musicModel = new MusicModel();
        //音乐列表
        musicModel.setPosition(mPosition);
        musicModel.setMusicList(musicList);

        //专辑图片
        musicModel.setImgUrl(musicBean.getAlbumImg());
        musicModel.setAlbumname(musicBean.getAlbumname());

        //歌曲名称、mid
        musicModel.setSongname(musicBean.getSongname());
        musicModel.setSongmid(musicBean.getSongmid());

        //歌手名字
        String singerName = musicBean.getSinger();
        musicModel.setSinger(singerName);

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
                    musicModel.setUrl(Constant.qqmusicUrl + purl);

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
                    musicModel.setLyric(lyric);

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
    }

    private void checkAllFinish() {
        if (isUrlFinish && isLyricFinish) {
            isUrlFinish = false;
            isLyricFinish = false;

            if (onMusicFragmentListener != null) {
                onMusicFragmentListener.setMusicModel(musicModel);
            }
        }
    }

    public void setPosition(String songmid) {
        mPosition = musicAdapter.getPositionBySongmid(songmid);
        musicAdapter.setSelect(mPosition);
//        rv_music.scrollToPosition(mPosition);
    }
}
