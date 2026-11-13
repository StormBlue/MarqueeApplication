package io.github.stormblue.marqueeview

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout

/** Device-test host, excluded from the release AAR. */
class TestActivity : Activity() {
    lateinit var marquee: MarqueeView
    lateinit var container: FrameLayout
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        marquee = MarqueeView(this)
        container = FrameLayout(this).apply { addView(marquee) }
        setContentView(container)
    }
}
