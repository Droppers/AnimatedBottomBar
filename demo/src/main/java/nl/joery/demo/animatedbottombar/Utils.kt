package nl.joery.demo.animatedbottombar

internal object Utils {
    fun getProperty(instance: Any, property: String): Any? {
        val methodName = "get" + property.capitalize()
        val method = instance.javaClass.methods.toList().find { it.name == methodName }
        return method?.invoke(instance)
    }

    fun setProperty(instance: Any, property: String, value: Any) {
        val methodName = "set" + property.capitalize()
        val method = instance.javaClass.methods.toList().find { it.name == methodName }
        method?.invoke(instance, value)
    }
}