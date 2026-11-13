package io.github.stormblue.marqueeview.sample

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.TextView
import io.github.stormblue.marqueeview.MarqueeView
import kotlin.math.roundToInt

/** A focused, interactive showcase; gradient angles change only when the slider changes. */
class GradientGalleryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_gallery)
        findViewById<View>(R.id.gallery_root).setOnApplyWindowInsetsListener { view, insets ->
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
        findViewById<View>(R.id.gallery_back).setOnClickListener { finish() }
        val custom = findViewById<MarqueeView>(R.id.gallery_custom)
        findViewById<SeekBar>(R.id.gallery_angle_slider).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                custom.gradientAngle = progress.toFloat()
                showAngle(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        showAngle(custom.gradientAngle.roundToInt())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val angle = findViewById<MarqueeView>(R.id.gallery_custom).gradientAngle.roundToInt()
        findViewById<SeekBar>(R.id.gallery_angle_slider).progress = angle
        showAngle(angle)
    }

    private fun showAngle(angle: Int) {
        findViewById<TextView>(R.id.gallery_angle_value).text = getString(R.string.gallery_angle, angle)
    }
}
