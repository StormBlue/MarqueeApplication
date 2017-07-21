package com.example.ytx_gao.marqueeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class MarqueeView extends View {
    private final static String TAG = "MarqueeView";

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
    private int lightGrayColor = default_light_gray_color;

    private int[] lightTargetColors = new int[lightAmount];
    private int[] lightColors = new int[lightAmount];
    private RectF[] lightRectFs = new RectF[lightAmount];
    private Paint lightPaint;

    private boolean isFirstDraw = true;

    private int currentIndex = 0;
    private Interpolator interpolator;
    private Handler uiHandler;

    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_BACKGROUND_COLOR = "saved_bg_color";
    private static final String INSTANCE_BRIGHT_COLOR = "saved_bright_color";
    private static final String INSTANCE_GRAY_COLOR = "saved_gray_color";

    private static final int TYPE_TWINKLE_BRIGHT = 0;
    private static final int TYPE_TWINKLE_TARGET = 1;

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
        init();
        mTypedArray.recycle();
    }

    protected void initByAttributes(TypedArray attributes) {

    }

    private void init() {
        for (int i = 0; i < lightAmount; i++) {
            lightTargetColors[i] = Color.argb((int) (255 * (1 - (float) i / lightAmount)), 0, 245, 170);
        }
        uiHandler = new UIHandler(this);
        initView();
    }

    public void initView() {
        interpolator = new DecelerateInterpolator();
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
            lightColors[i] = lightGrayColor;
            float left = i * gap;
            lightRectFs[i] = new RectF(left, lightDValueTop, left + lightWidth, bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFirstDraw) {
            canvas.drawColor(backgroundColor);
        }
        for (int j = 0; j < lightAmount; j++) {
            lightPaint.setColor(lightColors[j]);
            canvas.drawRoundRect(lightRectFs[j], lightRadius, lightRadius, lightPaint);
        }
    }

    private boolean isTwinkle = false;

    public void startTwinkle() {
        if (!isTwinkle) {
            isTwinkle = true;
            uiHandler.sendEmptyMessage(TYPE_TWINKLE_TARGET);
        }
    }

    public void stopTwinkle() {
        isTwinkle = false;
        currentIndex = 0;
        for (int i = 0; i < lightAmount; i++) {
            lightColors[i] = lightGrayColor;
        }
        invalidate();
    }

    private void twinkle() {
        if (isTwinkle) {
            if ((lightAmount - 1) > currentIndex) {
                uiHandler.sendEmptyMessageDelayed(TYPE_TWINKLE_TARGET, getAnimDelay());
            }
        }
    }

    private static class UIHandler extends Handler {
        private WeakReference<MarqueeView> reference;

        public UIHandler(MarqueeView view) {
            reference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            MarqueeView outer = reference.get();
            if (null == outer) return;
            switch (msg.what) {
                case TYPE_TWINKLE_BRIGHT:
                    outer.toBrightColors();
                    outer.invalidate();
                    outer.twinkle();
                    break;
                case TYPE_TWINKLE_TARGET:
                    outer.toTargetColors();
                    outer.invalidate();
                    outer.twinkle();
                    break;
                default:
                    break;
            }
        }
    }

    private void toBrightColors() {
        for (int i = 0; i < lightAmount; i++) {
            if (lightBrightColor != lightColors[i]) {
                lightColors[i] = lightBrightColor;
                currentIndex = i;
                break;
            }
        }
    }

    private void toTargetColors() {
        for (int i = 0; i < lightAmount; i++) {
            if (lightTargetColors[i] != lightColors[i]) {
                lightColors[i] = lightTargetColors[i];
                currentIndex = i;
                break;
            }
        }
    }

    private int getAnimDelay() {
        int delay = (int) (30 * interpolator.getInterpolation((float) currentIndex / lightAmount));
        Log.d(TAG, "时间间隔为------" + delay);
        return delay;
    }

//    private static class TwinkleThread extends Thread {
//
//        private boolean running = false;
//
//        private MarqueeView view;
//
//        public TwinkleThread(MarqueeView v) {
//            super();
//            view = v;
//        }
//
//        @Override
//        public void run() {
//            for (; ; ) {
//                if (running) {
//                    view.changeBrightColors();
//                    view.postInvalidate();
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(view.getAnimDelay());
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getLightBrightColor() {
        return lightBrightColor;
    }

    public void setLightBrightColor(int lightBrightColor) {
        this.lightBrightColor = lightBrightColor;
    }

    public int getLightGrayColor() {
        return lightGrayColor;
    }

    public void setLightGrayColor(int lightGrayColor) {
        this.lightGrayColor = lightGrayColor;
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
