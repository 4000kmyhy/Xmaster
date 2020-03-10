package com.xu.xmaster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.fragments.SplashFragment;
import com.xu.xmaster.utils.ScaleTransformer;
import com.xu.xmaster.utils.ShareUtils;

public class SplashActivity extends BaseActivity {

    public static final int PAGE_COUNT = 6;
    private ViewPager viewPager;
    private LinearLayout indicator;
    private ImageView iv_icon;
    private TextView tv_name, tv_version;

    private int currentIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setSwipeBackEnable(false);

        if (ShareUtils.isFirstLoading(getContext())) {
            initView();
        } else {
            startActivity(new Intent(getContext(), MainActivity.class));
            finish();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_splash);
        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.indicator);
        iv_icon = findViewById(R.id.iv_icon);
        tv_name = findViewById(R.id.tv_name);
        tv_version = findViewById(R.id.tv_version);

        initIndicator();
        initViewPager();
        initInfo();
    }

    private void initIndicator() {
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                10f, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
        lp.rightMargin = width / 2;
        lp.leftMargin = width / 2;
        for (int i = 0; i < PAGE_COUNT - 2; i++) {
            View view = new View(getContext());
            view.setId(i);
            view.setBackgroundResource(i == 0 ? R.drawable.dot_focus : R.drawable.dot_normal);
            view.setLayoutParams(lp);
            indicator.addView(view, i);
        }
    }

    private void initViewPager() {
        viewPager.setPageTransformer(true, new ScaleTransformer());

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return SplashFragment.newInstance(i);
            }

            @Override
            public int getCount() {
                return PAGE_COUNT;
            }
        });

        viewPager.setCurrentItem(currentIndex);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int index) {
                for (int i = 0; i < PAGE_COUNT - 2; i++) {
                    indicator.getChildAt(i).setBackgroundResource(index - 1 == i ? R.drawable.dot_focus : R.drawable.dot_normal);
                }
                if (index == 0) {
                    currentIndex = PAGE_COUNT - 2;
                    indicator.getChildAt(PAGE_COUNT - 3).setBackgroundResource(R.drawable.dot_focus);
                } else if (index == PAGE_COUNT - 1) {
                    currentIndex = 1;
                    indicator.getChildAt(0).setBackgroundResource(R.drawable.dot_focus);
                } else {
                    currentIndex = index;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_IDLE) {
                    viewPager.setCurrentItem(currentIndex, false);
                }
            }
        });
    }

    private void initInfo() {
        iv_icon.setImageResource(R.mipmap.ic_launcher);
        tv_name.setText(getResources().getString(R.string.app_name));
        tv_version.setText("version:" + ShareUtils.getVersionName(getContext()));
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
