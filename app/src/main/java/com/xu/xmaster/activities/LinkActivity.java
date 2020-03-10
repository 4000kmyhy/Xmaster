package com.xu.xmaster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.HistoryAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.database.HistoryDBHelper;
import com.xu.xmaster.views.SearchToolbar;

public class LinkActivity extends BaseActivity {

    private static final String TAG = "LinkActivity";

    private SearchToolbar toolbar;
    private TextView tv_clear;
    private RecyclerView rv_history;

    private HistoryAdapter historyAdapter;
    private HistoryDBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_link);

        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        tv_clear = findViewById(R.id.tv_clear);
        rv_history = findViewById(R.id.rv_history);

        historyAdapter = new HistoryAdapter(getContext(), true);
        rv_history.setAdapter(historyAdapter);
        dbHelper = new HistoryDBHelper(getContext(), "link_history.db");
        historyAdapter.setList(dbHelper.queryData());

        initToolbar();
        initEvent();
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
                if (TextUtils.isEmpty(toolbar.getText())) {
                    showToast("请输入链接");
                } else {
                    onSearch();
                }
            }
        });

        toolbar.setEditorSearchListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(toolbar.getText())) {
                        showToast("请输入链接");
                    } else {
                        onSearch();
                    }
                }
                return false;
            }
        });
    }

    private void initEvent() {
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.clearData();
                historyAdapter.setList(dbHelper.queryData());
            }
        });

        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                toolbar.setText(historyAdapter.getItem(position));
                onSearch();
            }

            @Override
            public void onItemDel(int position) {
                dbHelper.deleteData(historyAdapter.getItem(position));
                historyAdapter.setList(dbHelper.queryData());
            }
        });
    }

    private void onSearch() {
        dbHelper.insertData(toolbar.getText());
        historyAdapter.setList(dbHelper.queryData());
        Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
        intent.putExtra("path", toolbar.getText());
        startAc(intent);
    }
}
