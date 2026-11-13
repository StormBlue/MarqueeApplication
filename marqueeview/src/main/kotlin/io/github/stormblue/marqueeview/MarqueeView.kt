package io.github.stormblue.marqueeview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/** Segmented marquee lights. All mutations must run on the main thread; dimensions are pixels. */
class MarqueeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var targets = FloatArray(16)
    private var displayed = FloatArray(16)
    private var origins = FloatArray(16)
    private var animator: ValueAnimator? = null
    private var shimmerPhase = 0f
    private var shimmering = false
    private var gradients: Array<LinearGradient>? = null
    private var buildingGradients = false
    private var gradientWidth = -1f
    private var gradientHeight = -1f
    private var gradientLeft = -1
    private var gradientTop = -1

    var segmentCount: Int = 16
        set(value) {
            require(value in 1..256) { "segmentCount must be in 1..256" }
            if (field == value) return
            cancelAnimation()
            field = value
            targets = FloatArray(value)
            displayed = FloatArray(value)
            origins = FloatArray(value)
            invalidateGradients()
            invalidate()
            notifyProgressChanged()
        }
    var segmentSpacing: Float = 3f * resources.displayMetrics.density
        set(value) { requireDimension(value, "segmentSpacing"); if (field == value) return; field = value; invalidateGradients(); invalidate() }
    var cornerRadius: Float = 3f * resources.displayMetrics.density
        set(value) { requireDimension(value, "cornerRadius"); field = value; invalidate() }
    var lightColor: Int = Color.rgb(0, 245, 170)
        set(value) { field = value; invalidate() }
    var trackColor: Int = Color.rgb(13, 67, 47)
        set(value) { field = value; invalidate() }
    var shimmerColor: Int = Color.WHITE
        set(value) { field = value; invalidate() }
    var gradientEnabled: Boolean = false
        set(value) { if (field != value) { field = value; invalidateGradients(); invalidate() } }
    var gradientStartColor: Int = Color.rgb(88, 247, 176)
        set(value) { if (field != value) { field = value; invalidateGradients(); invalidate() } }
    var gradientEndColor: Int = Color.rgb(82, 227, 163)
        set(value) { if (field != value) { field = value; invalidateGradients(); invalidate() } }
    /** 0 degrees points right, 90 down; 45 produces a top-left to bottom-right gradient. */
    var gradientAngle: Float = 45f
        set(value) {
            require(value.isFinite()) { "gradientAngle must be finite" }
            val normalized = ((value % 360f) + 360f) % 360f
            if (field == normalized) return
            field = normalized
            invalidateGradients()
            invalidate()
        }
    /** Half-width of the highlight, relative to the complete strip (0, 1]. */
    var shimmerWidth: Float = 0.3f
        set(value) {
            require(value.isFinite() && value > 0f && value <= 1f) { "shimmerWidth must be in (0, 1]" }
            field = value
            invalidate()
        }
    var shimmerDuration: Long = 1600L
        set(value) {
            require(value > 0L) { "shimmerDuration must be > 0" }
            if (field == value) return
            field = value
            if (shimmering) { cancelAnimation(); updateAnimation() }
        }
    var revealDuration: Long = 400L
        set(value) { require(value >= 0L) { "revealDuration must be >= 0" }; field = value }
    /** Logical leading-to-trailing direction follows RTL; true reverses it. */
    var reverseDirection: Boolean = false
        set(value) { field = value; invalidate() }
    /** Requested playback state; retained while hidden or detached. */
    var isStarted: Boolean = false
        private set
    /** Whether a reveal or shimmer animator is actually running. */
    val isRunning: Boolean get() = animator?.isRunning == true
    /** Defensive copy in logical leading-to-trailing order, with values in [0, 1]. */
    val intensities: FloatArray get() = targets.copyOf()
    /** Mean target intensity; assigning fills lights in logical order. */
    var progress: Float
        get() = targets.sum() / segmentCount
        set(value) { setProgress(value, false) }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        val a = context.obtainStyledAttributes(attrs, R.styleable.MarqueeView, defStyleAttr, 0)
        try {
            segmentCount = a.getInt(R.styleable.MarqueeView_marqueeSegmentCount, segmentCount)
            segmentSpacing = a.getDimension(R.styleable.MarqueeView_marqueeSegmentSpacing, segmentSpacing)
            cornerRadius = a.getDimension(R.styleable.MarqueeView_marqueeCornerRadius, cornerRadius)
            lightColor = a.getColor(R.styleable.MarqueeView_marqueeLightColor, lightColor)
            trackColor = a.getColor(R.styleable.MarqueeView_marqueeTrackColor, trackColor)
            shimmerColor = a.getColor(R.styleable.MarqueeView_marqueeShimmerColor, shimmerColor)
            shimmerWidth = a.getFloat(R.styleable.MarqueeView_marqueeShimmerWidth, shimmerWidth)
            shimmerDuration = a.getInt(R.styleable.MarqueeView_marqueeShimmerDuration, 1600).toLong()
            revealDuration = a.getInt(R.styleable.MarqueeView_marqueeRevealDuration, 400).toLong()
            reverseDirection = a.getBoolean(R.styleable.MarqueeView_marqueeReverseDirection, false)
            gradientStartColor = a.getColor(R.styleable.MarqueeView_marqueeGradientStartColor, gradientStartColor)
            gradientEndColor = a.getColor(R.styleable.MarqueeView_marqueeGradientEndColor, gradientEndColor)
            gradientAngle = a.getFloat(R.styleable.MarqueeView_marqueeGradientAngle, gradientAngle)
            gradientEnabled = a.getBoolean(R.styleable.MarqueeView_marqueeGradientEnabled, false)
            progress = a.getFloat(R.styleable.MarqueeView_marqueeProgress, 0f)
            if (a.getBoolean(R.styleable.MarqueeView_marqueeAutoStart, false)) start()
        } finally { a.recycle() }
    }

    /** Finite values are clamped to [0, 1]; a partial final light uses fractional intensity. */
    fun setProgress(value: Float, animated: Boolean) {
        require(value.isFinite()) { "progress must be finite" }
        val lit = value.coerceIn(0f, 1f) * segmentCount
        setIntensities(FloatArray(segmentCount) { (lit - it).coerceIn(0f, 1f) }, animated)
    }

    /** Copies the input. Animated changes reveal each light sequentially from its current value. */
    @JvmOverloads
    fun setIntensities(values: FloatArray, animated: Boolean = false) {
        require(values.size == segmentCount) { "intensities size must equal segmentCount" }
        require(values.all { it.isFinite() && it in 0f..1f }) { "intensities must be in [0, 1]" }
        if (targets.contentEquals(values) && (shimmering || !isRunning || animated)) return
        cancelAnimation()
        displayed.copyInto(origins)
        values.copyInto(targets)
        notifyProgressChanged()
        if (!animated || revealDuration == 0L || !canAnimate()) {
            targets.copyInto(displayed)
            invalidate()
            updateAnimation()
            return
        }
        animator = ValueAnimator.ofFloat(0f, segmentCount.toFloat()).apply {
            duration = revealDuration
            interpolator = LinearInterpolator()
            addUpdateListener {
                val cursor = it.animatedValue as Float
                for (i in 0 until segmentCount) {
                    displayed[i] = origins[i] + (targets[i] - origins[i]) * (cursor - i).coerceIn(0f, 1f)
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animator = null
                    targets.copyInto(displayed)
                    updateAnimation()
                }
            })
            start()
        }
    }

    fun start() { isStarted = true; updateAnimation() }

    /** Stops all animation and shows target intensities without a highlight. */
    fun stop() {
        isStarted = false
        cancelAnimation()
        targets.copyInto(displayed)
        invalidate()
    }

    private fun canAnimate(): Boolean {
        if (!isAttachedToWindow || !isShown || windowVisibility != VISIBLE || isInEditMode) return false
        return if (Build.VERSION.SDK_INT >= 26) ValueAnimator.areAnimatorsEnabled()
        else Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }

    private fun cancelAnimation() {
        animator?.run { removeAllListeners(); removeAllUpdateListeners(); cancel() }
        animator = null
        shimmering = false
    }

    private fun updateAnimation() {
        if (!canAnimate()) {
            cancelAnimation()
            targets.copyInto(displayed)
            invalidate()
            return
        }
        if (isRunning || !isStarted || targets.none { it > 0f }) return
        shimmering = true
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = shimmerDuration
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { shimmerPhase = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); updateAnimation() }
    override fun onDetachedFromWindow() {
        cancelAnimation()
        targets.copyInto(displayed)
        super.onDetachedFromWindow()
    }
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isAttachedToWindow) updateAnimation()
    }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (isAttachedToWindow) updateAnimation()
    }
    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        rebuildGradients()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        setMeasuredDimension(
            resolveSize(maxOf(suggestedMinimumWidth, (240 * density).roundToInt() + paddingLeft + paddingRight), widthMeasureSpec),
            resolveSize(maxOf(suggestedMinimumHeight, (12 * density).roundToInt() + paddingTop + paddingBottom), heightMeasureSpec),
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildGradients()
    }
    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        rebuildGradients()
    }
    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        rebuildGradients()
    }

    // Called only on geometry/style changes, never from drawing or animation updates.
    private fun invalidateGradients() {
        gradients = null
        rebuildGradients()
    }

    private fun rebuildGradients() {
        if (!gradientEnabled) { gradients = null; return }
        // Resolving relative padding may invoke onRtlPropertiesChanged recursively.
        if (buildingGradients) return
        buildingGradients = true
        try {
            val left = paddingLeft
            val top = paddingTop
            val w = (width - left - paddingRight).coerceAtLeast(0).toFloat()
            val h = (height - top - paddingBottom).coerceAtLeast(0).toFloat()
            if (gradients != null && w == gradientWidth && h == gradientHeight &&
                left == gradientLeft && top == gradientTop) return
            val gap = if (segmentCount == 1) 0f else min(segmentSpacing, w / (segmentCount - 1))
            val segmentWidth = (w - gap * (segmentCount - 1)) / segmentCount
            if (segmentWidth <= 0f || h <= 0f) { gradients = null; return }
            val radians = Math.toRadians(gradientAngle.toDouble())
            val dx = cos(radians).toFloat()
            val dy = sin(radians).toFloat()
            val radius = (abs(segmentWidth * dx) + abs(h * dy)) / 2f
            val cy = top + h / 2f
            gradients = Array(segmentCount) { physical ->
                val cx = left + physical * (segmentWidth + gap) + segmentWidth / 2f
                LinearGradient(cx - radius * dx, cy - radius * dy, cx + radius * dx, cy + radius * dy,
                    gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
            }
            gradientWidth = w; gradientHeight = h; gradientLeft = left; gradientTop = top
        } finally { buildingGradients = false }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = (width - paddingLeft - paddingRight).coerceAtLeast(0).toFloat()
        val h = (height - paddingTop - paddingBottom).coerceAtLeast(0).toFloat()
        val gap = if (segmentCount == 1) 0f else min(segmentSpacing, w / (segmentCount - 1))
        val segmentWidth = (w - gap * (segmentCount - 1)) / segmentCount
        if (segmentWidth <= 0f || h <= 0f) return
        val radius = min(cornerRadius, min(segmentWidth, h) / 2f)
        val reversed = (layoutDirection == LAYOUT_DIRECTION_RTL) xor reverseDirection
        val highlight = shimmerPhase * (1f + 2f * shimmerWidth) - shimmerWidth
        val shaders = gradients
        for (i in 0 until segmentCount) {
            val intensity = displayed[i]
            val physical = if (reversed) segmentCount - 1 - i else i
            val left = paddingLeft + physical * (segmentWidth + gap)
            val strength = if (shimmering && intensity > 0f)
                (1f - abs((i + 0.5f) / segmentCount - highlight) / shimmerWidth).coerceAtLeast(0f) * intensity * 0.7f
                else 0f
            paint.shader = null
            if (shaders == null) {
                paint.color = blend(blend(trackColor, lightColor, intensity), shimmerColor, strength)
                canvas.drawRoundRect(left, paddingTop.toFloat(), left + segmentWidth, paddingTop + h, radius, radius, paint)
            } else {
                paint.color = trackColor
                canvas.drawRoundRect(left, paddingTop.toFloat(), left + segmentWidth, paddingTop + h, radius, radius, paint)
                if (intensity > 0f) {
                    paint.shader = shaders[physical]
                    paint.alpha = (intensity * 255f).roundToInt()
                    canvas.drawRoundRect(left, paddingTop.toFloat(), left + segmentWidth, paddingTop + h, radius, radius, paint)
                    paint.shader = null
                }
                if (strength > 0f) {
                    paint.color = shimmerColor
                    paint.alpha = (Color.alpha(shimmerColor) * strength).roundToInt()
                    canvas.drawRoundRect(left, paddingTop.toFloat(), left + segmentWidth, paddingTop + h, radius, radius, paint)
                }
            }
        }
        paint.shader = null
    }

    private fun blend(from: Int, to: Int, fraction: Float): Int {
        val a = Color.alpha(from) + ((Color.alpha(to) - Color.alpha(from)) * fraction).roundToInt()
        val r = Color.red(from) + ((Color.red(to) - Color.red(from)) * fraction).roundToInt()
        val g = Color.green(from) + ((Color.green(to) - Color.green(from)) * fraction).roundToInt()
        val b = Color.blue(from) + ((Color.blue(to) - Color.blue(from)) * fraction).roundToInt()
        return Color.argb(a, r, g, b)
    }

    private fun requireDimension(value: Float, name: String) {
        require(value.isFinite() && value >= 0f) { "$name must be finite and >= 0" }
    }
    private fun notifyProgressChanged() = sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    override fun getAccessibilityClassName(): CharSequence = ProgressBar::class.java.name
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = accessibilityClassName
        info.rangeInfo = AccessibilityNodeInfo.RangeInfo.obtain(
            AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT, 0f, 1f, progress)
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable("super", super.onSaveInstanceState())
        putInt("count", segmentCount); putFloatArray("targets", targets.copyOf())
        putFloat("spacing", segmentSpacing); putFloat("radius", cornerRadius)
        putInt("light", lightColor); putInt("track", trackColor); putInt("shimmer", shimmerColor)
        putFloat("shimmerWidth", shimmerWidth); putLong("shimmerDuration", shimmerDuration)
        putLong("revealDuration", revealDuration); putBoolean("reverse", reverseDirection)
        putBoolean("gradientEnabled", gradientEnabled); putInt("gradientStart", gradientStartColor)
        putInt("gradientEnd", gradientEndColor); putFloat("gradientAngle", gradientAngle)
        putBoolean("started", isStarted)
    }
    @Suppress("DEPRECATION")
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) { super.onRestoreInstanceState(state); return }
        super.onRestoreInstanceState(state.getParcelable("super"))
        stop()
        segmentCount = state.getInt("count", 16)
        segmentSpacing = state.getFloat("spacing", segmentSpacing); cornerRadius = state.getFloat("radius", cornerRadius)
        lightColor = state.getInt("light", lightColor); trackColor = state.getInt("track", trackColor)
        shimmerColor = state.getInt("shimmer", shimmerColor); shimmerWidth = state.getFloat("shimmerWidth", shimmerWidth)
        shimmerDuration = state.getLong("shimmerDuration", shimmerDuration)
        revealDuration = state.getLong("revealDuration", revealDuration); reverseDirection = state.getBoolean("reverse")
        gradientStartColor = state.getInt("gradientStart", gradientStartColor)
        gradientEndColor = state.getInt("gradientEnd", gradientEndColor)
        gradientAngle = state.getFloat("gradientAngle", gradientAngle)
        gradientEnabled = state.getBoolean("gradientEnabled")
        state.getFloatArray("targets")?.let { setIntensities(it) }
        if (state.getBoolean("started")) start()
    }
}
