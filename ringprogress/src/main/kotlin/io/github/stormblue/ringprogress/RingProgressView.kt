package io.github.stormblue.ringprogress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A hardware accelerated ring. Values use [0, max]; dimensions in the Kotlin API are pixels.
 * Configure and update on the UI thread. Animation is opt-in via [setProgress].
 */
class RingProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bounds = RectF()
    private var displayedProgress = 0f
    private var rotation = 0f
    private var animator: ValueAnimator? = null

    var max: Float = 100f
        set(value) {
            require(value.isFinite() && value > 0f) { "max must be finite and > 0" }
            if (field == value) return
            field = value
            progress = progress.coerceAtMost(value)
            notifyViewAccessibilityStateChangedIfNeeded()
            invalidate()
        }

    var progress: Float = 0f
        set(value) {
            require(value.isFinite()) { "progress must be finite" }
            val next = value.coerceIn(0f, max)
            val changed = field != next
            val drawingChanged = displayedProgress != next
            field = next
            if (!isIndeterminate) cancelAnimator()
            displayedProgress = next
            if (changed || drawingChanged) invalidate()
            if (changed) notifyViewAccessibilityStateChangedIfNeeded()
        }

    var strokeWidth: Float = 8f * resources.displayMetrics.density
        set(value) {
            require(value.isFinite() && value > 0f) { "strokeWidth must be finite and > 0" }
            if (field == value) return
            field = value
            invalidate()
        }
    var progressColor: Int = Color.rgb(0, 137, 123)
        set(value) { if (field != value) { field = value; invalidate() } }
    var trackColor: Int = Color.argb(40, 0, 137, 123)
        set(value) { if (field != value) { field = value; invalidate() } }
    var startAngle: Float = -90f
        set(value) {
            require(value.isFinite()) { "startAngle must be finite" }
            field = value % 360f
            invalidate()
        }
    var clockwise: Boolean = true
        set(value) { field = value; invalidate() }
    var roundedCaps: Boolean = true
        set(value) { field = value; invalidate() }
    var animationDuration: Long = 300L
        set(value) {
            require(value >= 0) { "animationDuration must be >= 0" }
            field = value
        }
    var isIndeterminate: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            cancelAnimator()
            displayedProgress = progress
            updateIndeterminateAnimation()
            invalidate()
            notifyViewAccessibilityStateChangedIfNeeded()
        }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        val a = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView, defStyleAttr, 0)
        try {
            max = a.getFloat(R.styleable.RingProgressView_ringMax, max)
            progress = a.getFloat(R.styleable.RingProgressView_ringProgress, progress)
            strokeWidth = a.getDimension(R.styleable.RingProgressView_ringStrokeWidth, strokeWidth)
            progressColor = a.getColor(R.styleable.RingProgressView_ringProgressColor, progressColor)
            trackColor = a.getColor(R.styleable.RingProgressView_ringTrackColor, trackColor)
            startAngle = a.getFloat(R.styleable.RingProgressView_ringStartAngle, startAngle)
            clockwise = a.getBoolean(R.styleable.RingProgressView_ringClockwise, clockwise)
            roundedCaps = a.getBoolean(R.styleable.RingProgressView_ringRoundedCaps, roundedCaps)
            animationDuration = a.getInt(R.styleable.RingProgressView_ringAnimationDuration, 300).toLong()
            isIndeterminate = a.getBoolean(R.styleable.RingProgressView_ringIndeterminate, false)
        } finally {
            a.recycle()
        }
    }

    /** Cancels any previous transition; a new transition starts at the currently drawn value. */
    fun setProgress(value: Float, animated: Boolean = false) {
        require(value.isFinite()) { "progress must be finite" }
        val from = displayedProgress
        progress = value
        if (!animated || isIndeterminate || animationDuration == 0L || !canAnimate() || from == progress) {
            invalidate()
            return
        }
        displayedProgress = from
        animator = ValueAnimator.ofFloat(from, progress).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                displayedProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun canAnimate() = isAttachedToWindow && isShown && windowVisibility == VISIBLE &&
        !isInEditMode && (android.os.Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled())

    private fun cancelAnimator() {
        animator?.run { cancel(); removeAllUpdateListeners() }
        animator = null
    }

    private fun updateIndeterminateAnimation() {
        if (!isIndeterminate || !canAnimate()) {
            cancelAnimator()
            displayedProgress = progress
            return
        }
        if (animator?.isRunning == true) return
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { rotation = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); updateIndeterminateAnimation() }
    override fun onDetachedFromWindow() { cancelAnimator(); super.onDetachedFromWindow() }
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // View's constructor can invoke this before Kotlin properties have initialized.
        if (isAttachedToWindow) updateIndeterminateAnimation()
    }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (isAttachedToWindow) updateIndeterminateAnimation()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val diameter = (96 * resources.displayMetrics.density).roundToInt()
        setMeasuredDimension(
            resolveSize(maxOf(suggestedMinimumWidth, diameter + paddingLeft + paddingRight), widthMeasureSpec),
            resolveSize(maxOf(suggestedMinimumHeight, diameter + paddingTop + paddingBottom), heightMeasureSpec),
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = (width - paddingLeft - paddingRight).coerceAtLeast(0).toFloat()
        val h = (height - paddingTop - paddingBottom).coerceAtLeast(0).toFloat()
        val diameter = min(w, h)
        if (diameter <= 0f) return
        val stroke = min(strokeWidth, diameter / 2f)
        val left = paddingLeft + (w - diameter) / 2f + stroke / 2f
        val top = paddingTop + (h - diameter) / 2f + stroke / 2f
        bounds.set(left, top, left + diameter - stroke, top + diameter - stroke)
        paint.strokeWidth = stroke
        paint.strokeCap = Paint.Cap.BUTT
        paint.color = trackColor
        canvas.drawOval(bounds, paint)
        val sweep = if (isIndeterminate) 90f else 360f * (displayedProgress / max)
        if (sweep <= 0f) return
        paint.color = progressColor
        paint.strokeCap = if (roundedCaps) Paint.Cap.ROUND else Paint.Cap.BUTT
        canvas.drawArc(bounds, startAngle + (if (isIndeterminate) rotation else 0f) * (if (clockwise) 1 else -1),
            sweep * (if (clockwise) 1 else -1), false, paint)
    }

    override fun getAccessibilityClassName(): CharSequence = ProgressBar::class.java.name

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = accessibilityClassName
        if (isIndeterminate) {
            if (android.os.Build.VERSION.SDK_INT >= 30) info.stateDescription = context.getString(R.string.ring_progress_indeterminate)
        } else {
            info.rangeInfo = AccessibilityNodeInfo.RangeInfo.obtain(
                AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT, 0f, max, progress)
        }
    }

    private fun notifyViewAccessibilityStateChangedIfNeeded() {
        // Notify on target changes only, never on every animation frame.
        sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable("super", super.onSaveInstanceState())
        putFloat("max", max); putFloat("progress", progress)
        putFloat("stroke", strokeWidth); putFloat("angle", startAngle)
        putInt("color", progressColor); putInt("track", trackColor)
        putBoolean("clockwise", clockwise); putBoolean("rounded", roundedCaps)
        putBoolean("indeterminate", isIndeterminate); putLong("duration", animationDuration)
    }

    @Suppress("DEPRECATION")
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) { super.onRestoreInstanceState(state); return }
        super.onRestoreInstanceState(state.getParcelable("super"))
        max = state.getFloat("max", 100f); progress = state.getFloat("progress")
        strokeWidth = state.getFloat("stroke", strokeWidth); startAngle = state.getFloat("angle", -90f)
        progressColor = state.getInt("color", progressColor); trackColor = state.getInt("track", trackColor)
        clockwise = state.getBoolean("clockwise", true); roundedCaps = state.getBoolean("rounded", true)
        animationDuration = state.getLong("duration", 300L)
        isIndeterminate = state.getBoolean("indeterminate")
    }
}
