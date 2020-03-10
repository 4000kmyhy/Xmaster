package com.xu.xmaster.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.adapters.CityAdapter;
import com.xu.xmaster.adapters.HistoryAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.database.HistoryDBHelper;
import com.xu.xmaster.views.SearchToolbar;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import me.yokeyword.swipebackfragment.SwipeBackFragment;

public class CityFragment extends SwipeBackFragment {

    private static final String TAG = "CityFragment";

    private SearchToolbar toolbar;
    private FrameLayout layout_history;
    private TextView tv_clear;
    private RecyclerView rv_history, rv_city;

    private HistoryAdapter historyAdapter;
    private CityAdapter cityAdapter;
    private HistoryDBHelper dbHelper;

    public interface OnCitySelectedListener {
        void setCityName(String s);
    }

    private OnCitySelectedListener onCitySelectedListener;

    public void setOnCitySelectedListener(OnCitySelectedListener listener) {
        onCitySelectedListener = listener;
    }

    public static CityFragment newInstance() {
        CityFragment fragment = new CityFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city, container, false);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        layout_history = view.findViewById(R.id.layout_history);
        tv_clear = view.findViewById(R.id.tv_clear);

        rv_history = view.findViewById(R.id.rv_history);
        historyAdapter = new HistoryAdapter(getContext());
        rv_history.setAdapter(historyAdapter);
        dbHelper = new HistoryDBHelper(getContext(), "city_history.db");
        historyAdapter.setList(dbHelper.queryData());

        rv_city = view.findViewById(R.id.rv_city);
        cityAdapter = new CityAdapter(getContext());
        rv_city.setAdapter(cityAdapter);

        initToolbar();
        initEvent();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        toolbar.setOnSearchListener(new SearchToolbar.OnSearchListener() {
            @Override
            public void afterTextChanged(String s) {
                if (TextUtils.isEmpty(s)) {
                    layout_history.setVisibility(View.VISIBLE);
                    rv_city.setVisibility(View.GONE);
                }
            }
        });

        toolbar.setRightBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(toolbar.getText())) {
                    ((BaseActivity) getContext()).showToast("请输入城市名");
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
                        ((BaseActivity) getContext()).showToast("请输入城市名");
                    } else {
                        onSearch();
                    }
                }
                return false;
            }
        });
    }

    private void onSearch() {
        initData(toolbar.getText());
        dbHelper.insertData(toolbar.getText());
        cityAdapter.setName(toolbar.getText());
        historyAdapter.setList(dbHelper.queryData());
        layout_history.setVisibility(View.GONE);
        rv_city.setVisibility(View.VISIBLE);
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

        cityAdapter.setOnItemClickListener(new CityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (onCitySelectedListener != null) {
                    onCitySelectedListener.setCityName(cityAdapter.getLocation(position));
                }
                getActivity().onBackPressed();
            }

            @Override
            public void directSearch() {
                if (onCitySelectedListener != null) {
                    onCitySelectedListener.setCityName(toolbar.getText());
                }
                getActivity().onBackPressed();
            }
        });
    }

    private void initData(String s) {
        HeWeather.getSearch(getContext(),
                s,
                "world",
                10, Lang.CHINESE_SIMPLIFIED,
                new HeWeather.OnResultSearchBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        ((BaseActivity) getContext()).showToast("数据获取错误");
                    }

                    @Override
                    public void onSuccess(Search search) {
                        if (search.getStatus().equals(Code.OK.getCode())) {
                            cityAdapter.setList(search.getBasic());
                        } else {
                            ((BaseActivity) getContext()).showToast(Code.toEnum(search.getStatus()).getTxt());
                        }
                    }
                });
    }
}
