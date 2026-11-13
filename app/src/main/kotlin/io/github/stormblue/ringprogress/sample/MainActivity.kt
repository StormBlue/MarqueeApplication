package io.github.stormblue.ringprogress.sample

import android.app.Activity
import android.os.Bundle
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.TextView
import io.github.stormblue.ringprogress.RingProgressView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<android.view.View>(R.id.root).setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        val ring = findViewById<RingProgressView>(R.id.progress)
        val loading = findViewById<RingProgressView>(R.id.loading)
        val value = findViewById<TextView>(R.id.value)
        val slider = findViewById<SeekBar>(R.id.slider)
        fun showValue() { value.text = getString(R.string.progress_label, ring.progress.toInt()) }
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) { ring.progress = progress.toFloat(); showValue() }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        findViewById<android.view.View>(R.id.animate).setOnClickListener {
            ring.setProgress(75f, animated = true); slider.progress = 75; showValue()
        }
        findViewById<android.view.View>(R.id.reset).setOnClickListener {
            ring.progress = 0f; slider.progress = 0; showValue()
        }
        findViewById<android.view.View>(R.id.toggle).setOnClickListener { loading.isIndeterminate = !loading.isIndeterminate }
        showValue()
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val ring = findViewById<RingProgressView>(R.id.progress)
        findViewById<SeekBar>(R.id.slider).progress = ring.progress.toInt()
        findViewById<TextView>(R.id.value).text = getString(R.string.progress_label, ring.progress.toInt())
    }
}
