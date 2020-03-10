package com.xu.xmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.activities.MainActivity;
import com.xu.xmaster.activities.SplashActivity;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.utils.ShareUtils;

public class SplashFragment extends Fragment {

    private static final String TAG = "SplashFragment";
    private static final int[] colors = {
            R.color.colorBaidu,
            R.color.colorNews,
            R.color.colorWeather,
            R.color.colorMusic,
            R.color.colorBaidu,
            R.color.colorNews,};
    private static final String[] titles = {
            "Translate",
            "News",
            "Weather",
            "Music",
            "Translate",
            "News",};

    private FrameLayout layout_bg;
    private TextView tv_title;
    private Button btn_goto;
    private int mPosition = 0;

    public static SplashFragment newInstance(int position) {
        SplashFragment fragment = new SplashFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt("position", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout_bg = view.findViewById(R.id.layout_bg);
        tv_title = view.findViewById(R.id.tv_title);
        btn_goto = view.findViewById(R.id.btn_goto);

        layout_bg.setBackgroundColor(getResources().getColor(colors[mPosition]));
        tv_title.setText(titles[mPosition]);

        if (mPosition == SplashActivity.PAGE_COUNT - 2 || mPosition == 0) {
            btn_goto.setVisibility(View.VISIBLE);
        } else {
            btn_goto.setVisibility(View.GONE);
        }
        initEvent();
    }

    private void initEvent() {
        btn_goto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MainActivity.class));
                ((BaseActivity) getContext()).finish();
                ShareUtils.setFirstLoading(getContext(), false);
            }
        });
    }
}
