package nl.joery.demo.animatedbottombar.playground.properties

abstract class Property(val name: String) {
    var modified: Boolean = false

    companion object {
        const val TYPE_INTEGER = 1
        const val TYPE_COLOR = 2
        const val TYPE_ENUM = 3
        const val TYPE_BOOLEAN = 4
        const val TYPE_INTERPOLATOR = 5
        const val TYPE_CATEGORY = 6
    }
}