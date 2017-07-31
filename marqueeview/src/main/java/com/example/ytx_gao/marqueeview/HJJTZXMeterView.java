package com.example.ytx_gao.marqueeview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Gao on 28/07/2017.
 */

public class HJJTZXMeterView extends View {

    private static final String TAG = "HJJTZXMeterView";

    private int meterBgRes, meterPointerRes;

    private Bitmap meterBgImg, pointerImg;

    private float meterCenterXRatio, meterCenterYRatio, pointerCenterXRatio, pointerCenterYRatio;

    private float meterCenterX, meterCenterY, pointerCenterX, pointerCenterY;

    private int currentDegree, targetPointerDegree;

    private int animDuration;

    private float pointerDrawStartX, pointerDrawStartY;

    private float meterBgWHRatio, meterBgScaleRatio;

    private ValueAnimator pointerAnimator;

    private Paint imgPaint;

    private boolean isStartedAnimation = false, isMeasureView = false;

    public HJJTZXMeterView(Context context) {
        this(context, null);
    }

    public HJJTZXMeterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HJJTZXMeterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        useDefaults();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MeterView, 0, 0);
            try {
                if (a.hasValue(R.styleable.MeterView_meter_img)) {
                    setMeterBgRes(a.getResourceId(R.styleable.MeterView_meter_img, R.drawable.bg_hjjt_strong_weak));
                }
                if (a.hasValue(R.styleable.MeterView_meter_centerx_ratio)) {
                    setMeterCenterXRatio(a.getFloat(R.styleable.MeterView_meter_centerx_ratio, 0.5f));
                }
                if (a.hasValue(R.styleable.MeterView_meter_centery_ratio)) {
                    setMeterCenterYRatio(a.getFloat(R.styleable.MeterView_meter_centery_ratio, 0.5f));
                }
                if (a.hasValue(R.styleable.MeterView_pointer_img)) {
                    setMeterPointerRes(a.getResourceId(R.styleable.MeterView_pointer_img, R.drawable.ic_hjjt_pointer));
                }
                if (a.hasValue(R.styleable.MeterView_pointer_centerx_ratio)) {
                    setPointerCenterXRatio(a.getFloat(R.styleable.MeterView_pointer_centerx_ratio, 0.210787f));
                }
                if (a.hasValue(R.styleable.MeterView_pointer_centery_ratio)) {
                    setPointerCenterYRatio(a.getFloat(R.styleable.MeterView_pointer_centery_ratio, 0.49f));
                }
                if (a.hasValue(R.styleable.MeterView_anim_duration)) {
                    setAnimDuration(a.getInt(R.styleable.MeterView_anim_duration, 1000));
                }
                if (a.hasValue(R.styleable.MeterView_meter_img)) {
                    setTargetPointerDegree(a.getInt(R.styleable.MeterView_target_angle, 90));
                }

            } finally {
                a.recycle();
            }
        }
        init();
    }

    private void useDefaults() {
        setMeterBgRes(R.drawable.bg_hjjt_strong_weak);
        setMeterCenterXRatio(0.545f);
        setMeterCenterYRatio(0.8f);
        setMeterPointerRes(R.drawable.ic_hjjt_pointer);
        setPointerCenterXRatio(0.210787f);
        setPointerCenterYRatio(0.49f);
        setTargetPointerDegree(180);
        setAnimDuration(1000);
    }

    private void init() {
        meterBgImg = BitmapFactory.decodeResource(getResources(), getMeterBgRes()).copy(Bitmap.Config.ARGB_8888, true);
        meterBgWHRatio = (float) meterBgImg.getWidth() / meterBgImg.getHeight();
        pointerImg = BitmapFactory.decodeResource(getResources(), getMeterPointerRes()).copy(Bitmap.Config.ARGB_8888, true);
        imgPaint = new Paint();
        imgPaint.setAntiAlias(true);
        imgPaint.setDither(true);
        imgPaint.setFilterBitmap(true);
        initView();
    }

    private void initView() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isMeasureView) {
            meterBgScaleRatio = (float) MeasureSpec.getSize(widthMeasureSpec) / meterBgImg.getWidth();
            if (meterBgScaleRatio <= 0) {
                meterBgScaleRatio = 1;
            }
            Matrix matrio = new Matrix();
            matrio.postScale(meterBgScaleRatio, meterBgScaleRatio);
            meterBgImg = Bitmap.createBitmap(meterBgImg, 0, 0, meterBgImg.getWidth(),
                    meterBgImg.getHeight(), matrio, true);
            pointerImg = Bitmap.createBitmap(pointerImg, 0, 0, pointerImg.getWidth(),
                    pointerImg.getHeight(), matrio, true);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(meterBgImg.getWidth(), MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(meterBgImg.getHeight(), MeasureSpec.EXACTLY);
            measureDrawValue();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void measureDrawValue() {
        meterCenterX = meterBgImg.getWidth() * getMeterCenterXRatio();
        meterCenterY = meterBgImg.getHeight() * getMeterCenterYRatio();
        pointerCenterX = pointerImg.getWidth() * getPointerCenterXRatio();
        pointerCenterY = pointerImg.getHeight() * getPointerCenterYRatio();
        pointerDrawStartX = meterCenterX - pointerCenterX;
        pointerDrawStartY = meterCenterY - pointerCenterY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(meterBgImg, 0, 0, imgPaint);
        canvas.save();
        canvas.rotate(currentDegree, meterCenterX, meterCenterY);
        canvas.drawBitmap(pointerImg, pointerDrawStartX, pointerDrawStartY, imgPaint);
        canvas.restore();
    }

    private void resetAll() {
        stopRotateAnimation();
    }

    public void startRotateAnimation() {
        stopRotateAnimation();
        Animator animator = getPointerAnimator();
        animator.start();
        setStartedAnimation(true);
    }

    private void stopRotateAnimation() {
        if (pointerAnimator != null) {
            pointerAnimator.end();
            pointerAnimator.removeAllListeners();
            pointerAnimator.removeAllUpdateListeners();
            pointerAnimator.cancel();
        }
        pointerAnimator = null;
        setStartedAnimation(false);
    }

    private ValueAnimator getPointerAnimator() {
        if (pointerAnimator != null) {
            return pointerAnimator;
        }
        pointerAnimator = ValueAnimator.ofInt(0, targetPointerDegree);
        pointerAnimator.setDuration(animDuration);
        pointerAnimator.setInterpolator(new OvershootInterpolator());
        pointerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setStartedAnimation(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        pointerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                currentDegree = -value;
                invalidate();
            }
        });
        return pointerAnimator;
    }

    public int getMeterBgRes() {
        return meterBgRes;
    }

    public void setMeterBgRes(int meterBgRes) {
        this.meterBgRes = meterBgRes;
    }

    public int getMeterPointerRes() {
        return meterPointerRes;
    }

    public void setMeterPointerRes(int meterPointerRes) {
        this.meterPointerRes = meterPointerRes;
    }

    public float getMeterCenterXRatio() {
        return meterCenterXRatio;
    }

    public void setMeterCenterXRatio(float meterCenterXRatio) {
        this.meterCenterXRatio = meterCenterXRatio;
    }

    public float getMeterCenterYRatio() {
        return meterCenterYRatio;
    }

    public void setMeterCenterYRatio(float meterCenterYRatio) {
        this.meterCenterYRatio = meterCenterYRatio;
    }

    public float getPointerCenterXRatio() {
        return pointerCenterXRatio;
    }

    public void setPointerCenterXRatio(float pointerCenterXRatio) {
        this.pointerCenterXRatio = pointerCenterXRatio;
    }

    public float getPointerCenterYRatio() {
        return pointerCenterYRatio;
    }

    public void setPointerCenterYRatio(float pointerCenterYRatio) {
        this.pointerCenterYRatio = pointerCenterYRatio;
    }

    public int getCurrentDegree() {
        return currentDegree;
    }

    public void setCurrentDegree(int currentDegree) {
        this.currentDegree = currentDegree;
    }

    public float getTargetPointerDegree() {
        return targetPointerDegree;
    }

    public void setTargetPointerDegree(int targetPointerDegree) {
        this.targetPointerDegree = targetPointerDegree;
        resetAll();
    }

    public boolean isStartedAnimation() {
        return isStartedAnimation;
    }

    private void setStartedAnimation(boolean startedAnimation) {
        isStartedAnimation = startedAnimation;
    }

    public int getAnimDuration() {
        return animDuration;
    }

    public void setAnimDuration(int animDuration) {
        this.animDuration = animDuration;
    }
}
