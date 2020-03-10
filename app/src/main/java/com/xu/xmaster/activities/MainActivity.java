package com.xu.xmaster.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.fragments.NewsFragment;
import com.xu.xmaster.views.NavItemView;
import com.xu.xmaster.views.SimpleToolbar;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer_layout;
    private SimpleToolbar toolbar;
    private LinearLayout nav_view, nav_head_layout;
    private ImageView nav_head_icon;
    private NavItemView nav_photo, nav_video, nav_link, nav_music, nav_translate, nav_weather, nav_setting, nav_exit;
    private SlidingTabLayout mTabLayout;
    private ViewPager mViewPager;

    private long preDownTime = 0l;
    private boolean isPreDown = false;

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setSwipeBackEnable(false);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }

        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.showWea();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        drawer_layout = findViewById(R.id.drawer_layout);

        nav_view = findViewById(R.id.nav_view);
        nav_head_layout = findViewById(R.id.nav_head_layout);
        nav_head_icon = findViewById(R.id.nav_head_icon);
        nav_photo = findViewById(R.id.nav_photo);
        nav_video = findViewById(R.id.nav_video);
        nav_link = findViewById(R.id.nav_link);
        nav_music = findViewById(R.id.nav_music);
        nav_translate = findViewById(R.id.nav_translate);
        nav_weather = findViewById(R.id.nav_weather);
        nav_setting = findViewById(R.id.nav_setting);
        nav_exit = findViewById(R.id.nav_exit);

        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        initToolbar();
        initNavHead();
        initViewPager();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_layout.openDrawer(nav_view);
            }
        });

        toolbar.setWeaLayoutOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAc(new Intent(getContext(), WeatherActivity.class));
            }
        });
    }

    private void initNavHead() {
        nav_head_layout.setPadding(0, QMUIStatusBarHelper.getStatusbarHeight(getContext()), 0, 0);
        int headIcon = R.drawable.pic_cat;
        Glide.with(getContext())
                .load(headIcon)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 5)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        nav_head_layout.setBackground(resource);
                    }
                });

        Glide.with(getContext())
                .load(headIcon)
                .circleCrop()
                .into(nav_head_icon);
    }

    private void initViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            String[] neteaseTitle = getResources().getStringArray(R.array.netease_title);
            String[] neteaseKey = getResources().getStringArray(R.array.netease_key);

            @Override
            public Fragment getItem(int i) {
                return NewsFragment.newInstance(neteaseKey[i]);
            }

            @Override
            public int getCount() {
                return neteaseKey.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return neteaseTitle[position];
            }
        });

        //在viewpager setAdapter之后
        mTabLayout.setViewPager(mViewPager);
        mTabLayout.setSnapOnTabClick(true);//viewpager切换不滑动
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void initEvent() {
        nav_photo.setOnClickListener(this);
        nav_video.setOnClickListener(this);
        nav_link.setOnClickListener(this);
        nav_music.setOnClickListener(this);
        nav_translate.setOnClickListener(this);
        nav_weather.setOnClickListener(this);
        nav_setting.setOnClickListener(this);
        nav_exit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.nav_photo:
                intent.setClass(getContext(), FileActivity.class);
                intent.putExtra("position", 0);
                startAc(intent);
                break;
            case R.id.nav_video:
                intent.setClass(getContext(), FileActivity.class);
                intent.putExtra("position", 1);
                startAc(intent);
                break;
            case R.id.nav_link:
                intent.setClass(getContext(), LinkActivity.class);
                startAc(intent);
                break;
            case R.id.nav_music:
                intent.setClass(getContext(), MusicActivity.class);
                startAc(intent);
                break;
            case R.id.nav_translate:
                intent.setClass(getContext(), TranslateActivity.class);
                startAc(intent);
                break;
            case R.id.nav_weather:
                intent.setClass(getContext(), WeatherActivity.class);
                startAc(intent);
                break;
            case R.id.nav_setting:
                intent.setClass(getContext(), SettingsActivity.class);
                startAc(intent);
                break;
            case R.id.nav_exit:
                finish();
                break;
        }
        drawer_layout.closeDrawer(nav_view, false);
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(nav_view)) {
            drawer_layout.closeDrawer(nav_view);
        } else {
            long lastDownTime = System.currentTimeMillis();
            if (isPreDown) {
                if (lastDownTime - preDownTime < 2000) {
                    finish();
                } else {
                    preDownTime = lastDownTime;
                    showToast("再次返回退出应用");
                }
            } else {
                preDownTime = lastDownTime;
                isPreDown = true;
                showToast("再次返回退出应用");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.d(TAG, "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
                if (grantResults[i] == -1) {
                    finish();
                }
            }
        }
    }
}
