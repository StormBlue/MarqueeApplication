package io.github.stormblue.marqueeview.sample

import android.content.Intent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.stormblue.marqueeview.MarqueeView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleStateTest {
    @Test fun recreationPreservesPercentageAndStoppedState() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
        try {
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                activity.findViewById<MarqueeView>(R.id.marquee).progress = 0.53f
                activity.findViewById<SeekBar>(R.id.slider).progress = 53
                activity.findViewById<View>(R.id.stop).performClick()
            }
            val monitor = instrumentation.addMonitor(MainActivity::class.java.name, null, false)
            try {
                instrumentation.runOnMainSync { activity.recreate() }
                val restored = monitor.waitForActivityWithTimeout(5000) as? MainActivity
                assertNotNull("Activity must recreate", restored)
                activity = restored!!
                instrumentation.waitForIdleSync()
                instrumentation.runOnMainSync {
                    assertEquals(53, activity.findViewById<SeekBar>(R.id.slider).progress)
                    assertEquals(activity.getString(R.string.level, 53), activity.findViewById<TextView>(R.id.value).text)
                    assertFalse(activity.findViewById<MarqueeView>(R.id.marquee).isStarted)
                    activity.findViewById<View>(R.id.reveal).performClick()
                    assertEquals(0.53f, activity.findViewById<MarqueeView>(R.id.marquee).progress, 0.0001f)
                }
            } finally { instrumentation.removeMonitor(monitor) }
        } finally { instrumentation.runOnMainSync { activity.finish() } }
    }
}
