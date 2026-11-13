package io.github.stormblue.ringprogress

import android.app.Activity
import android.os.Bundle

/** Host for device tests; excluded from the published release AAR. */
class TestActivity : Activity() {
    lateinit var ring: RingProgressView
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        ring = RingProgressView(this)
        setContentView(ring)
    }
}
