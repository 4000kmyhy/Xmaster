package com.xu.xmaster.utils;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class ScaleTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.5f;

    @Override
    public void transformPage(@NonNull View view, float v) {
        Log.d("ScaleTransformer", "transformPage: " + v);
        if (v < -1) {//[,-1]
//            view.setScaleX(MIN_SCALE);
//            view.setScaleY(MIN_SCALE);
//            view.setAlpha(MIN_ALPHA);
        } else if (v <= 1) {//[-1,1]
            //左边的页面
            if (v < 0) {
                float scaleA = MIN_SCALE + (1 - MIN_SCALE) * (1 + v);
                view.setScaleX(scaleA);
                view.setScaleY(scaleA);
                float alphaA = MIN_ALPHA + (1 - MIN_ALPHA) * (1 + v);
                view.setAlpha(alphaA);
            } else {//右边的页面
                float scaleB = MIN_SCALE + (1 - MIN_SCALE) * (1 - v);
                view.setScaleX(scaleB);
                view.setScaleY(scaleB);
                float alphaB = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - v);
                view.setAlpha(alphaB);
            }
        } else {//[1,]
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
            view.setAlpha(MIN_ALPHA);
        }
    }
}
