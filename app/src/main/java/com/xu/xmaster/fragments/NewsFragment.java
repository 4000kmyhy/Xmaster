package com.xu.xmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.activities.WebActivity;
import com.xu.xmaster.adapters.NewsAdapter;
import com.xu.xmaster.beans.NewsBean;
import com.xu.xmaster.utils.net.INetCallBack;
import com.xu.xmaster.utils.net.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";
    private static final String BUNDLE_KEY = "key";

    private SmartRefreshLayout refreshLayout;
    private RecyclerView rv_news;
    private LinearLayout layout_float;
    private ImageView iv_refresh, iv_totop;

    private LinearLayoutManager layoutManager;
    private List<NewsBean> newsList;
    private NewsAdapter mAdapter;

    private String mKey;
    private int mIndex = 0;
    private int mCount = 10;
    private boolean isRefresh = false;
    private boolean isLoadMore = false;

    public static NewsFragment newInstance(String key) {
        NewsFragment fragment = new NewsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mKey = bundle.getString(BUNDLE_KEY, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //refreshlayout
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
//        refreshLayout.autoRefresh();
        refreshLayout.setEnableAutoLoadMore(false);//不启用下拉到底部自动加载更多

        //recyclerview
        rv_news = view.findViewById(R.id.rv_news);
        layoutManager = new LinearLayoutManager(getContext());
        rv_news.setLayoutManager(layoutManager);
        rv_news.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        newsList = new ArrayList<>();
        mAdapter = new NewsAdapter(getContext(), newsList);
        rv_news.setAdapter(mAdapter);

        //floatlayout
        layout_float = view.findViewById(R.id.layout_float);
        iv_refresh = view.findViewById(R.id.iv_refresh);
        iv_totop = view.findViewById(R.id.iv_totop);

        mIndex = 0;
        isRefresh = true;
        initData(mIndex, mCount);
        initEvent();
    }

    private void initData(int index, int count) {
        String url = Constant.neteaseAPI + mKey + "/" + index + "-" + count + ".html";
        Log.d(TAG, "initData:url " + url);
        OkHttpUtil.getInstance().getNetManager().get(url, new INetCallBack() {
            @Override
            public void success(String response) {
                String str1 = "artiList(";
                String str2 = ")";
                if (response.startsWith(str1) && response.endsWith(str2)) {
                    response = response.replace(str1,"").replace(str2,"");
                    Log.d(TAG, "success: " + response);

                    if (isRefresh) {
                        refreshLayout.finishRefresh();
                        isRefresh = false;
                        newsList.clear();
                        parseJson(response);
                    } else if (isLoadMore) {
                        refreshLayout.finishLoadMore();
                        isLoadMore = false;
                        parseJson(response);
                    }
                }
            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
                ((BaseActivity) getContext()).showToast("数据获取错误，请检查网络");

                if (isRefresh) {
                    refreshLayout.finishRefresh(false);
                    isRefresh = false;
                } else if (isLoadMore) {
                    refreshLayout.finishLoadMore(false);
                    isLoadMore = false;
                }
            }
        });
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(mKey);
            for (int i = 0; i < result.length(); i++) {
                JSONObject temp = result.getJSONObject(i);
                String title = temp.optString("title", "");
                String url = temp.optString("url", "");
                String skipURL = temp.optString("skipURL", "");
                String imgsrc = temp.optString("imgsrc", "");
                String source = temp.optString("source", "");
                String ptime = temp.optString("ptime", "");

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    NewsBean newsBean = new NewsBean(title, url, imgsrc, source, ptime);
                    newsList.add(newsBean);
                    Log.d(TAG, "parseJson: " + newsBean.toString());
                } else if (skipURL.startsWith("http://") || skipURL.startsWith("https://")) {
                    NewsBean newsBean = new NewsBean(title, skipURL, imgsrc, source, ptime);
                    newsList.add(newsBean);
                    Log.d(TAG, "parseJson: " + newsBean.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();

        //新闻数小于10，loadmore
        if (newsList.size() < 10) {
            mIndex += 10;
            isLoadMore = true;
            initData(mIndex, mCount);
        }
    }

    private void initEvent() {
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mIndex += 10;
                isLoadMore = true;
                initData(mIndex, mCount);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mIndex = 0;
                isRefresh = true;
                initData(mIndex, mCount);
            }
        });

        rv_news.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItem = layoutManager.findFirstVisibleItemPosition();
                //向上滑动，且第一个显示的item大于10时显示悬浮按钮
                if (dy < 0 && firstItem > 9) {
                    layout_float.setVisibility(View.VISIBLE);
                } else {
                    layout_float.setVisibility(View.GONE);
                }
            }
        });

        iv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout.autoRefresh();
            }
        });

        iv_totop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_news.scrollToPosition(0);
            }
        });

        mAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                String url = mAdapter.getItemUrl(position);
                String title = mAdapter.getItemTitle(position);
                Intent intent = new Intent(getContext(), WebActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("title", title);
                ((BaseActivity) getContext()).startAc(intent);
            }
        });
    }
}
