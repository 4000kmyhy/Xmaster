package com.xu.xmaster.fragments;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUICenterGravityRefreshOffsetCalculator;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.xu.xmaster.R;
import com.xu.xmaster.activities.VideoActivity;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.activities.FileActivity;
import com.xu.xmaster.activities.VideoPlayerActivity;
import com.xu.xmaster.adapters.FileAdapter;
import com.xu.xmaster.utils.FileManager;
import com.xu.xmaster.views.MyPullRefreshLayout;

import java.util.List;

public class VideoFragment extends Fragment {

    private static final String TAG = "VideoFragment";

    private MyPullRefreshLayout mPullRefreshLayout;
    private RecyclerView rv_file;

    private GridLayoutManager manager;
    private FileAdapter mAdapter;
    private List<String> mList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_file, null);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPullRefreshLayout = view.findViewById(R.id.pull_to_refresh);
        mPullRefreshLayout.setRefreshOffsetCalculator(new QMUICenterGravityRefreshOffsetCalculator());

        rv_file = view.findViewById(R.id.rv_file);

        manager = new GridLayoutManager(getContext(), 4);
        rv_file.setLayoutManager(manager);

        mList = FileManager.loadVideo(getContext());
        mAdapter = new FileAdapter(getContext(), mList);
        rv_file.setAdapter(mAdapter);

        initContentObserver();
        initEvent();
    }

    private void initContentObserver() {
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        Log.d(TAG, "onChange: " + selfChange + "," + uri);
                        refreshList();
                    }
                });
    }

    private void initEvent() {
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {

            }

            @Override
            public void onMoveRefreshView(int offset) {

            }

            @Override
            public void onRefresh() {
                mPullRefreshLayout.finishRefresh();
                ((FileActivity) getContext()).openCamera();
            }
        });

        mAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                Intent intent = new Intent(getContext(), VideoActivity.class);
                intent.putExtra("path", mAdapter.getItemObject(position));
                ((BaseActivity) getContext()).startAc(intent);
            }
        });
    }

    public void refreshList() {
        mList.clear();
        mList.addAll(FileManager.loadVideo(getContext()));
        mAdapter.notifyDataSetChanged();
    }

    public void changeSpanCount(int count) {
        manager.setSpanCount(count);
    }

    public void goToTop() {
        if (manager.findFirstCompletelyVisibleItemPosition() == -1 ||
                manager.findFirstCompletelyVisibleItemPosition() > 4 * manager.getSpanCount() * manager.getSpanCount()) {
            rv_file.scrollToPosition(4 * manager.getSpanCount() * manager.getSpanCount());
        }
        rv_file.smoothScrollToPosition(0);
    }
}
