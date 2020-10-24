package nl.joery.animatedbottombar.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.AnimRes

internal object Utils {
    @SuppressLint("ResourceType")
    fun loadInterpolator(
        context: Context, @AnimRes resId: Int,
        defaultInterpolator: Interpolator
    ): Interpolator {
        if (resId > 0) {
            return AnimationUtils.loadInterpolator(context, resId)
        }

        return defaultInterpolator
    }
}