package com.xu.xmaster.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.utils.PixelUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NavItemView extends FrameLayout {

    private static final String TAG = "NavItemView";

    private View mView;
    private LinearLayout mLayout;
    private ImageView mImg;
    private TextView mTv;

    public NavItemView(@NonNull Context context) {
        this(context, null);
    }

    public NavItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
        initAttr(attrs);
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.nav_item, this, false);
        mLayout = mView.findViewById(R.id.nav_item_layout);
        mImg = mView.findViewById(R.id.nav_item_img);
        mTv = mView.findViewById(R.id.nav_item_tv);
        addView(mView);
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.nav_item);
        int src = typedArray.getResourceId(R.styleable.nav_item_src, R.mipmap.ic_launcher);
        String text = typedArray.getString(R.styleable.nav_item_text);
        String gravity = typedArray.getString(R.styleable.nav_item_gravity);
        float paddingTop = typedArray.getDimension(R.styleable.nav_item_layout_padding_top, PixelUtils.dp2px(getContext(), 15));
        float paddingBottom = typedArray.getDimension(R.styleable.nav_item_layout_padding_bottom, PixelUtils.dp2px(getContext(), 15));
        float paddingLeft = typedArray.getDimension(R.styleable.nav_item_layout_padding_left, PixelUtils.dp2px(getContext(), 20));
        float paddingRight = typedArray.getDimension(R.styleable.nav_item_layout_padding_right, PixelUtils.dp2px(getContext(), 20));
        float imageSize = typedArray.getDimension(R.styleable.nav_item_imageview_size, PixelUtils.dp2px(getContext(), 30));
        float textPaddingLeft = typedArray.getDimension(R.styleable.nav_item_textview_padding_left, PixelUtils.dp2px(getContext(), 15));
        float textSize = typedArray.getDimension(R.styleable.nav_item_textsize, PixelUtils.sp2px(getContext(), 18));
        typedArray.recycle();

        if (src != R.mipmap.ic_launcher) {
            mImg.setVisibility(VISIBLE);
            mImg.setImageResource(src);
        }
        if (!TextUtils.isEmpty(text)) {
            mTv.setText(text);
        }
        if (TextUtils.isEmpty(gravity) || TextUtils.equals("vertical", gravity)) {
            mLayout.setGravity(Gravity.CENTER_VERTICAL);
        } else if (TextUtils.equals("center", gravity)) {
            mLayout.setGravity(Gravity.CENTER);
        }
        mLayout.setPadding((int) paddingLeft, (int) paddingTop, (int) paddingRight, (int) paddingBottom);
        ViewGroup.LayoutParams params = mImg.getLayoutParams();
        params.width = (int) imageSize;
        params.height = (int) imageSize;
        mImg.setLayoutParams(params);
        mTv.setPadding((int) textPaddingLeft, 0, 0, 0);
        mTv.setTextSize(PixelUtils.px2sp(getContext(), textSize));
    }
}
