package nl.joery.animatedbottombar

import android.graphics.Color
import android.graphics.Typeface
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object BottomBarStyle {
    data class Tab(
        // Type
        var selectedTabType: AnimatedBottomBar.TabType = AnimatedBottomBar.TabType.ICON,

        // Animations
        var tabAnimationSelected: AnimatedBottomBar.TabAnimation = AnimatedBottomBar.TabAnimation.SLIDE,
        var tabAnimation: AnimatedBottomBar.TabAnimation = AnimatedBottomBar.TabAnimation.SLIDE,
        var animationDuration: Int = 400,
        var animationInterpolator: Interpolator = FastOutSlowInInterpolator(),

        // Colors
        @ColorInt var tabColorSelected: Int = Color.BLACK,
        @ColorInt var tabColor: Int = Color.BLACK,

        // Ripple
        var rippleEnabled: Boolean = false,
        @ColorInt var rippleColor: Int = Color.BLACK,

        // Text appearance
        @StyleRes var textAppearance: Int = -1,
        var typeface: Typeface = Typeface.DEFAULT,
        var textSize: Int = 14.spPx
    )

    data class Indicator(
        @Dimension var indicatorHeight: Int = 3.dpPx,
        @Dimension var indicatorMargin: Int = 0,
        @ColorInt var indicatorColor: Int = Color.BLACK,
        var indicatorAppearance: AnimatedBottomBar.IndicatorAppearance = AnimatedBottomBar.IndicatorAppearance.SQUARE,
        var indicatorLocation: AnimatedBottomBar.IndicatorLocation = AnimatedBottomBar.IndicatorLocation.TOP,
        var indicatorAnimation: AnimatedBottomBar.IndicatorAnimation = AnimatedBottomBar.IndicatorAnimation.SLIDE
    )

    enum class StyleUpdateType {
        TAB_TYPE,
        COLORS,
        ANIMATIONS,
        RIPPLE,
        TEXT
    }
}