package nl.joery.animatedbottombar

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.text.Layout
import android.view.Gravity
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import nl.joery.animatedbottombar.utils.dpPx
import nl.joery.animatedbottombar.utils.spPx

object BottomBarStyle {
    // Suppresses the warning on textBreakStrategy and textHyphenationFrequency.
    // The values won't be used if API level < 23
    @SuppressLint("NewApi")
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
        @ColorInt var tabColorDisabled: Int = Color.BLACK,
        @ColorInt var tabColor: Int = Color.BLACK,

        // Ripple
        var rippleEnabled: Boolean = false,
        @ColorInt var rippleColor: Int = Color.BLACK,

        // Text
        @StyleRes var textAppearance: Int = -1,
        var typeface: Typeface = Typeface.DEFAULT,
        var textSize: Int = 14.spPx,
        var textMaxLines: Int = 1,
        var textGravity: Int = Gravity.CENTER_HORIZONTAL,
        var textBreakStrategy: Int = LineBreaker.BREAK_STRATEGY_SIMPLE,
        var textHyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE,

        // Icon
        var iconSize: Int = 24.dpPx,

        // Badge
        var badge: Badge = Badge()
    )

    data class Badge(
        var animation: AnimatedBottomBar.BadgeAnimation = AnimatedBottomBar.BadgeAnimation.SCALE,
        var animationDuration: Int = 150,
        @ColorInt var backgroundColor: Int = Color.rgb(255, 12, 16),
        @ColorInt var textColor: Int = Color.WHITE,
        @Dimension var textSize: Int = 9.spPx
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
        TEXT,
        ICON,
        BADGE
    }
}