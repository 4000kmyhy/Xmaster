package com.xu.xmaster.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.SettingsAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.utils.ShareUtils;
import com.xu.xmaster.views.SimpleToolbar;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private SimpleToolbar toolbar;
    private RecyclerView rv_settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_settings);

        initView();
        initData();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        rv_settings = findViewById(R.id.rv_settings);

        initToolbar();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        List<String> list = new ArrayList<>();
        list.add("应用名称=" + getResources().getString(R.string.app_name));
        list.add("版本号=" + ShareUtils.getVersionName(getContext()));
        list.add("语言=中文");
        SettingsAdapter settingsAdapter = new SettingsAdapter(getContext(), list);
        rv_settings.setAdapter(settingsAdapter);
    }
}
