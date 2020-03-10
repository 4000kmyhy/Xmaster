package com.xu.xmaster.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.HistoryAdapter;
import com.xu.xmaster.adapters.MusicAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.beans.MusicBean;
import com.xu.xmaster.beans.MusicModel;
import com.xu.xmaster.database.HistoryDBHelper;
import com.xu.xmaster.utils.net.INetCallBack;
import com.xu.xmaster.utils.net.OkHttpUtil;
import com.xu.xmaster.views.MyMusicPlayerView;
import com.xu.xmaster.views.SearchToolbar;
import com.xu.xxplayer.utils.XXPlayerManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.swipebackfragment.SwipeBackFragment;

public class MusicSearchFragment extends SwipeBackFragment {

    private static final String TAG = "MusicSearchFragment";

    private SearchToolbar toolbar;
    private FrameLayout layout_history;
    private TextView tv_clear;
    private RecyclerView rv_history, rv_music;

    private HistoryAdapter historyAdapter;
    private MusicAdapter musicAdapter;
    private List<MusicBean> musicList;
    private HistoryDBHelper dbHelper;

    private String searchName;
    private int mPosition = -1;
    private MusicModel musicModel;
    private boolean isUrlFinish, isLyricFinish;

    public interface OnMusicSearchFragmentListener {
        void setMusicModel(MusicModel musicModel);
    }

    private OnMusicSearchFragmentListener onMusicSearchFragmentListener;

    public void setOnMusicSearchFragmentListener(OnMusicSearchFragmentListener listener) {
        onMusicSearchFragmentListener = listener;
    }

    public static MusicSearchFragment newInstance() {
        MusicSearchFragment fragment = new MusicSearchFragment();
        return fragment;
    }

    public static MusicSearchFragment newInstance(String name) {
        MusicSearchFragment fragment = new MusicSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            searchName = bundle.getString("name");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_search, container, false);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        layout_history = view.findViewById(R.id.layout_history);
        tv_clear = view.findViewById(R.id.tv_clear);

        rv_history = view.findViewById(R.id.rv_history);
        historyAdapter = new HistoryAdapter(getContext(), true);
        rv_history.setAdapter(historyAdapter);
        dbHelper = new HistoryDBHelper(getContext(), "music_history.db");
        historyAdapter.setList(dbHelper.queryData());

        rv_music = view.findViewById(R.id.rv_music);
        musicList = new ArrayList<>();
        musicAdapter = new MusicAdapter(getContext(), musicList);
        rv_music.setAdapter(musicAdapter);

        initToolbar();
        initEvent();
        initPlayer();

        if (!TextUtils.isEmpty(searchName)) {
            toolbar.setText(searchName);
            initData(searchName);
            dbHelper.insertData(searchName);
            historyAdapter.setList(dbHelper.queryData());
            layout_history.setVisibility(View.GONE);
            rv_music.setVisibility(View.VISIBLE);
        }
    }

    private void initToolbar() {
        toolbar.setPaddingTop();

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        toolbar.setOnSearchListener(new SearchToolbar.OnSearchListener() {
            @Override
            public void afterTextChanged(String s) {
                if (TextUtils.isEmpty(s)) {
                    layout_history.setVisibility(View.VISIBLE);
                    rv_music.setVisibility(View.GONE);
                }
            }
        });

        toolbar.setRightBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(toolbar.getText())) {
                    ((BaseActivity) getContext()).showToast("请输入音乐、歌手、专辑");
                } else {
                    initData(toolbar.getText());
                    dbHelper.insertData(toolbar.getText());
                    historyAdapter.setList(dbHelper.queryData());
                    layout_history.setVisibility(View.GONE);
                    rv_music.setVisibility(View.VISIBLE);
                }
            }
        });

        toolbar.setEditorSearchListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(toolbar.getText())) {
                        ((BaseActivity) getContext()).showToast("请输入音乐、歌手、专辑");
                    } else {
                        initData(toolbar.getText());
                        dbHelper.insertData(toolbar.getText());
                        historyAdapter.setList(dbHelper.queryData());
                        layout_history.setVisibility(View.GONE);
                        rv_music.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });
    }

    private void initEvent() {
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.clearData();
                historyAdapter.setList(dbHelper.queryData());
            }
        });

        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                toolbar.setText(historyAdapter.getItem(position));
                initData(historyAdapter.getItem(position));
                dbHelper.insertData(historyAdapter.getItem(position));
                historyAdapter.setList(dbHelper.queryData());
                layout_history.setVisibility(View.GONE);
                rv_music.setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemDel(int position) {
                dbHelper.deleteData(historyAdapter.getItem(position));
                historyAdapter.setList(dbHelper.queryData());
            }
        });

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
                    ((BaseActivity) getContext()).showToast("没有VIP。");
                    return;
                }
                mPosition = position;
                musicAdapter.setSelect(position);
                getMusicInfo(musicBean);
            }
        });
    }

    private void getMusicInfo(MusicBean musicBean) {
        musicModel = new MusicModel();
        //音乐列表
        musicModel.setPosition(mPosition);
        musicModel.setMusicList(musicList);

        //专辑图片、名称
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

            if (onMusicSearchFragmentListener != null) {
                onMusicSearchFragmentListener.setMusicModel(musicModel);
            }
        }
    }

    private void initData(String s) {
        musicList.clear();
        musicAdapter.notifyDataSetChanged();

        String url = Constant.qqmusicSearchSong + s;
        Log.d(TAG, "initData: " + url);
        OkHttpUtil.getInstance().getNetManager().get(url, new INetCallBack() {
            @Override
            public void success(String response) {
                parseJson(response);
            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            int code = jsonObject.optInt("code", -1);
            if (code != 0) {
                return;
            }
            JSONObject data = jsonObject.optJSONObject("data");
            JSONObject song = data.optJSONObject("song");
            JSONArray list = song.optJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject temp = list.optJSONObject(i);
                String albummid = Constant.getQQmusicAlbumImg(temp.optString("albummid", ""));
                String albumname = temp.optString("albumname", "");
                JSONArray singer = temp.optJSONArray("singer");
                String singerName = "";
                for (int j = 0; j < singer.length(); j++) {
                    JSONObject tempSinger = singer.optJSONObject(j);
                    if (j != 0) singerName += "/";
                    singerName += tempSinger.optString("name", "");
                }
                String songmid = temp.optString("songmid", "");
                String songname = temp.optString("songname", "");
                JSONObject pay = temp.optJSONObject("pay");
                int payplay = pay.optInt("payplay", 0);

                musicList.add(new MusicBean(albummid, albumname, singerName, songmid, songname, payplay));
            }
            musicAdapter.notifyDataSetChanged();

            if (XXPlayerManager.instance().getCurrentPlayer() != null &&
                    XXPlayerManager.instance().getCurrentPlayer() instanceof MyMusicPlayerView &&
                    ((MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer()).getMusicModel() != null) {
                setPosition(((MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer()).getMusicModel().getSongmid());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initPlayer() {
        if (XXPlayerManager.instance().getCurrentPlayer() != null &&
                XXPlayerManager.instance().getCurrentPlayer() instanceof MyMusicPlayerView) {
            ((MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer()).setOnMusicChangedListener(new MyMusicPlayerView.OnMusicChangedListener() {
                @Override
                public void setSongmid(String songmid) {
                    setPosition(songmid);
                }
            });
        }
    }

    private void setPosition(String songmid) {
        mPosition = musicAdapter.getPositionBySongmid(songmid);
        musicAdapter.setSelect(mPosition);
//        rv_music.scrollToPosition(mPosition);
    }
}
