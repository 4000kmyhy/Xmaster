package com.xu.xmaster.fragments;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.maning.imagebrowserlibrary.MNImageBrowser;
import com.maning.imagebrowserlibrary.listeners.OnClickListener;
import com.maning.imagebrowserlibrary.listeners.OnLongClickListener;
import com.maning.imagebrowserlibrary.listeners.OnPageChangeListener;
import com.maning.imagebrowserlibrary.model.ImageBrowserConfig;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUICenterGravityRefreshOffsetCalculator;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.xu.xmaster.R;
import com.xu.xmaster.activities.FileActivity;
import com.xu.xmaster.adapters.FileAdapter;
import com.xu.xmaster.utils.FileManager;
import com.xu.xmaster.utils.GlideImageEngine;
import com.xu.xmaster.views.MyPullRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class PhotoFragment extends Fragment {

    private static final String TAG = "PhotoFragment";

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

        mList = FileManager.loadPhoto(getContext());
        mAdapter = new FileAdapter(getContext(), mList);
        rv_file.setAdapter(mAdapter);

        initContentObserver();
        initEvent();
    }

    private void initContentObserver() {
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
                MNImageBrowser.with(getContext())
                        //页面切换效果
                        .setTransformType(ImageBrowserConfig.TransformType.Transform_Default)
                        //指示器效果
                        .setIndicatorType(ImageBrowserConfig.IndicatorType.Indicator_Number)
                        //设置隐藏指示器
                        .setIndicatorHide(false)
                        //设置自定义遮盖层，定制自己想要的效果，当设置遮盖层后，原本的指示器会被隐藏
                        .setCustomShadeView(null)
                        //自定义ProgressView，不设置默认默认没有
                        .setCustomProgressViewLayoutID(R.layout.layout_custom_progress_view)
                        //当前位置
                        .setCurrentPosition(position)
                        //图片引擎
                        .setImageEngine(new GlideImageEngine())
                        //图片集合
                        .setImageList((ArrayList<String>) mList)
                        //方向设置
                        .setScreenOrientationType(ImageBrowserConfig.ScreenOrientationType.Screenorientation_Default)
                        //点击监听
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(FragmentActivity activity, ImageView view, int position, String url) {

                            }
                        })
                        //长按监听
                        .setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public void onLongClick(final FragmentActivity activity, final ImageView imageView, int position, String url) {
                            }
                        })
                        //页面切换监听
                        .setOnPageChangeListener(new OnPageChangeListener() {
                            @Override
                            public void onPageSelected(int position) {
                                Log.d(TAG, "onPageSelected:" + position);
                            }
                        })
                        //全屏模式
                        .setFullScreenMode(true)
                        //打开动画
                        .setActivityOpenAnime(R.anim.mn_browser_enter_anim)
                        //关闭动画
                        .setActivityExitAnime(R.anim.mn_browser_exit_anim)
                        //手势下拉缩小效果
                        .setOpenPullDownGestureEffect(true)
                        //显示：传入当前View
                        .show(((FileAdapter.MyViewHolder) viewHolder).item_img);
            }
        });
    }

    public void refreshList() {
        mList.clear();
        mList.addAll(FileManager.loadPhoto(getContext()));
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
