package com.example.ytx_gao.marqueeapplication.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.ytx_gao.marqueeapplication.R;
import com.example.ytx_gao.marqueeview.ShimmerMarqueeView;

/**
 * Created by ytx_gao on 27/07/2017.
 */

public class HJJTZXIndicatorView extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = "HJJTZXIndicatorView";

    private static final int DEFAULT_MASK_COLOR = Color.argb(127, 0, 0, 0);

    private RelativeLayout mainContent, shimmerMarqueeContainer;

    private LinearLayout indicatorContainer;

    private ShimmerMarqueeView shimmerMarqueeView;

    private ImageView hjjtzxStatusImg, contentArrowImg;

    private int indicatorContainerHeight;

    private boolean isExpanded = false;

    private ObjectAnimator expandMarqueeContainerAnimator, expandIndicatorContainerAnimator, collapseMarqueeContainerAnimator, collapseIndicatorContainerAnimator;

    private int animationDuration = 300;

    public HJJTZXIndicatorView(Context context) {
        this(context, null);
    }

    public HJJTZXIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HJJTZXIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_hjjtzx_indicator, this, true);
        init();
    }

    private void init() {
        initView();
        mainContent.setOnClickListener(this);
        shimmerMarqueeContainer.setOnClickListener(this);
        indicatorContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                indicatorContainerHeight = indicatorContainer.getHeight();
                indicatorContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                indicatorContainer.setTranslationY(indicatorContainerHeight);
                shimmerMarqueeContainer.setTranslationY(indicatorContainerHeight);
            }
        });
    }

    private void initView() {
        mainContent = (RelativeLayout) findViewById(R.id.rl_main_content);
        shimmerMarqueeContainer = (RelativeLayout) findViewById(R.id.rl_marquee_contianer);
        shimmerMarqueeView = (ShimmerMarqueeView) findViewById(R.id.shimmer_marquee_view);
        indicatorContainer = (LinearLayout) findViewById(R.id.ll_indicator_container);
        hjjtzxStatusImg = (ImageView) findViewById(R.id.img_hjjtzx_status);
        contentArrowImg = (ImageView) findViewById(R.id.img_hjjtzx_dialog_arrow);
    }

    public void setHJJTData(int[] targetColors) {
        shimmerMarqueeView.setLightTargetColors(targetColors);
        shimmerMarqueeView.startAnimations();
    }

    private void expandIndicatorView() {
        if (expandMarqueeContainerAnimator == null) {
            expandMarqueeContainerAnimator = ObjectAnimator
                    .ofFloat(shimmerMarqueeContainer, "translationY", indicatorContainerHeight, 0)
                    .setDuration(animationDuration);
        }
        if (expandIndicatorContainerAnimator == null) {
            expandIndicatorContainerAnimator = ObjectAnimator
                    .ofFloat(indicatorContainer, "translationY", indicatorContainerHeight, 0)
                    .setDuration(animationDuration);
            expandIndicatorContainerAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mainContent.setClickable(true);
                    mainContent.setBackgroundColor(DEFAULT_MASK_COLOR);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isExpanded = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        expandMarqueeContainerAnimator.start();
        expandIndicatorContainerAnimator.start();
    }

    private void collapseIndicatorView() {
        if (collapseMarqueeContainerAnimator == null) {
            collapseMarqueeContainerAnimator = ObjectAnimator
                    .ofFloat(shimmerMarqueeContainer, "translationY", 0, indicatorContainerHeight)
                    .setDuration(animationDuration);
        }
        if (collapseIndicatorContainerAnimator == null) {
            collapseIndicatorContainerAnimator = ObjectAnimator
                    .ofFloat(indicatorContainer, "translationY", 0, indicatorContainerHeight)
                    .setDuration(animationDuration);
            collapseIndicatorContainerAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mainContent.setClickable(false);
                    mainContent.setBackgroundColor(Color.TRANSPARENT);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isExpanded = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        collapseMarqueeContainerAnimator.start();
        collapseIndicatorContainerAnimator.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_main_content:
                if (isExpanded) {
                    collapseIndicatorView();
                }
                break;
            case R.id.rl_marquee_contianer:
                if (isExpanded) {
                    collapseIndicatorView();
                } else {
                    expandIndicatorView();
                }
                break;
            default:
                break;
        }
    }
}
