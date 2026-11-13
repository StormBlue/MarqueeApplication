package io.github.stormblue.marqueeview

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.provider.Settings
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class MarqueeViewTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private fun onMain(block: () -> Unit) = instrumentation.runOnMainSync(block)
    private fun view() = MarqueeView(instrumentation.targetContext)
    private fun render(view: MarqueeView, w: Int = 100, h: Int = 20): Bitmap {
        view.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, w, h)
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
    }
    private fun withHost(block: (TestActivity) -> Unit) {
        val activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, TestActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as TestActivity
        try { instrumentation.waitForIdleSync(); onMain { block(activity) } }
        finally { onMain { activity.finish() } }
    }
    private fun animator(view: MarqueeView) = MarqueeView::class.java.getDeclaredField("animator")
        .apply { isAccessible = true }.get(view) as ValueAnimator

    @Test fun progressClampsAndFillsPartialLight() = onMain {
        val view = view().apply { segmentCount = 4 }
        view.progress = 0.375f
        assertArrayEquals(floatArrayOf(1f, 0.5f, 0f, 0f), view.intensities, 0f)
        assertEquals(0.375f, view.progress)
        view.progress = -1f; assertEquals(0f, view.progress)
        view.progress = 2f; assertEquals(1f, view.progress)
    }

    @Test fun invalidInputsAreRejectedWithoutChangingData() = onMain {
        val view = view().apply { progress = 0.5f }
        val expected = view.intensities
        for (invalid in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { view.progress = invalid }
        }
        assertThrows(IllegalArgumentException::class.java) { view.segmentCount = 0 }
        assertThrows(IllegalArgumentException::class.java) { view.segmentCount = 257 }
        assertThrows(IllegalArgumentException::class.java) { view.segmentSpacing = -1f }
        assertThrows(IllegalArgumentException::class.java) { view.cornerRadius = Float.NaN }
        assertThrows(IllegalArgumentException::class.java) { view.gradientAngle = Float.POSITIVE_INFINITY }
        assertThrows(IllegalArgumentException::class.java) { view.shimmerWidth = 0f }
        assertThrows(IllegalArgumentException::class.java) { view.shimmerWidth = 1.1f }
        assertThrows(IllegalArgumentException::class.java) { view.shimmerDuration = 0 }
        assertThrows(IllegalArgumentException::class.java) { view.revealDuration = -1 }
        assertThrows(IllegalArgumentException::class.java) { view.setIntensities(floatArrayOf(1f)) }
        assertThrows(IllegalArgumentException::class.java) { view.setIntensities(FloatArray(16) { Float.NaN }) }
        assertThrows(IllegalArgumentException::class.java) { view.setIntensities(FloatArray(16) { 1.1f }) }
        assertArrayEquals(expected, view.intensities, 0f)
    }

    @Test fun intensityArraysAreDefensivelyCopied() = onMain {
        val view = view().apply { segmentCount = 2 }
        val input = floatArrayOf(1f, 0.3f)
        view.setIntensities(input)
        input[0] = 0f
        val output = view.intensities; output[1] = 0f
        assertArrayEquals(floatArrayOf(1f, 0.3f), view.intensities, 0f)
    }

    @Test fun pixelsMatchSpacingIntensitiesDirectionAndPadding() = onMain {
        val view = view().apply {
            segmentCount = 4; segmentSpacing = 4f; cornerRadius = 0f
            lightColor = Color.RED; trackColor = Color.BLUE
            setIntensities(floatArrayOf(1f, 0.5f, 0f, 0f))
        }
        var bitmap = render(view)
        assertEquals(Color.RED, bitmap.getPixel(10, 10))
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(24, 10))
        assertEquals(Color.BLUE, bitmap.getPixel(60, 10))
        val partial = bitmap.getPixel(35, 10)
        assertTrue(Color.red(partial) in 127..128 && Color.blue(partial) in 127..128)
        view.reverseDirection = true
        bitmap = render(view)
        assertEquals(Color.BLUE, bitmap.getPixel(10, 10))
        assertEquals(Color.RED, bitmap.getPixel(90, 10))
        view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        bitmap = render(view)
        assertEquals(Color.RED, bitmap.getPixel(10, 10))
        view.setPadding(10, 4, 10, 4)
        bitmap = render(view)
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(1, 10))
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(15, 1))
        assertEquals(Color.RED, bitmap.getPixel(15, 10))
        view.segmentSpacing = 1000f
        assertEquals(Color.TRANSPARENT, render(view).getPixel(50, 10))
        view.segmentCount = 1; view.progress = 1f
        assertEquals(Color.RED, render(view).getPixel(50, 10))
    }

    @Test fun eachSegmentHasItsOwnDiagonalGradientAndAngle() = onMain {
        val view = view().apply {
            segmentCount = 2; segmentSpacing = 4f; cornerRadius = 0f; progress = 1f
            gradientStartColor = Color.RED; gradientEndColor = Color.BLUE
            gradientAngle = 45f; gradientEnabled = true
        }
        var bitmap = render(view, 84, 40)
        val topLeft = bitmap.getPixel(3, 3)
        val bottomRight = bitmap.getPixel(36, 36)
        assertTrue(Color.red(topLeft) > Color.blue(topLeft))
        assertTrue(Color.blue(bottomRight) > Color.red(bottomRight))
        assertEquals(topLeft, bitmap.getPixel(47, 3))
        assertEquals(bottomRight, bitmap.getPixel(80, 36))
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(42, 20))
        view.gradientAngle = 135f
        bitmap = render(view, 84, 40)
        assertTrue(Color.red(bitmap.getPixel(36, 3)) > Color.blue(bitmap.getPixel(36, 3)))
        assertTrue(Color.blue(bitmap.getPixel(3, 36)) > Color.red(bitmap.getPixel(3, 36)))
        view.gradientAngle = -225f
        assertEquals(135f, view.gradientAngle)
        view.setPaddingRelative(8, 2, 0, 2)
        view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        bitmap = render(view, 84, 40)
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(82, 20))
        assertNotEquals(Color.TRANSPARENT, bitmap.getPixel(2, 20))
    }

    @Test fun gradientShadersAreReusedAcrossAnimationFramesAndRebuiltForGeometry() = withHost { host ->
        val view = host.marquee
        view.segmentCount = 2; view.segmentSpacing = 4f; view.gradientEnabled = true; view.progress = 1f
        render(view, 84, 40)
        val field = MarqueeView::class.java.getDeclaredField("gradients").apply { isAccessible = true }
        val shaders = field.get(view)
        assertNotNull(shaders)
        view.start()
        repeat(10) {
            animator(view).currentPlayTime = it * 40L
            render(view, 84, 40)
            view.segmentSpacing = view.segmentSpacing
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
            view.setPaddingRelative(view.paddingStart, view.paddingTop, view.paddingEnd, view.paddingBottom)
            assertSame("No shader recreation per frame or identical geometry assignment", shaders, field.get(view))
        }
        view.setPadding(4, 2, 4, 2)
        assertNotSame(shaders, field.get(view))
        val padded = field.get(view)
        render(view, 100, 40)
        assertNotSame(padded, field.get(view))
        val resized = field.get(view)
        view.gradientStartColor = Color.RED
        assertNotSame(resized, field.get(view))
        view.gradientEnabled = false
        assertNull(field.get(view))
    }

    @Test fun measurementRespectsConstraintsAndResize() = onMain {
        val view = view()
        view.measure(View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(10, View.MeasureSpec.AT_MOST))
        assertEquals(50, view.measuredWidth); assertEquals(10, view.measuredHeight)
        view.measure(0, 0)
        assertTrue(view.measuredWidth > view.measuredHeight)
        view.segmentSpacing = 0f; view.progress = 1f; view.cornerRadius = 0f
        assertEquals(view.lightColor, render(view, 80, 40).getPixel(79, 39))
        assertEquals(view.lightColor, render(view, 160, 12).getPixel(159, 11))
    }

    @Test fun savedStateIsSnapshotAndRestoresStyleAndPlaybackIntent() = onMain {
        val view = view().apply {
            id = 42; segmentCount = 3; setIntensities(floatArrayOf(1f, 0.5f, 0f))
            segmentSpacing = 2f; cornerRadius = 1f; reverseDirection = true
            lightColor = Color.RED; trackColor = Color.BLUE; shimmerColor = Color.YELLOW
            shimmerWidth = 0.4f; shimmerDuration = 900L; revealDuration = 200L
            gradientEnabled = true; gradientStartColor = Color.GREEN; gradientEndColor = Color.CYAN
            gradientAngle = 135f; start()
        }
        val state = SparseArray<android.os.Parcelable>()
        view.saveHierarchyState(state)
        view.progress = 0f; view.stop()
        val restored = view().apply { id = 42; restoreHierarchyState(state) }
        assertArrayEquals(floatArrayOf(1f, 0.5f, 0f), restored.intensities, 0f)
        assertEquals(3, restored.segmentCount); assertEquals(2f, restored.segmentSpacing)
        assertEquals(1f, restored.cornerRadius); assertTrue(restored.reverseDirection)
        assertEquals(Color.RED, restored.lightColor); assertEquals(Color.BLUE, restored.trackColor)
        assertEquals(Color.YELLOW, restored.shimmerColor); assertEquals(0.4f, restored.shimmerWidth)
        assertEquals(900L, restored.shimmerDuration); assertEquals(200L, restored.revealDuration)
        assertTrue(restored.isStarted); assertFalse(restored.isRunning)
        assertTrue(restored.gradientEnabled); assertEquals(Color.GREEN, restored.gradientStartColor)
        assertEquals(Color.CYAN, restored.gradientEndColor); assertEquals(135f, restored.gradientAngle)
    }

    @Test fun accessibilityReportsMeanTargetIntensity() = onMain {
        val view = view().apply { segmentCount = 2; setIntensities(floatArrayOf(1f, 0.5f)) }
        val info = AccessibilityNodeInfo.obtain()
        view.onInitializeAccessibilityNodeInfo(info)
        assertEquals("android.widget.ProgressBar", info.className)
        assertEquals(0.75f, info.rangeInfo.current)
        assertEquals(1f, info.rangeInfo.max)
    }

    @Test fun xmlParametersInflateCorrectly() = onMain {
        val view = LayoutInflater.from(instrumentation.targetContext).inflate(R.layout.test_marquee, null) as MarqueeView
        assertEquals(8, view.segmentCount); assertEquals(0.5f, view.progress)
        assertEquals(Color.RED, view.lightColor); assertTrue(view.reverseDirection)
        assertEquals(0f, view.cornerRadius); assertTrue(view.isStarted)
        assertEquals(900L, view.shimmerDuration); assertFalse(view.isRunning)
        assertTrue(view.gradientEnabled); assertEquals(135f, view.gradientAngle)
    }

    @Test fun revealIsSequentialRetargetsAndThenStartsShimmer() = withHost { host ->
        val view = host.marquee
        view.segmentCount = 4; view.segmentSpacing = 4f; view.cornerRadius = 0f
        view.lightColor = Color.RED; view.trackColor = Color.BLUE
        view.revealDuration = 1000L
        view.setProgress(1f, true)
        animator(view).currentPlayTime = 250L
        assertEquals(Color.RED, render(view).getPixel(10, 10))
        assertEquals(Color.BLUE, render(view).getPixel(60, 10))
        view.setProgress(0.5f, true)
        assertEquals(Color.RED, render(view).getPixel(10, 10))
        view.start()
        animator(view).end()
        assertTrue(view.isRunning)
        assertEquals(0.5f, view.progress)
        val loop = animator(view)
        view.progress = 0.5f
        assertSame(loop, animator(view)) // repeated binding must not restart shimmer
        val before = render(view).getPixel(10, 10)
        loop.currentPlayTime = (view.shimmerDuration * 0.25f).toLong()
        assertNotEquals(before, render(view).getPixel(10, 10))
    }

    @Test fun animationsStopForHiddenAncestorsWindowAndDetachThenResume() = withHost { host ->
        val view = host.marquee
        view.progress = 1f; view.start(); assertTrue(view.isRunning)
        view.visibility = View.GONE; assertFalse(view.isRunning); assertTrue(view.isStarted)
        view.visibility = View.VISIBLE; assertTrue(view.isRunning)
        host.container.visibility = View.INVISIBLE; assertFalse(view.isRunning)
        host.container.visibility = View.VISIBLE; assertTrue(view.isRunning)
        host.window.decorView.visibility = View.INVISIBLE; assertFalse(view.isRunning)
        host.window.decorView.visibility = View.VISIBLE; assertTrue(view.isRunning)
        host.container.removeView(view); assertFalse(view.isRunning)
        host.container.addView(view); assertTrue(view.isRunning)
        view.stop(); assertFalse(view.isRunning); assertFalse(view.isStarted)
        view.visibility = View.GONE; view.visibility = View.VISIBLE; assertFalse(view.isRunning)
    }

    @Test fun detachingDuringRevealSettlesAtTargetOnReattachment() = withHost { host ->
        val view = host.marquee
        view.segmentCount = 4; view.segmentSpacing = 4f; view.cornerRadius = 0f
        view.lightColor = Color.RED; view.trackColor = Color.BLUE
        view.revealDuration = 1000L; view.setProgress(1f, true)
        animator(view).currentPlayTime = 250L
        assertEquals(Color.BLUE, render(view).getPixel(90, 10))
        host.container.removeView(view); assertFalse(view.isRunning)
        host.container.addView(view)
        assertEquals(Color.RED, render(view).getPixel(90, 10))
        assertEquals(1f, view.progress); assertFalse(view.isRunning)
    }

    @Test fun allDarkAndDetachedViewsDoNotStartFrameLoops() = onMain {
        val view = view()
        view.start(); assertFalse(view.isRunning)
        view.setProgress(0.8f, true)
        assertEquals(0.8f, view.progress, 0.0001f); assertFalse(view.isRunning)
        view.segmentCount = 256
        assertEquals(0f, view.progress); assertTrue(view.isStarted)
        render(view, 1, 1)
    }

    @androidx.test.filters.SdkSuppress(minSdkVersion = 29)
    @Test fun systemDisabledAnimationsShowTargetsWithoutStartingLoop() {
        val context = instrumentation.targetContext
        val original = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        instrumentation.uiAutomation.adoptShellPermissionIdentity(android.Manifest.permission.WRITE_SECURE_SETTINGS)
        try {
            Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
            var disabled = false
            repeat(20) {
                if (!ValueAnimator.areAnimatorsEnabled()) disabled = true
                if (!disabled) android.os.SystemClock.sleep(50)
            }
            assertTrue(disabled)
            withHost { host ->
                host.marquee.setProgress(1f, true); host.marquee.start()
                assertEquals(1f, host.marquee.progress); assertFalse(host.marquee.isRunning)
            }
        } finally {
            Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, original)
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }
}
