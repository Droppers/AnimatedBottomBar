package nl.joery.demo.animatedbottombar.playground.properties

import android.view.animation.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator


class InterpolatorProperty(name: String) : Property(name) {
    companion object {
        val interpolators: List<Interpolator> by lazy {
            ArrayList<Interpolator>().apply {
                add(FastOutSlowInInterpolator())
                add(LinearInterpolator())
                add(AccelerateDecelerateInterpolator())
                add(AccelerateInterpolator())
                add(DecelerateInterpolator())
                add(AnticipateInterpolator())
                add(AnticipateOvershootInterpolator())
                add(OvershootInterpolator())
                add(BounceInterpolator())
            }
        }
    }
}