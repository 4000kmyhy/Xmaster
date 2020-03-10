package com.xu.xmaster.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.xu.xmaster.R;
import com.xu.xmaster.beans.ForecastBean;
import com.xu.xmaster.views.SimpleToolbar;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.yokeyword.swipebackfragment.SwipeBackFragment;

public class ForecastFragment extends SwipeBackFragment {

    private static final String TAG = "ForecastFragment";

    private SimpleToolbar toolbar;
    private ViewPager viewPager;
    private LinearLayout indicator;

    private List<ForecastBean> forecastList;
    private int mPosition;

    public static ForecastFragment newInstance(List<ForecastBean> forecastList, int position) {
        ForecastFragment fragment = new ForecastFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", (Serializable) forecastList);
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            forecastList = (List<ForecastBean>) bundle.getSerializable("list");
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

        String time = forecastList.get(mPosition).getDate();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(time);
            if (mPosition == 0) {
                dateFormat.applyPattern("MM月dd日 今天");
            } else {
                dateFormat.applyPattern("MM月dd日 EEE");
            }
            time = dateFormat.format(date);
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
        for (int i = 0; i < forecastList.size(); i++) {
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
                return DayFragment.newInstance(forecastList.get(i));
            }

            @Override
            public int getCount() {
                return forecastList.size();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int index, float v, int i1) {

            }

            @Override
            public void onPageSelected(int index) {
                for (int i = 0; i < forecastList.size(); i++) {
                    indicator.getChildAt(i).setBackgroundResource(index == i ? R.drawable.dot_focus : R.drawable.dot_normal);
                }
                String time = forecastList.get(index).getDate();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(time);
                    if (index == 0) {
                        dateFormat.applyPattern("MM月dd日 今天");
                    } else {
                        dateFormat.applyPattern("MM月dd日 EEE");
                    }
                    time = dateFormat.format(date);
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
