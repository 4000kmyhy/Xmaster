package com.xu.xmaster.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.fragments.PhotoFragment;
import com.xu.xmaster.fragments.VideoFragment;
import com.xu.xmaster.utils.PixelUtils;
import com.xu.xmaster.views.TabToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileActivity extends BaseActivity {

    private static final String TAG = "LocalActivity";

    private TabToolbar toolbar;
    private QMUITabSegment mTabSegment;
    private ViewPager mViewPager;

    private List<Fragment> fragments;
    private PhotoFragment photoFragment;
    private VideoFragment videoFragment;

    private int pagerIndex = 0;

    private File cameraSavePath;//拍照照片路径
    private Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_file);

        pagerIndex = getIntent().getIntExtra("position", 0);

        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        mTabSegment = toolbar.getTabSegment();
        mViewPager = findViewById(R.id.viewPager);

        initToolbar();
        initTabSegment();
        initViewPager();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();
        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setRightBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog(mViewPager.getCurrentItem());
            }
        });
    }

    private void initTabSegment() {
        QMUITabSegment.Tab tab1 = new QMUITabSegment.Tab("图片");
        tab1.setTextColor(Color.parseColor("#80ffffff"), Color.parseColor("#ffffff"));
        tab1.setTextSize(PixelUtils.sp2px(getContext(), 20));
        QMUITabSegment.Tab tab2 = new QMUITabSegment.Tab("视频");
        tab2.setTextColor(Color.parseColor("#80ffffff"), Color.parseColor("#ffffff"));
        tab2.setTextSize(PixelUtils.sp2px(getContext(), 20));
        mTabSegment.addTab(tab1);
        mTabSegment.addTab(tab2);

        mTabSegment.setupWithViewPager(mViewPager, false);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);

        mTabSegment.setHasIndicator(true);//指示线
        mTabSegment.setIndicatorPosition(false);//指示线在底部
        mTabSegment.setIndicatorWidthAdjustContent(true);//指示线长度跟随内容长度

        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final int index) {//当某个 Tab 被选中时会触发
                mTabSegment.hideSignCountView(index);
                mViewPager.setCurrentItem(index, false);
            }

            @Override
            public void onTabReselected(int index) {//当某个 Tab 处于被选中状态下再次被点击时会触发
                mTabSegment.hideSignCountView(index);
                if (index == 0) {
                    photoFragment.goToTop();
                } else if (index == 1) {
                    videoFragment.goToTop();
                }
            }

            @Override
            public void onTabUnselected(int index) {//当某个 Tab 被取消选中时会触发

            }

            @Override
            public void onDoubleTap(int index) {//当某个 Tab 被双击时会触发

            }
        });
    }

    private void initViewPager() {
        photoFragment = new PhotoFragment();
        videoFragment = new VideoFragment();

        fragments = new ArrayList<Fragment>();
        fragments.add(photoFragment);
        fragments.add(videoFragment);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //最左边的页面才可以侧滑关闭activity
//                if (i != 0) {
//                    setSwipeBackEnable(false);
//                } else {
//                    setSwipeBackEnable(true);
//                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mViewPager.setCurrentItem(pagerIndex);
    }

    /**
     * 打开相机
     */
    public void openCamera() {
        File file = new File(Constant.FileSavePath);
        if (!file.exists()) {
            file.mkdir();
        }

        Intent intent = null;
        int index = mViewPager.getCurrentItem();
        if (index == 0) {
            cameraSavePath = new File(Constant.FileSavePath, System.currentTimeMillis() + ".jpg");
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        } else if (index == 1) {
            cameraSavePath = new File(Constant.FileSavePath, System.currentTimeMillis() + ".mp4");
            intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getContext(), "com.xu.xmaster.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, index);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String photoPath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
            } else {
                photoPath = uri.getEncodedPath();
            }
            File mfile = new File(photoPath);
            if (mfile.exists()) {
                try {
                    //发送广播通知更新数据库
                    Uri mUri = Uri.fromFile(mfile);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, mUri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showBottomDialog(final int index) {
        new QMUIBottomSheet.BottomGridSheetBuilder(getContext())
                .addItem(R.drawable.column1, "一列", 1, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.column2, "二列", 2, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.column3, "三列", 3, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.column4, "四列", 4, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView) {
                        dialog.dismiss();
                        int tag = (int) itemView.getTag();
                        if (index == 0) {
                            photoFragment.changeSpanCount(tag);
                        } else if (index == 1) {
                            videoFragment.changeSpanCount(tag);
                        }
                    }
                })
                .build()
                .show();
    }
}
