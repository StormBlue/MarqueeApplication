// Copyright 2004-present Facebook. All Rights Reserved.

package com.example.ytx_gao.marqueeview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class TestFrameLayout extends FrameLayout {

    private static final String TAG = "ShimmerFrameLayout";

    public TestFrameLayout(Context context) {
        this(context, null, 0);
    }

    public TestFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawColor(Color.argb(100, 100, 100, 100));
    }
}
