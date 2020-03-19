package nl.joery.demo.animatedbottombar

import android.content.res.Resources
import kotlin.math.roundToInt

internal val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).roundToInt()
internal val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()