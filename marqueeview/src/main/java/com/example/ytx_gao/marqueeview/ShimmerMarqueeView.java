// Copyright 2004-present Facebook. All Rights Reserved.

package com.example.ytx_gao.marqueeview;

import android.animation.Animator;
import android.animation.AnimatorSet;
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
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

public class ShimmerMarqueeView extends View {

    private static final String TAG = "ShimmerMarqueeView";
    private static final PorterDuffXfermode DST_IN_PORTER_DUFF_XFERMODE = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    private int width, height;

    private final int default_light_amount = 16;
    private final float default_light_radius = 4;
    private final int default_shimmer_mask_color = Color.rgb(201, 255, 232);
    private final int default_background_color = Color.rgb(11, 58, 41);
    private final int default_light_bright_color = Color.rgb(0, 245, 170);
    private final int default_light_gray_color = Color.rgb(13, 67, 47);

    private int lightAmount = default_light_amount;
    private int gapAmount = lightAmount - 1;

    private float lightWidth, gapWidth;
    private float lightRadius = default_light_radius;

    private int shimmerMaskColor = default_shimmer_mask_color;
    private int lightBrightColor = default_light_bright_color;
    private int lightGrayColor = default_light_gray_color;

    private int[] lightTargetColors = new int[lightAmount];
    private int[] lightCurrentColors = new int[lightAmount];
    private RectF[] lightRectFs = new RectF[lightAmount];
    private Paint lightPaint;

    private Paint mMaskShimmerPaint, mMaskPaint;

    private ShimmerMask mShimmerMask;
    private ShimmerMaskTranslation mShimmerMaskTranslation;

    private Bitmap mShimmerRenderMaskBitmap;

    private boolean mAutoStart;
    private int mProgressDuration, mAnimatorSetDelay, mShimmerDuration;
    private int mShimmerRepeatCount;
    private int mShimmerRepeatDelay;
    private int mShimmerRepeatMode;

    private int mShimmerMaskOffsetX;
    private int mShimmerMaskOffsetY;

    private boolean mAnimationsStarted, mProgressAnimationFinished, mNullAnimationFinished;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;

    protected AnimatorSet mAnimatorSet;
    protected ValueAnimator mProgressAnimator, mNullAnimator, mShimmerAnimator;
    protected Bitmap mShimmerMaskBitmap;

    public ShimmerMarqueeView(Context context) {
        this(context, null, 0);
    }

    public ShimmerMarqueeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShimmerMarqueeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mShimmerMask = new ShimmerMask();
        mMaskShimmerPaint = new Paint();
        mMaskShimmerPaint.setAntiAlias(true);
        mMaskShimmerPaint.setColor(shimmerMaskColor);
        mMaskPaint = new Paint();
        mMaskPaint.setAntiAlias(true);
        mMaskPaint.setDither(true);
        mMaskPaint.setFilterBitmap(true);
        mMaskPaint.setXfermode(DST_IN_PORTER_DUFF_XFERMODE);

        useDefaults();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerMarqueeView, 0, 0);
            try {
                if (a.hasValue(R.styleable.ShimmerMarqueeView_auto_start)) {
                    setAutoStart(a.getBoolean(R.styleable.ShimmerMarqueeView_auto_start, false));
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_duration)) {
                    setShimmerDuration(a.getInt(R.styleable.ShimmerMarqueeView_shimmer_duration, 2000));
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_progress_duration)) {
                    setProgressDuration(a.getInt(R.styleable.ShimmerMarqueeView_progress_duration, 150));
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_animator_set_delay)) {
                    setAnimatorSetDelay(a.getInt(R.styleable.ShimmerMarqueeView_animator_set_delay, 0));
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_repeat_count)) {
                    setShimmerRepeatCount(a.getInt(R.styleable.ShimmerMarqueeView_shimmer_repeat_count, 0));
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_repeat_delay)) {
                    setShimmerRepeatDelay(a.getInt(R.styleable.ShimmerMarqueeView_shimmer_repeat_delay, 0));
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_repeat_mode)) {
                    setShimmerRepeatMode(a.getInt(R.styleable.ShimmerMarqueeView_shimmer_repeat_mode, 0));
                }

                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_angle)) {
                    int angle = a.getInt(R.styleable.ShimmerMarqueeView_shimmer_angle, 0);
                    switch (angle) {
                        default:
                        case 0:
                            mShimmerMask.angle = ShimmerMaskAngle.CW_0;
                            break;
                        case 90:
                            mShimmerMask.angle = ShimmerMaskAngle.CW_90;
                            break;
                        case 180:
                            mShimmerMask.angle = ShimmerMaskAngle.CW_180;
                            break;
                        case 270:
                            mShimmerMask.angle = ShimmerMaskAngle.CW_270;
                            break;
                    }
                }

                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_shape)) {
                    int shape = a.getInt(R.styleable.ShimmerMarqueeView_shimmer_shape, 0);
                    switch (shape) {
                        default:
                        case 0:
                            mShimmerMask.shape = ShimmerMaskShape.LINEAR;
                            break;
                        case 1:
                            mShimmerMask.shape = ShimmerMaskShape.RADIAL;
                            break;
                    }
                }

                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_dropoff)) {
                    mShimmerMask.dropoff = a.getFloat(R.styleable.ShimmerMarqueeView_shimmer_dropoff, 0);
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_fixed_width)) {
                    mShimmerMask.fixedWidth = a.getDimensionPixelSize(R.styleable.ShimmerMarqueeView_shimmer_fixed_width, 0);
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_fixed_height)) {
                    mShimmerMask.fixedHeight = a.getDimensionPixelSize(R.styleable.ShimmerMarqueeView_shimmer_fixed_height, 0);
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_intensity)) {
                    mShimmerMask.intensity = a.getFloat(R.styleable.ShimmerMarqueeView_shimmer_intensity, 0);
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_relative_width)) {
                    mShimmerMask.relativeWidth = a.getFloat(R.styleable.ShimmerMarqueeView_shimmer_relative_width, 0);
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_relative_height)) {
                    mShimmerMask.relativeHeight = a.getFloat(R.styleable.ShimmerMarqueeView_shimmer_relative_height, 0);
                }
                if (a.hasValue(R.styleable.ShimmerMarqueeView_shimmer_tilt)) {
                    mShimmerMask.tilt = a.getFloat(R.styleable.ShimmerMarqueeView_shimmer_tilt, 0);
                }
            } finally {
                a.recycle();
            }
        }
        init();
    }

    /**
     * Resets the layout to its default state. Any parameters that were set or modified will be reverted back to their
     * original value. Also, stops the shimmer animation if it is currently playing.
     */
    public void useDefaults() {
        // Set defaults
        setAutoStart(false);
        setProgressDuration(150);
        setAnimatorSetDelay(0);
        setShimmerDuration(2000);
        setShimmerRepeatCount(ObjectAnimator.INFINITE);
        setShimmerRepeatDelay(0);
        setShimmerRepeatMode(ObjectAnimator.RESTART);

        mShimmerMask.angle = ShimmerMaskAngle.CW_0;
        mShimmerMask.shape = ShimmerMaskShape.LINEAR;
        mShimmerMask.dropoff = 0.5f;
        mShimmerMask.fixedWidth = 0;
        mShimmerMask.fixedHeight = 0;
        mShimmerMask.intensity = 0.0f;
        mShimmerMask.relativeWidth = 1.0f;
        mShimmerMask.relativeHeight = 1.0f;
        mShimmerMask.tilt = 20;

        mShimmerMaskTranslation = new ShimmerMaskTranslation();

        resetAll();
    }

    private void init() {
        lightPaint = new Paint();
        lightPaint.setStyle(Paint.Style.FILL);
        lightPaint.setAntiAlias(true);
        for (int i = 0; i < lightAmount; i++) {
            lightCurrentColors[i] = lightGrayColor;
            lightTargetColors[i] = Color.argb((int) (255 * (1 - (float) i / lightAmount)), 0, 245, 170);
//            lightTargetColors[i] = Color.rgb((int) (255 * (1 - (float) i / lightAmount)), 245, 170);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mGlobalLayoutListener == null) {
            mGlobalLayoutListener = getLayoutListener();
        }
        getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    private ViewTreeObserver.OnGlobalLayoutListener getLayoutListener() {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean animationStarted = mAnimationsStarted;
                resetAll();
                if (mAutoStart || animationStarted) {
                    startAnimations();
                }
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimations();
        if (mGlobalLayoutListener != null) {
            getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
            mGlobalLayoutListener = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        gapWidth = ((float) width) / (gapAmount + lightAmount * 5);
        lightWidth = 5 * gapWidth;
        // measure and set height
        height = (int) lightWidth;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        initLightRectFs();
    }

    private void initLightRectFs() {
        float gap = gapWidth + lightWidth;
        for (int i = 0; i < lightAmount; i++) {
            // 初始化
            float left = i * gap;
            lightRectFs[i] = new RectF(left, 0, left + lightWidth, lightWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!(mProgressAnimationFinished && mNullAnimationFinished) || getWidth() <= 0 || getHeight() <= 0) {
            drawCurrentStyle(canvas);
            return;
        }
        dispatchDrawUsingBitmap(canvas);
    }

    private void drawCurrentStyle(Canvas renderCanvas) {
        for (int j = 0; j < lightAmount; j++) {
            if (lightPaint.getColor() != lightCurrentColors[j]) {
                lightPaint.setColor(lightCurrentColors[j]);
            }
            renderCanvas.drawRoundRect(lightRectFs[j], lightRadius, lightRadius, lightPaint);
        }
    }

    /**
     * Draws and masks the children using a Bitmap.
     *
     * @param canvas Canvas that the masked children will end up being drawn to.
     */
    private boolean dispatchDrawUsingBitmap(Canvas canvas) {
        Bitmap maskBitmap = tryObtainRenderMaskBitmap();
        if (maskBitmap == null) {
            return false;
        }
        // First draw a original version
        for (int j = 0; j < lightAmount; j++) {
            if (lightPaint.getColor() != lightCurrentColors[j]) {
                lightPaint.setColor(lightTargetColors[j]);
            }
            canvas.drawRoundRect(lightRectFs[j], lightRadius, lightRadius, lightPaint);
        }

        // Then draw the shimmery mask
        drawMasked(new Canvas(maskBitmap));
        canvas.drawBitmap(maskBitmap, 0, 0, null);

        return true;
    }

    public void setLightTargetColors(int[] targetColors) {
        if (targetColors == null) {
            return;
        }
        int minAmount = Math.min(targetColors.length, lightAmount);
        System.arraycopy(targetColors, 0, lightTargetColors, 0, minAmount);
        resetAll();
    }

    /**
     * Start the animations. If the 'auto start' property is set, this method is called automatically when the
     * layout is attached to the current window. Calling this method has no effect if the animation is already playing.
     */
    public void startAnimations() {
        if (mAnimationsStarted) {
            return;
        }
        resetAll();
        mAnimatorSet = getAnimatorSet();
        mAnimatorSet.start();
        mAnimationsStarted = true;
    }

    /**
     * Stop the animations. Calling this method has no effect if the animation hasn't been started yet.
     */
    public void stopAnimations() {
        if (mAnimatorSet != null) {
            mAnimatorSet.end();
            mAnimatorSet.cancel();
        }
        mAnimatorSet = null;

        if (mProgressAnimator != null) {
            mProgressAnimator.end();
            mProgressAnimator.removeAllUpdateListeners();
            mProgressAnimator.removeAllListeners();
            mProgressAnimator.cancel();
        }
        mProgressAnimator = null;
        mProgressAnimationFinished = false;

        if (mNullAnimator != null) {
            mNullAnimator.end();
            mNullAnimator.removeAllUpdateListeners();
            mNullAnimator.removeAllListeners();
            mNullAnimator.cancel();
        }
        mNullAnimator = null;
        mNullAnimationFinished = false;

        if (mShimmerAnimator != null) {
            mShimmerAnimator.end();
            mShimmerAnimator.removeAllUpdateListeners();
            mShimmerAnimator.removeAllListeners();
            mShimmerAnimator.cancel();
        }
        mShimmerAnimator = null;

        mAnimationsStarted = false;
    }

    private Bitmap tryObtainRenderMaskBitmap() {
        if (mShimmerRenderMaskBitmap == null) {
            mShimmerRenderMaskBitmap = tryCreateRenderBitmap();
        }
        return mShimmerRenderMaskBitmap;
    }

    private Bitmap tryCreateRenderBitmap() {
        int width = getWidth();
        int height = getHeight();
        try {
            return createBitmapAndGcIfNecessary(width, height);
        } catch (OutOfMemoryError e) {
            String logMessage = "ShimmerMarqueeView failed to create working bitmap";
            StringBuilder logMessageStringBuilder = new StringBuilder(logMessage);
            logMessageStringBuilder.append(" (width = ");
            logMessageStringBuilder.append(width);
            logMessageStringBuilder.append(", height = ");
            logMessageStringBuilder.append(height);
            logMessageStringBuilder.append(")\n\n");
            for (StackTraceElement stackTraceElement :
                    Thread.currentThread().getStackTrace()) {
                logMessageStringBuilder.append(stackTraceElement.toString());
                logMessageStringBuilder.append("\n");
            }
            logMessage = logMessageStringBuilder.toString();
            Log.d(TAG, logMessage);
        }
        return null;
    }

    // Draws the children and masks them on the given Canvas.
    private void drawMasked(Canvas renderCanvas) {
        Bitmap maskBitmap = getMaskBitmap();
        if (maskBitmap == null) {
            return;
        }

        renderCanvas.clipRect(
                mShimmerMaskOffsetX,
                mShimmerMaskOffsetY,
                mShimmerMaskOffsetX + maskBitmap.getWidth(),
                mShimmerMaskOffsetY + maskBitmap.getHeight());
        for (int j = 0; j < lightAmount; j++) {
            renderCanvas.drawRoundRect(lightRectFs[j], lightRadius, lightRadius, mMaskShimmerPaint);
        }

        renderCanvas.drawBitmap(maskBitmap, mShimmerMaskOffsetX, mShimmerMaskOffsetY, mMaskPaint);
    }

    private void resetAll() {
        stopAnimations();
        for (int j = 0; j < lightAmount; j++) {
            lightCurrentColors[j] = lightGrayColor;
        }
        resetMaskBitmap();
        resetRenderedView();
        invalidate();
    }

    // If a mask bitmap was created, it's recycled and set to null so it will be recreated when needed.
    private void resetMaskBitmap() {
        if (mShimmerMaskBitmap != null) {
            mShimmerMaskBitmap.recycle();
            mShimmerMaskBitmap = null;
        }
    }

    // If a working bitmap was created, it's recycled and set to null so it will be recreated when needed.
    private void resetRenderedView() {
        if (mShimmerRenderMaskBitmap != null) {
            mShimmerRenderMaskBitmap.recycle();
            mShimmerRenderMaskBitmap = null;
        }
    }

    // Return the mask bitmap, creating it if necessary.
    private Bitmap getMaskBitmap() {
        if (mShimmerMaskBitmap != null) {
            return mShimmerMaskBitmap;
        }

        int width = mShimmerMask.maskWidth(getWidth());
        int height = mShimmerMask.maskHeight(getHeight());

        mShimmerMaskBitmap = createBitmapAndGcIfNecessary(width, height);
        Canvas canvas = new Canvas(mShimmerMaskBitmap);
        Shader gradient;
        switch (mShimmerMask.shape) {
            default:
            case LINEAR: {
                int x1, y1;
                int x2, y2;
                switch (mShimmerMask.angle) {
                    default:
                    case CW_0:
                        x1 = 0;
                        y1 = 0;
                        x2 = width;
                        y2 = 0;
                        break;
                    case CW_90:
                        x1 = 0;
                        y1 = 0;
                        x2 = 0;
                        y2 = height;
                        break;
                    case CW_180:
                        x1 = width;
                        y1 = 0;
                        x2 = 0;
                        y2 = 0;
                        break;
                    case CW_270:
                        x1 = 0;
                        y1 = height;
                        x2 = 0;
                        y2 = 0;
                        break;
                }
                gradient =
                        new LinearGradient(
                                x1, y1,
                                x2, y2,
                                mShimmerMask.getGradientColors(),
                                mShimmerMask.getGradientPositions(),
                                Shader.TileMode.REPEAT);
                break;
            }
            case RADIAL: {
                int x = width / 2;
                int y = height / 2;
                gradient =
                        new RadialGradient(
                                x,
                                y,
                                (float) (Math.max(width, height) / Math.sqrt(2)),
                                mShimmerMask.getGradientColors(),
                                mShimmerMask.getGradientPositions(),
                                Shader.TileMode.REPEAT);
                break;
            }
        }
        canvas.rotate(mShimmerMask.tilt, width / 2, height / 2);
        Paint paint = new Paint();
        paint.setShader(gradient);
        // We need to increase the rect size to account for the tilt
        int padding = (int) (Math.sqrt(2) * Math.max(width, height)) / 2;
        canvas.drawRect(-padding, -padding, width + padding, height + padding, paint);

        return mShimmerMaskBitmap;
    }

    public AnimatorSet getAnimatorSet() {
        if (mAnimatorSet != null) {
            return mAnimatorSet;
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(getProgressAnimation(), getNullAnimation(), getShimmerAnimation());
        return mAnimatorSet;
    }

    private Animator getNullAnimation() {
        if (mNullAnimator != null) {
            return mNullAnimator;
        }
        mNullAnimator = ValueAnimator.ofFloat(1.0F, 0F);
        mNullAnimator.setDuration(mAnimatorSetDelay);
        mNullAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mNullAnimationFinished = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return mNullAnimator;
    }

    private Animator getProgressAnimation() {
        if (mProgressAnimator != null) {
            return mProgressAnimator;
        }
        mProgressAnimator = ValueAnimator.ofInt(0, lightAmount - 1);
        mProgressAnimator.setDuration(mProgressDuration);
        mProgressAnimator.setInterpolator(new LinearInterpolator());
        mProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressAnimationFinished = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mProgressAnimator.addPauseListener(new Animator.AnimatorPauseListener() {
            @Override
            public void onAnimationPause(Animator animation) {

            }

            @Override
            public void onAnimationResume(Animator animation) {

            }
        });
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                for (int i = 0; i <= value; i++) {
                    if (lightCurrentColors[i] != lightTargetColors[i]) {
                        lightCurrentColors[i] = lightTargetColors[i];
                    }
                }
                invalidate();
            }
        });
        return mProgressAnimator;
    }

    // Get the shimmer <a href="http://developer.android.com/reference/android/animation/Animator.html">Animator</a>
    // object, which is responsible for driving the highlight mask animation.
    private Animator getShimmerAnimation() {
        if (mShimmerAnimator != null) {
            return mShimmerAnimator;
        }
        int width = getWidth();
        int height = getHeight();
        switch (mShimmerMask.shape) {
            default:
            case LINEAR:
                switch (mShimmerMask.angle) {
                    default:
                    case CW_0:
                        mShimmerMaskTranslation.set(-width, 0, width, 0);
                        break;
                    case CW_90:
                        mShimmerMaskTranslation.set(0, -height, 0, height);
                        break;
                    case CW_180:
                        mShimmerMaskTranslation.set(width, 0, -width, 0);
                        break;
                    case CW_270:
                        mShimmerMaskTranslation.set(0, height, 0, -height);
                        break;
                }
        }
        mShimmerAnimator = ValueAnimator.ofFloat(0.0f, 1.0f + (float) mShimmerRepeatDelay / mShimmerDuration);
        mShimmerAnimator.setDuration(mShimmerDuration + mShimmerRepeatDelay);
        mShimmerAnimator.setRepeatCount(mShimmerRepeatCount);
        mShimmerAnimator.setRepeatMode(mShimmerRepeatMode);
        mShimmerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = Math.max(0.0f, Math.min(1.0f, (Float) animation.getAnimatedValue()));
                setMaskOffsetX((int) (mShimmerMaskTranslation.fromX * (1 - value) + mShimmerMaskTranslation.toX * value));
                setMaskOffsetY((int) (mShimmerMaskTranslation.fromY * (1 - value) + mShimmerMaskTranslation.toY * value));
            }
        });
        return mShimmerAnimator;
    }

    /**
     * Creates a bitmap with the given width and height.
     * <p/>
     * If it fails with an OutOfMemory error, it will force a GC and then try to create the bitmap
     * one more time.
     *
     * @param width  width of the bitmap
     * @param height height of the bitmap
     */
    protected static Bitmap createBitmapAndGcIfNecessary(int width, int height) {
        try {
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            System.gc();
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
    }

    // enum specifying the shape of the highlight mask applied to the contained view
    private enum ShimmerMaskShape {
        LINEAR,
        RADIAL
    }

    // enum controlling the angle of the highlight mask animation
    private enum ShimmerMaskAngle {
        CW_0, // left to right
        CW_90, // top to bottom
        CW_180, // right to left
        CW_270, // bottom to top
    }

    // struct storing various mask related parameters, which are used to construct the mask bitmap
    private static class ShimmerMask {

        public ShimmerMaskAngle angle;
        public float tilt;
        public float dropoff;
        public int fixedWidth;
        public int fixedHeight;
        public float intensity;
        public float relativeWidth;
        public float relativeHeight;
        public ShimmerMaskShape shape;

        public int maskWidth(int width) {
            return fixedWidth > 0 ? fixedWidth : (int) (width * relativeWidth);
        }

        public int maskHeight(int height) {
            return fixedHeight > 0 ? fixedHeight : (int) (height * relativeHeight);
        }

        /**
         * Get the array of colors to be distributed along the gradient of the mask bitmap
         *
         * @return An array of black and transparent colors
         */
        public int[] getGradientColors() {
            switch (shape) {
                default:
                case LINEAR:
                    return new int[]{Color.TRANSPARENT, Color.BLACK, Color.BLACK, Color.TRANSPARENT};
                case RADIAL:
                    return new int[]{Color.BLACK, Color.BLACK, Color.TRANSPARENT};
            }
        }

        /**
         * Get the array of relative positions [0..1] of each corresponding color in the colors array
         *
         * @return A array of float values in the [0..1] range
         */
        public float[] getGradientPositions() {
            switch (shape) {
                default:
                case LINEAR:
                    return new float[]{
                            Math.max((1.0f - intensity - dropoff) / 2, 0.0f),
                            Math.max((1.0f - intensity) / 2, 0.0f),
                            Math.min((1.0f + intensity) / 2, 1.0f),
                            Math.min((1.0f + intensity + dropoff) / 2, 1.0f)};
                case RADIAL:
                    return new float[]{
                            0.0f,
                            Math.min(intensity, 1.0f),
                            Math.min(intensity + dropoff, 1.0f)};
            }
        }
    }

    // struct for storing the mask translation animation values
    private static class ShimmerMaskTranslation {

        public int fromX;
        public int fromY;
        public int toX;
        public int toY;

        public void set(int fromX, int fromY, int toX, int toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    /**
     * Whether the shimmer animation is currently underway.
     *
     * @return True if the shimmer animation is playing, false otherwise.
     */
    public boolean isAnimationStarted() {
        return mAnimationsStarted;
    }

    /**
     * Translate the mask offset horizontally. Used by the animator.
     *
     * @param maskOffsetX Horizontal translation offset of the mask
     */
    private void setMaskOffsetX(int maskOffsetX) {
        if (mShimmerMaskOffsetX == maskOffsetX) {
            return;
        }
        mShimmerMaskOffsetX = maskOffsetX;
        invalidate();
    }

    /**
     * Translate the mask offset vertically. Used by the animator.
     *
     * @param maskOffsetY Vertical translation offset of the mask
     */
    private void setMaskOffsetY(int maskOffsetY) {
        if (mShimmerMaskOffsetY == maskOffsetY) {
            return;
        }
        mShimmerMaskOffsetY = maskOffsetY;
        invalidate();
    }

    private static float clamp(float min, float max, float value) {
        return Math.min(max, Math.max(min, value));
    }

    /**
     * Is 'auto start' enabled for this layout. When auto start is enabled, the layout will start animating automatically
     * whenever it is attached to the current window.
     *
     * @return True if 'auto start' is enabled, false otherwise
     */
    public boolean isAutoStart() {
        return mAutoStart;
    }

    /**
     * Enable or disable 'auto start' for this layout. When auto start is enabled, the layout will start animating
     * automatically whenever it is attached to the current window.
     *
     * @param autoStart Whether auto start should be enabled or not
     */
    public void setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
        resetAll();
    }

    public static PorterDuffXfermode getDstInPorterDuffXfermode() {
        return DST_IN_PORTER_DUFF_XFERMODE;
    }

    public int getProgressDuration() {
        return mProgressDuration;
    }

    public void setProgressDuration(int mProgressDuration) {
        this.mProgressDuration = mProgressDuration;
    }

    public int getAnimatorSetDelay() {
        return mAnimatorSetDelay;
    }

    public void setAnimatorSetDelay(int mAminatorSetDelay) {
        this.mAnimatorSetDelay = mAminatorSetDelay;
    }

    /**
     * Get the duration of the current animation i.e. the time it takes for the highlight to move from one end
     * of the layout to the other. The default value is 1000 ms.
     *
     * @return Duration of the animation, in milliseconds
     */
    public int getShimmerDuration() {
        return mShimmerDuration;
    }

    /**
     * Set the duration of the animation i.e. the time it will take for the highlight to move from one end of the layout
     * to the other.
     *
     * @param duration Duration of the animation, in milliseconds
     */
    public void setShimmerDuration(int duration) {
        mShimmerDuration = duration;
        resetAll();
    }

    /**
     * Get the number of times of the current animation will repeat. The default value is -1, which means the animation
     * will repeat indefinitely.
     *
     * @return Number of times the current animation will repeat, or -1 for indefinite.
     */
    public int getShimmerRepeatCount() {
        return mShimmerRepeatCount;
    }

    /**
     * Set the number of times the animation should repeat. If the repeat count is 0, the animation stops after reaching
     * the end. If greater than 0, or -1 (for infinite), the repeat mode is taken into account.
     *
     * @param repeatCount Number of times the current animation should repeat, or -1 for indefinite.
     */
    public void setShimmerRepeatCount(int repeatCount) {
        mShimmerRepeatCount = repeatCount;
        resetAll();
    }

    /**
     * Get the delay after which the current animation will repeat. The default value is 0, which means the animation
     * will repeat immediately, unless it has ended.
     *
     * @return Delay after which the current animation will repeat, in milliseconds.
     */
    public int getShimmerRepeatDelay() {
        return mShimmerRepeatDelay;
    }

    /**
     * Set the delay after which the animation repeat, unless it has ended.
     *
     * @param repeatDelay Delay after which the animation should repeat, in milliseconds.
     */
    public void setShimmerRepeatDelay(int repeatDelay) {
        mShimmerRepeatDelay = repeatDelay;
        resetAll();
    }

    /**
     * Get what the current animation will do after reaching the end. One of
     * <a href="http://developer.android.com/reference/android/animation/ValueAnimator.html#REVERSE">REVERSE</a> or
     * <a href="http://developer.android.com/reference/android/animation/ValueAnimator.html#RESTART">RESTART</a>
     *
     * @return Repeat mode of the current animation
     */
    public int getShimmerRepeatMode() {
        return mShimmerRepeatMode;
    }

    /**
     * Set what the animation should do after reaching the end. One of
     * <a href="http://developer.android.com/reference/android/animation/ValueAnimator.html#REVERSE">REVERSE</a> or
     * <a href="http://developer.android.com/reference/android/animation/ValueAnimator.html#RESTART">RESTART</a>
     *
     * @param repeatMode Repeat mode of the animation
     */
    public void setShimmerRepeatMode(int repeatMode) {
        mShimmerRepeatMode = repeatMode;
        resetAll();
    }

    /**
     * Get the shape of the current animation's highlight mask. One of {@link ShimmerMaskShape#LINEAR} or
     * {@link ShimmerMaskShape#RADIAL}
     *
     * @return The shape of the highlight mask
     */
    public ShimmerMaskShape getShimmerMaskShape() {
        return mShimmerMask.shape;
    }

    /**
     * Set the shape of the animation's highlight mask. One of {@link ShimmerMaskShape#LINEAR} or {@link ShimmerMaskShape#RADIAL}
     *
     * @param shape The shape of the highlight mask
     */
    public void setShimmerMaskShape(ShimmerMaskShape shape) {
        mShimmerMask.shape = shape;
        resetAll();
    }

    /**
     * Get the angle at which the highlight mask is animated. One of:
     * <ul>
     * <li>{@link ShimmerMaskAngle#CW_0} which animates left to right,</li>
     * <li>{@link ShimmerMaskAngle#CW_90} which animates top to bottom,</li>
     * <li>{@link ShimmerMaskAngle#CW_180} which animates right to left, or</li>
     * <li>{@link ShimmerMaskAngle#CW_270} which animates bottom to top</li>
     * </ul>
     *
     * @return The {@link ShimmerMaskAngle} of the current animation
     */
    public ShimmerMaskAngle getShimmerAngle() {
        return mShimmerMask.angle;
    }

    /**
     * Set the angle of the highlight mask animation. One of:
     * <ul>
     * <li>{@link ShimmerMaskAngle#CW_0} which animates left to right,</li>
     * <li>{@link ShimmerMaskAngle#CW_90} which animates top to bottom,</li>
     * <li>{@link ShimmerMaskAngle#CW_180} which animates right to left, or</li>
     * <li>{@link ShimmerMaskAngle#CW_270} which animates bottom to top</li>
     * </ul>
     *
     * @param angle The {@link ShimmerMaskAngle} of the new animation
     */
    public void setShimmerAngle(ShimmerMaskAngle angle) {
        mShimmerMask.angle = angle;
        resetAll();
    }

    /**
     * Get the dropoff of the current animation's highlight mask. Dropoff controls the size of the fading edge of the
     * highlight.
     * <p/>
     * The default value of dropoff is 0.5.
     *
     * @return Dropoff of the highlight mask
     */
    public float getShimmerDropoff() {
        return mShimmerMask.dropoff;
    }

    /**
     * Set the dropoff of the animation's highlight mask, which defines the size of the highlight's fading edge.
     * <p/>
     * It is the relative distance from the center at which the highlight mask's opacity is 0 i.e it is fully transparent.
     * For a linear mask, the distance is relative to the center towards the edges. For a radial mask, the distance is
     * relative to the center towards the circumference. So a dropoff of 0.5 on a linear mask will create a band that
     * is half the size of the corresponding edge (depending on the {@link ShimmerMaskAngle}), centered in the layout.
     *
     * @param dropoff
     */
    public void setShimmerDropoff(float dropoff) {
        mShimmerMask.dropoff = dropoff;
        resetAll();
    }

    /**
     * Get the fixed width of the highlight mask, or 0 if it is not set. By default it is 0.
     *
     * @return The width of the highlight mask if set, in pixels.
     */
    public int getShimmerFixedWidth() {
        return mShimmerMask.fixedWidth;
    }

    /**
     * Set the fixed width of the highlight mask, regardless of the size of the layout.
     *
     * @param fixedWidth The width of the highlight mask in pixels.
     */
    public void setShimmerFixedWidth(int fixedWidth) {
        mShimmerMask.fixedWidth = fixedWidth;
        resetAll();
    }

    /**
     * Get the fixed height of the highlight mask, or 0 if it is not set. By default it is 0.
     *
     * @return The height of the highlight mask if set, in pixels.
     */
    public int getShimmerFixedHeight() {
        return mShimmerMask.fixedHeight;
    }

    /**
     * Set the fixed height of the highlight mask, regardless of the size of the layout.
     *
     * @param fixedHeight The height of the highlight mask in pixels.
     */
    public void setShimmerFixedHeight(int fixedHeight) {
        mShimmerMask.fixedHeight = fixedHeight;
        resetAll();
    }

    /**
     * Get the intensity of the highlight mask, in the [0..1] range. The intensity controls the brightness of the
     * highlight; the higher it is, the greater is the opaque region in the highlight. The default value is 0.
     *
     * @return The intensity of the highlight mask
     */
    public float getShimmerIntensity() {
        return mShimmerMask.intensity;
    }

    /**
     * Set the intensity of the highlight mask, in the [0..1] range.
     * <p/>
     * Intensity is the point relative to the center where opacity starts dropping off, so an intensity of 0 would mean
     * that the highlight starts becoming translucent immediately from the center (the spread is controlled by 'dropoff').
     *
     * @param intensity The intensity of the highlight mask.
     */
    public void setShimmerIntensity(float intensity) {
        mShimmerMask.intensity = intensity;
        resetAll();
    }

    /**
     * Get the width of the highlight mask relative to the layout's width. The default is 1.0, meaning that the mask is
     * of the same width as the layout.
     *
     * @return Relative width of the highlight mask.
     */
    public float getShimmerRelativeWidth() {
        return mShimmerMask.relativeWidth;
    }

    /**
     * Set the width of the highlight mask relative to the layout's width, in the [0..1] range.
     *
     * @param relativeWidth Relative width of the highlight mask.
     */
    public void setShimmerRelativeWidth(int relativeWidth) {
        mShimmerMask.relativeWidth = relativeWidth;
        resetAll();
    }

    /**
     * Get the height of the highlight mask relative to the layout's height. The default is 1.0, meaning that the mask is
     * of the same height as the layout.
     *
     * @return Relative height of the highlight mask.
     */
    public float getShimmerRelativeHeight() {
        return mShimmerMask.relativeHeight;
    }

    /**
     * Set the height of the highlight mask relative to the layout's height, in the [0..1] range.
     *
     * @param relativeHeight Relative height of the highlight mask.
     */
    public void setShimmerRelativeHeight(int relativeHeight) {
        mShimmerMask.relativeHeight = relativeHeight;
        resetAll();
    }

    /**
     * Get the tilt angle of the highlight, in degrees. The default value is 20.
     *
     * @return The highlight's tilt angle, in degrees.
     */
    public float getShimmerTilt() {
        return mShimmerMask.tilt;
    }

    /**
     * Set the tile angle of the highlight, in degrees.
     *
     * @param tilt The highlight's tilt angle, in degrees.
     */
    public void setShimmerTilt(float tilt) {
        mShimmerMask.tilt = tilt;
        resetAll();
    }

}
