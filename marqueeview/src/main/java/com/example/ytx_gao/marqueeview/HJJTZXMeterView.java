package com.example.ytx_gao.marqueeview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Gao on 28/07/2017.
 */

public class HJJTZXMeterView extends View {
    public HJJTZXMeterView(Context context) {
        this(context, null);
    }

    public HJJTZXMeterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HJJTZXMeterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
