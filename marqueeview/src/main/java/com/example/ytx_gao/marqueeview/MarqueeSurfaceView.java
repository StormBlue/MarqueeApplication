package com.example.ytx_gao.marqueeview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.concurrent.TimeUnit;

/**
 * Created by Gao-Krund on 2016/7/5.
 */
public class MarqueeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private final static String TAG = "MarqueeSurfaceView";

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

    private LoopThread thread;

    private static final String INSTANCE_STATE = "saved_instance";

    public MarqueeSurfaceView(Context context) {
        this(context, null);
    }

    public MarqueeSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.Marquee);
        initByAttributes(mTypedArray);
        init();
        mTypedArray.recycle();
        // 关闭硬件加速 Xfermode不支持硬件加速
//        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void init() {
        initView();
        SurfaceHolder holder = getHolder();
        holder.addCallback(this); //设置Surface生命周期回调
        thread = new LoopThread(holder, getContext());
    }

    private void initView() {

    }

    protected void initByAttributes(TypedArray attributes) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread.isRunning = true;
        thread.start();

//        // 设置透明
//        setZOrderOnTop(true);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread.isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行绘制的绘制线程
     *
     * @author Administrator
     */
    class LoopThread extends Thread {

        private final SurfaceHolder surfaceHolder;
        private Context context;
        private boolean isRunning;
        private Paint bitmapPaint, paintBg;
        private Bitmap dstBitmap, srcBitmap;
        private Canvas srcCanvas;

        private int totalAmount = 10, currentIndex = 0;

        public LoopThread(SurfaceHolder surfaceHolder, Context context) {

            this.surfaceHolder = surfaceHolder;
            this.context = context;
            isRunning = false;
            bitmapPaint = new Paint();
            bitmapPaint.setAntiAlias(true);
            paintBg = new Paint();
            paintBg.setStyle(Paint.Style.FILL);
            paintBg.setAntiAlias(true);
        }

        @Override
        public void run() {

            Canvas c = null;

//            while (isRunning) {
            for (; currentIndex < totalAmount; currentIndex++)

                synchronized (surfaceHolder) {
                    c = surfaceHolder.lockCanvas(null);
                    drawBackground(c);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    drawPathBitmap(c);

                    surfaceHolder.unlockCanvasAndPost(c);
                }

//            }

        }

        // 绘制有透明区域的背景
        private void drawBackground(Canvas c) {
//            c.drawColor(backgroundColor, PorterDuff.Mode.SRC);
            if (dstBitmap == null || srcBitmap == null) {
                // 绘制背景灰色色块
                dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas dstCanvas = new Canvas(dstBitmap);
                paintBg = new Paint();
                paintBg.setColor(lightGrayColor);
                for (int j = 0; j < lightAmount; j++) {
                    dstCanvas.drawRoundRect(lightRectFs[j], lightRadius, lightRadius, paintBg);
                }

                srcBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                srcCanvas = new Canvas(srcBitmap);
                paintBg.setColor(lightBrightColor);
            }
            drawPathBitmap(c);

        }

        private void drawPathBitmap(Canvas c) {
            bitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            c.drawBitmap(dstBitmap, 0, 0, bitmapPaint);
            bitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            srcCanvas.drawRect(new RectF(width * 0.55f, lightDValueTop, width, lightDValueTop + lightWidth), paintBg);
            c.drawBitmap(srcBitmap, 0, 0, bitmapPaint);

//            c.drawRect(new RectF(width * (1 - (float) currentIndex / totalAmount), lightDValueTop, width, lightDValueTop + lightWidth), bitmapPaint);
        }

    }
}