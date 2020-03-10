package com.xu.xmaster.activities;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.xu.xmaster.R;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.views.SimpleToolbar;
import com.xu.xmaster.views.X5WebView;

public class WebActivity extends BaseActivity {

    private static final String TAG = "WebActivity";

    private SimpleToolbar toolbar;
    private ProgressBar pb_loading;
    private X5WebView webView;

    private String mUrl, mTitle;
    private boolean isClose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_web);

        initView();
    }

    private void initView() {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        toolbar = findViewById(R.id.toolbar);
        pb_loading = findViewById(R.id.pb_loading);
        webView = findViewById(R.id.webView);

        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");
        initToolbar();
        initWebView();
    }

    private void initWebView() {
        webView.init(mUrl);

        webView.setOnWebViewListener(new X5WebView.OnWebViewListener() {
            @Override
            public void onProgressChanged(int progress) {
                pb_loading.setProgress(progress);
                if (progress > 0 && progress < 100) {
                    pb_loading.setVisibility(View.VISIBLE);
                } else if (progress == 100) {
                    pb_loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(String title) {
                toolbar.setTitle(title);
            }

            @Override
            public void onPageStarted(String url) {
                pb_loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(String url, String title) {
                pb_loading.setVisibility(View.GONE);
                //加载完标题可能会变
                if (TextUtils.isEmpty(title)) {
                    toolbar.setTitle("找不到网页");
                } else {
                    toolbar.setTitle(title);
                }
            }
        });
    }

    private void initToolbar() {
        toolbar.setPaddingTop();
        toolbar.setTitle(mTitle);

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClose = true;
                onBackPressed();
            }
        });

        toolbar.setRightBtn1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog();
            }
        });
    }

    private void showBottomDialog() {
        new QMUIBottomSheet.BottomGridSheetBuilder(getContext())
                .addItem(R.drawable.ic_browser, "在浏览器打开", 1, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.ic_reload, "刷新", 2, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView) {
                        dialog.dismiss();
                        int tag = (int) itemView.getTag();
                        switch (tag) {
                            case 1:
                                Uri uri = Uri.parse(mUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                                intent.setClassName("com.UCMobile","com.uc.browser.InnerUCMobile");//打开UC浏览器
//                                intent.setClassName("com.tencent.mtt", "com.tencent.mtt.MainActivity");//打开QQ浏览器
                                if (!TextUtils.isEmpty(uri.toString())) {
                                    startActivity(intent);
                                }
                                break;
                            case 2:
                                webView.reload();
                                break;
                        }
                    }
                })
                .build()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isClose) {
            super.onBackPressed();
        } else {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.clearHistory();
            //先从父容器中移除webview,然后再销毁webview
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
    }
}
