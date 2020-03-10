package com.xu.xmaster.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.utils.PixelUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class KeyValueView extends FrameLayout {

    private static final String TAG = "KeyValueView";

    private View mView;
    private TextView tv_key, tv_value;

    public KeyValueView(@NonNull Context context) {
        this(context, null);
    }

    public KeyValueView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
        initAttr(attrs);
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.key_value_view, this, false);
        tv_key = mView.findViewById(R.id.tv_key);
        tv_value = mView.findViewById(R.id.tv_value);
        addView(mView);
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.key_value);
        String key_txt = typedArray.getString(R.styleable.key_value_key_txt);
        String value_txt = typedArray.getString(R.styleable.key_value_value_txt);
        int key_color = typedArray.getColor(R.styleable.key_value_key_color, Color.parseColor("#e0e0e0"));
        int value_color = typedArray.getColor(R.styleable.key_value_value_color, Color.parseColor("#ffffff"));
        float text_size = typedArray.getDimension(R.styleable.key_value_text_size, PixelUtils.sp2px(getContext(), 14));
        float key_width = typedArray.getDimension(R.styleable.key_value_key_width, 0);
        typedArray.recycle();

        if (!TextUtils.isEmpty(key_txt)) {
            tv_key.setText(key_txt);
        }
        if (!TextUtils.isEmpty(value_txt)) {
            tv_value.setText(value_txt);
        }

        tv_key.setTextColor(key_color);
        tv_value.setTextColor(value_color);
        tv_key.setTextSize(PixelUtils.px2sp(getContext(), text_size));
        tv_value.setTextSize(PixelUtils.px2sp(getContext(), text_size));

        if (key_width > 0) {
            tv_key.setWidth((int) key_width);
        }
    }

    public TextView getTvKey() {
        return tv_key;
    }

    public TextView getTvValue() {
        return tv_value;
    }
}
