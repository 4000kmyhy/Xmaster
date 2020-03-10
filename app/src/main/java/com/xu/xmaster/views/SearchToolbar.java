package com.xu.xmaster.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.R;
import com.xu.xmaster.utils.DrawableUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SearchToolbar extends FrameLayout {

    private static final String TAG = "SearchToolbar";

    private View mView;
    private ImageView mLeftBtn;
    private TextView mRightBtn;
    private EditText mInput;

    public interface OnSearchListener {
        void afterTextChanged(String s);
    }

    private OnSearchListener onSearchListener;

    public SearchToolbar(@NonNull Context context) {
        this(context, null);
    }

    public SearchToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
        initAttr(attrs);
        initEvent();
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.search_toolbar, this, false);
        mLeftBtn = mView.findViewById(R.id.toolbar_left_btn);
        mRightBtn = mView.findViewById(R.id.toolbar_right_btn);
        mInput = mView.findViewById(R.id.toolbar_input);
        addView(mView);
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.toolbar);
        int leftBtnRes = typedArray.getResourceId(R.styleable.toolbar_left_btn, R.mipmap.ic_launcher);
        boolean isShowRight = typedArray.getBoolean(R.styleable.toolbar_is_show_right, false);
        String inputHint = typedArray.getString(R.styleable.toolbar_input_hint);
        typedArray.recycle();

        if (leftBtnRes != R.mipmap.ic_launcher) {
            mLeftBtn.setVisibility(VISIBLE);
            mLeftBtn.setImageResource(leftBtnRes);
        }
        if (isShowRight) {
            mRightBtn.setVisibility(VISIBLE);
        }
        if (!TextUtils.isEmpty(inputHint)) {
            mInput.setHint(inputHint);
        }
    }

    private void initEvent() {
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    setClearIconVisible(true);
                } else {
                    setClearIconVisible(false);
                }
                if (onSearchListener != null) {
                    onSearchListener.afterTextChanged(s.toString());
                }
            }
        });
    }

    private void setClearIconVisible(boolean visible) {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_del);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        if (visible) {
            mInput.setCompoundDrawables(mInput.getCompoundDrawables()[0], null, drawable, null);

            new DrawableUtils(mInput, new DrawableUtils.OnDrawableListener() {
                @Override
                public void onLeft(View v, Drawable left) {

                }

                @Override
                public void onRight(View v, Drawable right) {
                    String s = mInput.getText().toString();
                    if (!TextUtils.isEmpty(s)) {
                        mInput.setText("");
                    }
                }
            });
        } else {
            mInput.setCompoundDrawables(mInput.getCompoundDrawables()[0], null, null, null);
        }
    }

    public void setPaddingTop() {
        setPadding(0, QMUIStatusBarHelper.getStatusbarHeight(getContext()), 0, 0);
    }

    public void setLeftBtn(int res) {
        mLeftBtn.setVisibility(VISIBLE);
        mLeftBtn.setImageResource(res);
    }

    public void setIsShowRight(boolean isShow) {
        mRightBtn.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void setHint(String hint) {
        mInput.setHint(hint);
    }

    public void setText(String text) {
        mInput.setText(text);
    }

    public String getText() {
        return mInput.getText().toString();
    }

    public void setLeftBtnOnClickListener(OnClickListener onClickListener) {
        mLeftBtn.setOnClickListener(onClickListener);
    }

    public void setRightBtnOnClickListener(OnClickListener onClickListener) {
        mRightBtn.setOnClickListener(onClickListener);
    }

    public void setEditorSearchListener(TextView.OnEditorActionListener listener) {
        mInput.setOnEditorActionListener(listener);
    }

    public void setOnSearchListener(OnSearchListener listener) {
        onSearchListener = listener;
    }

}
