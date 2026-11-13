package io.github.stormblue.ringprogress

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RingProgressViewTest {
    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
    private fun ring() = RingProgressView(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test fun validatesAndClampsValues() = onMain {
        val view = ring()
        view.progress = -20f
        assertEquals(0f, view.progress)
        view.progress = 200f
        assertEquals(100f, view.progress)
        view.max = 40f
        assertEquals(40f, view.progress)
        for (invalid in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { view.max = invalid }
        }
        assertThrows(IllegalArgumentException::class.java) { view.progress = Float.NaN }
        assertThrows(IllegalArgumentException::class.java) { view.strokeWidth = 0f }
        assertThrows(IllegalArgumentException::class.java) { view.startAngle = Float.NEGATIVE_INFINITY }
        assertThrows(IllegalArgumentException::class.java) { view.animationDuration = -1 }
    }

    @Test fun drawingRespectsProgressDirectionPaddingAndResize() = onMain {
        val view = ring().apply {
            progressColor = Color.RED; trackColor = Color.BLUE
            strokeWidth = 10f; roundedCaps = false
        }
        fun draw(): Bitmap {
            view.measure(View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, 100, 100)
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
        }
        view.progress = 0f
        assertEquals(Color.BLUE, draw().getPixel(95, 50))
        view.progress = 25f
        var bitmap = draw()
        assertEquals(Color.RED, bitmap.getPixel(82, 18))
        assertEquals(Color.BLUE, bitmap.getPixel(18, 18))
        view.clockwise = false
        bitmap = draw()
        assertEquals(Color.RED, bitmap.getPixel(18, 18))
        assertEquals(Color.BLUE, bitmap.getPixel(82, 18))
        view.progress = 100f
        assertEquals(Color.RED, draw().getPixel(95, 50))
        view.setPadding(10, 10, 10, 10)
        bitmap = draw()
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(95, 50))
        assertEquals(Color.RED, bitmap.getPixel(85, 50))
        // Even an oversized stroke and tiny non-square layout must render without invalid bounds.
        view.strokeWidth = 1000f
        view.layout(0, 0, 20, 10)
        view.draw(Canvas(bitmap))
    }

    @Test fun wrapContentAndAtMostRespectParent() = onMain {
        val view = ring()
        view.setPadding(3, 5, 7, 11)
        view.measure(View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.AT_MOST))
        assertEquals(50, view.measuredWidth)
        assertEquals(70, view.measuredHeight)
        view.measure(0, 0)
        assertTrue(view.measuredWidth > 10)
        assertEquals(6, view.measuredHeight - view.measuredWidth)
    }

    @Test fun stateAndAccessibilityDescribeTarget() = onMain {
        val view = ring().apply {
            id = 42; max = 200f; progress = 125f; strokeWidth = 11f
            clockwise = false; roundedCaps = false; startAngle = 30f
            progressColor = Color.MAGENTA; animationDuration = 600L
        }
        val state = SparseArray<android.os.Parcelable>()
        view.saveHierarchyState(state)
        val restored = ring().apply { id = 42; restoreHierarchyState(state) }
        assertEquals(200f, restored.max); assertEquals(125f, restored.progress)
        assertEquals(11f, restored.strokeWidth); assertFalse(restored.clockwise)
        assertFalse(restored.roundedCaps); assertEquals(30f, restored.startAngle)
        assertEquals(Color.MAGENTA, restored.progressColor); assertEquals(600L, restored.animationDuration)
        val info = AccessibilityNodeInfo.obtain()
        restored.onInitializeAccessibilityNodeInfo(info)
        assertEquals("android.widget.ProgressBar", info.className)
        assertEquals(125f, info.rangeInfo.current)
        restored.isIndeterminate = true
        val indeterminateInfo = AccessibilityNodeInfo.obtain()
        restored.onInitializeAccessibilityNodeInfo(indeterminateInfo)
        assertNull(indeterminateInfo.rangeInfo)
        restored.saveHierarchyState(state)
        assertTrue(ring().apply { id = 42; restoreHierarchyState(state) }.isIndeterminate)
    }

    @Test fun detachedAnimationSnapsToTarget() = onMain {
        val view = ring()
        view.setProgress(80f, animated = true)
        assertEquals(80f, view.progress)
        val field = RingProgressView::class.java.getDeclaredField("animator").apply { isAccessible = true }
        assertNull(field.get(view))
    }
    @Test fun attachedAnimationsRetargetAndStopWhenHiddenOrDetached() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = android.content.Intent(instrumentation.targetContext, TestActivity::class.java)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent) as TestActivity
        val field = RingProgressView::class.java.getDeclaredField("animator").apply { isAccessible = true }
        try {
            instrumentation.waitForIdleSync()
            onMain {
                activity.ring.animationDuration = 1000L
                activity.ring.setProgress(90f, animated = true)
                assertNotNull(field.get(activity.ring))
            }
            android.os.SystemClock.sleep(60)
            onMain {
                activity.ring.setProgress(30f, animated = true)
                assertEquals(30f, activity.ring.progress)
                assertTrue((field.get(activity.ring) as android.animation.ValueAnimator).isRunning)
                activity.ring.isIndeterminate = true
                activity.ring.visibility = View.GONE
                assertNull(field.get(activity.ring))
                activity.ring.visibility = View.VISIBLE
                assertTrue((field.get(activity.ring) as android.animation.ValueAnimator).isRunning)
                activity.setContentView(View(activity))
                assertNull(field.get(activity.ring))
            }
        } finally { onMain { activity.finish() } }
    }

}
