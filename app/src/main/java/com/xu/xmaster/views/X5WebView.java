package com.xu.xmaster.views;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.activities.WebActivity;

public class X5WebView extends WebView {

    private static final String TAG = "X5WebView";

    private OnWebViewListener onWebViewListener;

    public interface OnWebViewListener {
        void onProgressChanged(int progress);

        void onReceivedTitle(String title);

        void onPageStarted(String url);

        void onPageFinished(String url, String title);
    }

    public void setOnWebViewListener(OnWebViewListener listener) {
        onWebViewListener = listener;
    }

    public X5WebView(Context context) {
        super(context);
    }

    public X5WebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void init(String url) {
        loadUrl(url);

        initWebSetting();

        setWebViewClient(MyWebViewClient);

        setWebChromeClient(MyWebChromeClient);
    }

    private void initWebSetting() {
        WebSettings webSettings = getSettings();

        webSettings.setJavaScriptEnabled(true);//支持js
        webSettings.setPluginsEnabled(true);//支持插件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        webSettings.setAllowFileAccess(true);//设置可以访问文件
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);//支持内容重新布局
        webSettings.setSupportZoom(true);//支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true);//设置内置的缩放控件。
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        webSettings.setSupportMultipleWindows(true);//多窗口
        //webSettings.setLoadWithOverviewMode(true);
        webSettings.setAppCacheEnabled(true);
        //webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheMaxSize(Long.MAX_VALUE);
        //webSettings.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        //webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//关闭webview中缓存
    }

    private WebViewClient MyWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String s) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + s);
            //防止系统打开手机浏览器
            try {
                if (!s.startsWith("http://") && !s.startsWith("https://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    getContext().startActivity(intent);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                return true;
            }
            webView.loadUrl(s);
            return true;
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            Log.d(TAG, "onPageStarted: " + webView.getTitle() + " " + s);
            if (onWebViewListener != null) {
                onWebViewListener.onPageStarted(s);
            }
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            Log.d(TAG, "onPageFinished: " + webView.getTitle() + " " + s);
            if (onWebViewListener != null) {
                onWebViewListener.onPageFinished(s, webView.getTitle());
            }
        }
    };

    private WebChromeClient MyWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView webView, int i) {
            super.onProgressChanged(webView, i);
            Log.d(TAG, "onProgressChanged: " + i);
            if (onWebViewListener != null) {
                onWebViewListener.onProgressChanged(i);
            }
        }

        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            Log.d(TAG, "onReceivedTitle: " + webView.getTitle());
            if (onWebViewListener != null) {
                onWebViewListener.onReceivedTitle(webView.getTitle());
            }
        }

        @Override
        public boolean onCreateWindow(WebView webView, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView newWebView = new WebView(getContext());
            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    //<a target="_blank">需要重新打开一个页面
                    Intent intent = new Intent(getContext(), WebActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", view.getTitle());
                    ((BaseActivity) getContext()).startAc(intent);
                    return true;
                }
            });
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }
    };
}
