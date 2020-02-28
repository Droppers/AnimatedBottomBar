package nl.joery.library.animatedbottombar

import android.graphics.Color
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object BottomBarStyle {
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
        @ColorInt var tabColor: Int = Color.BLACK

        // TODO: Implement these properties
        //@Dimension var textSize: Int = 0
        //@ColorInt var rippleColor: Int = Color.BLACK
    )

    // TODO: Implement indicator
    data class Indicator(
        var animateIndicator: Boolean = true,
        @Dimension var indicatorMargin: Int = 12,
        @Dimension var indicatorHeight: Int = 4,
        @Dimension var indicatorRadius: Int = 4,
        @ColorInt var indicatorColor: Int? = null
    )

    enum class StyleUpdateType {
        TAB_TYPE,
        COLORS,
        ANIMATIONS
    }
}