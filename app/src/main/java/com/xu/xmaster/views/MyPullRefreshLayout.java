package com.xu.xmaster.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.xu.xmaster.R;
import com.xu.xmaster.utils.PixelUtils;

public class MyPullRefreshLayout extends QMUIPullRefreshLayout {
    public MyPullRefreshLayout(Context context) {
        super(context);
    }

    public MyPullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View createRefreshView() {
//        return super.createRefreshView();
        return new RefreshView(getContext());
    }

    public static class RefreshView extends LinearLayout implements IRefreshView {

        public RefreshView(Context context) {
            super(context);
            setOrientation(VERTICAL);
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.ic_camera);
            imageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlue)));
            imageView.setMinimumWidth(PixelUtils.dp2px(getContext(),30));
            imageView.setMinimumHeight(PixelUtils.dp2px(getContext(),30));
            addView(imageView);

            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.colorBlue));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setText("下拉打开相机");
            addView(textView);
        }

        @Override
        public void onPull(int offset, int total, int overPull) {
        }

        public void stop() {
        }

        public void doRefresh() {
        }
    }
}
