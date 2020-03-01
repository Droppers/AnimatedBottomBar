package nl.joery.animatedbottombar

import android.graphics.Color
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

internal object BottomBarStyle {
    data class Tab(
        // Type
        var selectedTabType: AnimatedBottomBar.TabType = AnimatedBottomBar.TabType.TEXT,

        // Animations
        var animationTypeSelected: AnimatedBottomBar.TabAnimationType = AnimatedBottomBar.TabAnimationType.SLIDE,
        var animationType: AnimatedBottomBar.TabAnimationType = AnimatedBottomBar.TabAnimationType.SLIDE,
        var animationDuration: Long = 400L,
        var animationInterpolator: Interpolator = FastOutSlowInInterpolator(),

        // Colors
        @ColorInt var tabColorSelected: Int = Color.BLACK,
        @ColorInt var tabColor: Int = Color.BLACK,

        // Ripple
        var rippleEnabled: Boolean = false,
        @ColorInt var rippleColor: Int = Color.BLACK

        // TODO: Implement these properties
        //@Dimension var textSize: Int = 0
    )

    data class Indicator(
        @Dimension var indicatorHeight: Float = 3.px.toFloat(),
        @Dimension var indicatorMargin: Float = 0f,
        @ColorInt var indicatorColor: Int = Color.BLACK,
        var indicatorAppearance: AnimatedBottomBar.IndicatorAppearance = AnimatedBottomBar.IndicatorAppearance.SQUARE,
        var indicatorLocation: AnimatedBottomBar.IndicatorLocation = AnimatedBottomBar.IndicatorLocation.TOP,
        var indicatorAnimation: Boolean = true
    )

    enum class StyleUpdateType {
        TAB_TYPE,
        COLORS,
        ANIMATIONS,
        RIPPLE
    }
}