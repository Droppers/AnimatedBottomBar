package nl.joery.demo.animatedbottombar

import android.content.res.Resources
import kotlin.math.roundToInt

internal val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).roundToInt()
internal val Int.sp: Int
    get() = (this / Resources.getSystem().displayMetrics.scaledDensity).roundToInt()
internal val Int.dpPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()
internal val Int.spPx: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()