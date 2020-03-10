package com.xu.xmaster.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.MusicListAdapter;
import com.xu.xmaster.beans.MusicBean;
import com.xu.xmaster.beans.MusicListBean;
import com.xu.xmaster.database.FavMusicDBHelper;
import com.xu.xmaster.utils.net.INetCallBack;
import com.xu.xmaster.utils.net.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MusicListFragment extends Fragment {

    private static final String TAG = "MusicFragment";

    private SmartRefreshLayout refreshLayout;
    private RecyclerView rv_songlist;

    private int[] topid = {};
    private MusicListAdapter mAdapter;
    private List<MusicListBean> songList;
    private boolean isHomePage = false;
    private FavMusicDBHelper dbHelper;

    public interface OnSongListItemClickListener {
        void setSongList(MusicListBean songList);
    }

    private OnSongListItemClickListener onSongListItemClickListener;

    public void setOnSongListItemClickListener(OnSongListItemClickListener listener) {
        onSongListItemClickListener = listener;
    }

    public static MusicListFragment newInstance(int[] topid, boolean isHomePage) {
        MusicListFragment fragment = new MusicListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("topid", topid);
        bundle.putBoolean("isHomePage", isHomePage);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            topid = (int[]) bundle.getSerializable("topid");
            isHomePage = bundle.getBoolean("isHomePage", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musiclist, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        rv_songlist = view.findViewById(R.id.rv_songlist);

        songList = new ArrayList<>();
        mAdapter = new MusicListAdapter(getContext(), songList);
        rv_songlist.setAdapter(mAdapter);
        dbHelper = new FavMusicDBHelper(getContext());

        initData();
        initEvent();
    }

    private void initData() {
        songList.clear();
        if (isHomePage) {
            List<MusicBean> musicList = dbHelper.queryData();
            String picUrl = "";
            if (musicList.size() > 0) {
                picUrl = musicList.get(0).getAlbumImg();
            }
            songList.add(new MusicListBean("我喜欢", picUrl, musicList));
            mAdapter.notifyDataSetChanged();
        }
        for (int i = 0; i < topid.length; i++) {
            String url = Constant.qqmusicAPI + topid[i];
            OkHttpUtil.getInstance().getNetManager().get(url, new INetCallBack() {
                @Override
                public void success(String response) {
                    parseJson(response);
                    refreshLayout.finishRefresh();
                }

                @Override
                public void failed(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            int code = jsonObject.optInt("code", -1);
            if (code != 0) {
                return;
            }
            JSONArray result = jsonObject.optJSONArray("songlist");
            List<MusicBean> musicList = new ArrayList<>();
            for (int i = 0; i < result.length(); i++) {
                JSONObject temp = result.optJSONObject(i);
                JSONObject data = temp.optJSONObject("data");

                String albummid = data.optString("albummid", "");
                String albumImg = Constant.getQQmusicAlbumImg(albummid);
                String albumname = data.optString("albumname", "");
                JSONArray singer = data.optJSONArray("singer");
                String singerName = "";
                for (int j = 0; j < singer.length(); j++) {
                    JSONObject tempSinger = singer.optJSONObject(j);
                    if (j != 0) singerName += "/";
                    singerName += tempSinger.optString("name", "");
                }
                String songmid = data.optString("songmid", "");
                String songname = data.optString("songname", "");
                JSONObject pay = data.optJSONObject("pay");
                int payplay = pay.optInt("payplay", 0);

                musicList.add(new MusicBean(albumImg, albumname, singerName, songmid, songname, payplay));
            }
            JSONObject topinfo = jsonObject.optJSONObject("topinfo");
            String ListName = topinfo.optString("ListName", "");
            String MacDetailPicUrl = topinfo.optString("MacDetailPicUrl", "");

            String imgUrl;
            if (musicList.size() > 0) {
                imgUrl = musicList.get(0).getAlbumImg();
            } else {
                imgUrl = MacDetailPicUrl;
            }
            songList.add(new MusicListBean(ListName, imgUrl, musicList));
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initEvent() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
            }
        });

        mAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                MusicListBean songListBean = mAdapter.getItemObject(position);
                if (onSongListItemClickListener != null) {
                    onSongListItemClickListener.setSongList(songListBean);
                }
            }
        });
    }
}
