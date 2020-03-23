package nl.joery.demo.animatedbottombar.playground

import android.annotation.SuppressLint
import android.util.TypedValue
import nl.joery.demo.animatedbottombar.ReflectionUtils
import nl.joery.demo.animatedbottombar.dp
import nl.joery.demo.animatedbottombar.playground.properties.*
import nl.joery.demo.animatedbottombar.sp

object XmlGenerator {
    fun generateHtmlXml(
        name: String,
        prefix: String,
        instance: Any,
        properties: List<Property>,
        defaultProviders: Array<Any>
    ): String {
        val sb = StringBuilder()
        sb.append("&lt;")
            .append(coloredText(name, "#22863a"))
            .append("<br>")

        sb.append(getXmlProperty("android:layout_width", "match_parent"))
        sb.append(getXmlProperty("android:layout_height", "wrap_content"))

        for (property in properties) {
            if (!property.modified || property is CategoryProperty) {
                continue
            }

            val defaultValue = getDefaultValue(defaultProviders, property.name)
            val actualValue = ReflectionUtils.getPropertyValue(instance, property.name)
            if ((defaultValue == null && actualValue != null) || defaultValue != actualValue || property is ColorProperty) {
                sb.append(
                    getXmlProperty(
                        if (property.name == "backgroundColor") "android:background" else "app:${prefix}_${property.name}",
                        getHumanValue(property, actualValue!!)
                    )
                )
            }
        }

        return sb.toString().substring(0, sb.toString().length - 4) + " /&gt;"
    }

    private fun getXmlProperty(name: String, value: String): String {
        val sb = StringBuilder()
        return sb.append("&nbsp;&nbsp;&nbsp;&nbsp;")
            .append(coloredText(name, "#6f42c1"))
            .append("=")
            .append(coloredText("&quot;", "#032f62"))
            .append(coloredText(value, "#032f62"))
            .append(coloredText("&quot;", "#032f62"))
            .append("<br>").toString()
    }

    private fun getDefaultValue(defaultProviders: Array<Any>, propertyName: String): Any? {
        for (provider in defaultProviders) {
            val value = ReflectionUtils.getPropertyValue(provider, propertyName)
            if (value != null) {
                return value
            }
        }
        return null
    }

    @SuppressLint("DefaultLocale")
    private fun getHumanValue(property: Property, value: Any): String {
        return when (property) {
            is ColorProperty -> "#%06X".format(0xFFFFFF and (value as Int))
            is IntegerProperty -> when (property.density) {
                TypedValue.COMPLEX_UNIT_DIP -> (value as Int).dp.toString() + "dp"
                TypedValue.COMPLEX_UNIT_SP -> (value as Int).sp.toString() + "sp"
                else -> value.toString()
            }
            is EnumProperty -> value.toString().toLowerCase()
            is InterpolatorProperty -> "@android:anim/" + ReflectionUtils.pascalCaseToSnakeCase(
                value::class.java.simpleName
            )
            else -> value.toString()
        }
    }

    private fun coloredText(text: String, color: String): String {
        return "<font color='$color'>$text</font>"
    }
}