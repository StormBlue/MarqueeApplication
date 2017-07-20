package com.example.ytx_gao.marqueeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class MarqueeView extends View {
    private final String TAG = this.getClass().getSimpleName();

    private int width, height;// view的宽与高

    private final int default_light_amount = 16;
    private final float default_light_radius = 4;
    private final int default_background_color = Color.rgb(11, 58, 41);
    private final int default_light_bright_color = Color.rgb(0, 245, 170);
    private final int default_light_gray_color = Color.rgb(13, 67, 47);

    private int lightAmount = default_light_amount;
    private int gapAmount = lightAmount - 1;

    private float lightWidth, gapWidth;
    private float lightDValueTop;// light距离view顶部距离
    private float lightRadius = default_light_radius;

    private int backgroundColor = default_background_color;
    private int lightBrightColor = default_light_bright_color;

    private int[] lightColors = new int[lightAmount];
    private int[] lightCurrentColors = new int[lightAmount];
    private RectF[] lightRectFs = new RectF[lightAmount];
    private Paint lightPaint;

    private static final String INSTANCE_STATE = "saved_instance";


    public MarqueeView(Context context) {
        this(context, null);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public MarqueeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.Marquee);
        initByAttributes(mTypedArray);
        initView();
        mTypedArray.recycle();
    }

    protected void initByAttributes(TypedArray attributes) {

    }

    public void initView() {
        lightPaint = new Paint();
        lightPaint.setStyle(Paint.Style.FILL);
        lightPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        gapWidth = ((float) width) / (gapAmount + lightAmount * 5);
        lightWidth = 5 * gapWidth;

        lightDValueTop = (height - lightWidth) / 2;
        initLightRectFs();
    }

    private void initLightRectFs() {
        float gap = gapWidth + lightWidth;
        float bottom = lightDValueTop + lightWidth;
        for (int i = 0; i < lightAmount; i++) {
            // 初始化
            lightColors[i] = default_light_gray_color;
            float left = i * gap;
            lightRectFs[i] = new RectF(left, lightDValueTop, left + lightWidth, bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(backgroundColor);
        for (int j = 0; j < lightAmount; j++) {
            lightPaint.setColor(lightColors[j]);
            canvas.drawRoundRect(lightRectFs[j], lightRadius, lightRadius, lightPaint);
            lightCurrentColors[j] = lightColors[j];
        }
    }

    public void twinkle(){

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            initView();
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
