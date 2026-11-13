package io.github.stormblue.marqueeview.sample

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.TextView
import io.github.stormblue.marqueeview.MarqueeView
import kotlin.math.roundToInt

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { view, insets ->
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
        val marquee = findViewById<MarqueeView>(R.id.marquee)
        val gradient = findViewById<MarqueeView>(R.id.gradient)
        gradient.setIntensities(FloatArray(gradient.segmentCount) { 1f - it.toFloat() / gradient.segmentCount })
        gradient.start()
        val reverse = findViewById<MarqueeView>(R.id.reverse)
        val slider = findViewById<SeekBar>(R.id.slider)
        fun showValue() { findViewById<TextView>(R.id.value).text = getString(R.string.level, slider.progress) }
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) { marquee.progress = progress / 100f; showValue() }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        findViewById<View>(R.id.reveal).setOnClickListener {
            marquee.progress = 0f
            marquee.setProgress(slider.progress / 100f, true)
            marquee.start()
        }
        findViewById<View>(R.id.start).setOnClickListener {
            marquee.start(); gradient.start(); reverse.start()
        }
        findViewById<View>(R.id.stop).setOnClickListener {
            marquee.stop(); gradient.stop(); reverse.stop()
        }
        findViewById<View>(R.id.clear).setOnClickListener {
            marquee.stop(); marquee.progress = 0f; slider.progress = 0; showValue()
        }
        showValue()
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val percent = (findViewById<MarqueeView>(R.id.marquee).progress * 100f).roundToInt()
        findViewById<SeekBar>(R.id.slider).progress = percent
        findViewById<TextView>(R.id.value).text = getString(R.string.level, percent)
    }
}
