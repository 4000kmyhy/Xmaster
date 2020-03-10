package com.xu.xmaster.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.xu.xmaster.R;

public class TabToolbar extends FrameLayout {

    private static final String TAG = "SimpleToolbar";

    private View mView;
    private ImageView mLeftBtn, mRightBtn;
    private QMUITabSegment mTabSegment;

    public TabToolbar(Context context) {
        this(context, null);
    }

    public TabToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
        initAttr(attrs);
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.tab_toolbar, this, false);
        mLeftBtn = mView.findViewById(R.id.toolbar_left_btn);
        mRightBtn = mView.findViewById(R.id.toolbar_right_btn);
        mTabSegment = mView.findViewById(R.id.toolbar_tabSegment);
        addView(mView);
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.toolbar);
        int leftBtnRes = typedArray.getResourceId(R.styleable.toolbar_left_btn, R.mipmap.ic_launcher);
        int rightBtnRes = typedArray.getResourceId(R.styleable.toolbar_right_btn, R.mipmap.ic_launcher);
        typedArray.recycle();

        if (leftBtnRes != R.mipmap.ic_launcher) {
            mLeftBtn.setVisibility(VISIBLE);
            mLeftBtn.setImageResource(leftBtnRes);
        }
        if (rightBtnRes != R.mipmap.ic_launcher) {
            mRightBtn.setVisibility(VISIBLE);
            mRightBtn.setImageResource(rightBtnRes);
        }
    }

    public void setPaddingTop() {
        setPadding(0, QMUIStatusBarHelper.getStatusbarHeight(getContext()), 0, 0);
    }

    public QMUITabSegment getTabSegment() {
        return mTabSegment;
    }

    public void setLeftBtn(int res) {
        mLeftBtn.setVisibility(VISIBLE);
        mLeftBtn.setImageResource(res);
    }

    public void setRightBtn(int res) {
        mRightBtn.setVisibility(VISIBLE);
        mRightBtn.setImageResource(res);
    }

    public void setLeftBtnOnClickListener(View.OnClickListener onClickListener) {
        mLeftBtn.setOnClickListener(onClickListener);
    }

    public void setRightBtnOnClickListener(View.OnClickListener onClickListener) {
        mRightBtn.setOnClickListener(onClickListener);
    }
}
