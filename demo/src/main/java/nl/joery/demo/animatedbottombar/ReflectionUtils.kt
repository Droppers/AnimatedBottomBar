package nl.joery.demo.animatedbottombar

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable

internal object ReflectionUtils {
    @SuppressLint("DefaultLocale")
    fun getPropertyValue(instance: Any, property: String): Any? {
        val methodName =
            if (property == "backgroundColor") "getBackground" else "get" + property.capitalize()
        val method = instance::class.java.methods.toList().find { it.name == methodName }
        val result = method?.invoke(instance)

        return if (result != null && result is ColorDrawable && property == "backgroundColor") {
            result.color
        } else {
            result
        }
    }

    @SuppressLint("DefaultLocale")
    fun setPropertyValue(instance: Any, property: String, value: Any) {
        val methodName = "set" + property.capitalize()
        val method = instance::class.java.methods.toList().find { it.name == methodName }
        method?.invoke(instance, value)
    }
}