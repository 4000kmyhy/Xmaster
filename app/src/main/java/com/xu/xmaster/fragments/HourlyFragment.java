package com.xu.xmaster.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xu.xmaster.R;
import com.xu.xmaster.beans.HourlyBean;
import com.xu.xmaster.views.SimpleToolbar;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.yokeyword.swipebackfragment.SwipeBackFragment;

public class HourlyFragment extends SwipeBackFragment {

    private static final String TAG = "HourlyFragment";

    private SimpleToolbar toolbar;
    private ViewPager viewPager;
    private LinearLayout indicator;

    private List<HourlyBean> hourlyList;
    private int mPosition;

    public static HourlyFragment newInstance(List<HourlyBean> hourlyList, int position) {
        HourlyFragment fragment = new HourlyFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", (Serializable) hourlyList);
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            hourlyList = (List<HourlyBean>) bundle.getSerializable("list");
            mPosition = bundle.getInt("position", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        viewPager = view.findViewById(R.id.viewPager);
        indicator = view.findViewById(R.id.indicator);

        initToolbar();
        initIndicator();
        initViewPager();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();

        String time = hourlyList.get(mPosition).getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date newsDate = dateFormat.parse(time);
            dateFormat.applyPattern("yyyyMMdd");
            String newsTime = dateFormat.format(newsDate);
            String nowTime = dateFormat.format(new Date());
            if (TextUtils.equals(newsTime, nowTime)) {
                dateFormat.applyPattern("今天 HH:mm");
                time = dateFormat.format(newsDate);
            } else {
                dateFormat.applyPattern("明天 HH:mm");
                time = dateFormat.format(newsDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        toolbar.setTitle(time);

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void initIndicator() {
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                10f, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
        lp.rightMargin = width / 2;
        lp.leftMargin = width / 2;
        for (int i = 0; i < hourlyList.size(); i++) {
            View view = new View(getContext());
            view.setId(i);
            view.setBackgroundResource(i == mPosition ? R.drawable.dot_focus : R.drawable.dot_normal);
            view.setLayoutParams(lp);
            indicator.addView(view, i);
        }
    }

    private void initViewPager() {
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return HourFragment.newInstance(hourlyList.get(i));
            }

            @Override
            public int getCount() {
                return hourlyList.size();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int index, float v, int i1) {

            }

            @Override
            public void onPageSelected(int index) {
                for (int i = 0; i < hourlyList.size(); i++) {
                    indicator.getChildAt(i).setBackgroundResource(index == i ? R.drawable.dot_focus : R.drawable.dot_normal);
                }
                String time = hourlyList.get(index).getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    Date newsDate = dateFormat.parse(time);
                    dateFormat.applyPattern("yyyyMMdd");
                    String newsTime = dateFormat.format(newsDate);
                    String nowTime = dateFormat.format(new Date());
                    if (TextUtils.equals(newsTime, nowTime)) {
                        dateFormat.applyPattern("今天 HH:mm");
                        time = dateFormat.format(newsDate);
                    } else {
                        dateFormat.applyPattern("明天 HH:mm");
                        time = dateFormat.format(newsDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                toolbar.setTitle(time);
            }

            @Override
            public void onPageScrollStateChanged(int index) {
            }
        });
        viewPager.setCurrentItem(mPosition);
    }
}