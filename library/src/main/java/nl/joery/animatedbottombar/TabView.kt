package nl.joery.animatedbottombar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.view_tab.view.*
import nl.joery.animatedbottombar.utils.getResourceId


internal class TabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var animatedView: View
    private lateinit var selectedAnimatedView: View

    private val badgeViews: List<BadgeView> by lazy { arrayListOf(text_badge, icon_badge) }
    private var _badge: AnimatedBottomBar.Badge? = null

    private var selectedOutAnimation: Animation? = null
    private var selectedInAnimation: Animation? = null
    private var outAnimation: Animation? = null
    private var inAnimation: Animation? = null

    private lateinit var style: BottomBarStyle.Tab

    var title
        get() = text_view.text.toString()
        set(value) {
            text_view.text = value
        }

    var icon: Drawable?
        get() = icon_view.drawable
        set(value) {
            val newDrawable = value?.constantState?.newDrawable()

            if (newDrawable != null) {
                icon_view.setImageDrawable(newDrawable)
            }
        }

    var badge: AnimatedBottomBar.Badge?
        get() = _badge
        set(value) {
            _badge = value
            updateBadge()
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        updateColors()
    }

    init {
        View.inflate(context, R.layout.view_tab, this)

        icon_badge.scaleLayout = false
        text_badge.scaleLayout = true
    }

    fun applyStyle(style: BottomBarStyle.Tab) {
        BottomBarStyle.StyleUpdateType.values().forEach {
            applyStyle(it, style)
        }
    }

    fun applyStyle(type: BottomBarStyle.StyleUpdateType, style: BottomBarStyle.Tab) {
        this.style = style

        when (type) {
            BottomBarStyle.StyleUpdateType.TAB_TYPE -> updateTabType()
            BottomBarStyle.StyleUpdateType.ANIMATIONS -> updateAnimations()
            BottomBarStyle.StyleUpdateType.COLORS -> updateColors()
            BottomBarStyle.StyleUpdateType.RIPPLE -> updateRipple()
            BottomBarStyle.StyleUpdateType.TEXT -> updateText()
            BottomBarStyle.StyleUpdateType.ICON -> updateIcon()
            BottomBarStyle.StyleUpdateType.BADGE -> updateBadge()
        }
    }

    private fun updateTabType() {
        animatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> icon_layout
            AnimatedBottomBar.TabType.ICON -> text_layout
        }

        selectedAnimatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> text_layout
            AnimatedBottomBar.TabType.ICON -> icon_layout
        }

        if (selectedAnimatedView.visibility == View.VISIBLE) {
            animatedView.visibility = View.VISIBLE
            selectedAnimatedView.visibility = View.INVISIBLE
        } else {
            animatedView.visibility = View.INVISIBLE
            selectedAnimatedView.visibility = View.VISIBLE
        }

        bringViewsToFront()
    }

    private fun updateColors() {
        val tabColor = if (isEnabled) style.tabColor else style.tabColorDisabled
        val tabColorSelected = if (isEnabled) style.tabColorSelected else style.tabColorDisabled
        if (style.selectedTabType == AnimatedBottomBar.TabType.ICON) {
            ImageViewCompat.setImageTintList(
                icon_view,
                ColorStateList.valueOf(tabColorSelected)
            )
            text_view.setTextColor(tabColor)
        } else if (style.selectedTabType == AnimatedBottomBar.TabType.TEXT) {
            ImageViewCompat.setImageTintList(icon_view, ColorStateList.valueOf(tabColor))
            text_view.setTextColor(tabColorSelected)
        }
    }

    private fun updateRipple() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        if (style.rippleEnabled) {
            // Fix for not being able to retrieve color from 'selectableItemBackgroundBorderless'
            if (style.rippleColor > 0) {
                setBackgroundResource(context.getResourceId(style.rippleColor))
            } else {
                background = RippleDrawable(ColorStateList.valueOf(style.rippleColor), null, null)
            }
        } else {
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun updateText() {
        text_view.typeface = style.typeface
        text_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.textSize.toFloat())

        if (style.textAppearance != -1) {
            TextViewCompat.setTextAppearance(text_view, style.textAppearance)
        }
    }

    private fun updateIcon() {
        icon_view.layoutParams.width = style.iconSize
        icon_view.layoutParams.height = style.iconSize
    }

    private fun updateBadge() {
        if (_badge == null) {
            badgeViews.forEach { it.isEnabled = false }
        } else {
            badgeViews.forEach {
                it.text = _badge!!.text

                it.animationType = style.badge.animation
                it.animationDuration = style.badge.animationDuration
                it.setBackgroundColor(_badge?.backgroundColor ?: style.badge.backgroundColor)
                it.textColor = _badge?.textColor ?: style.badge.textColor
                it.textSize = _badge?.textSize ?: style.badge.textSize

                it.isEnabled = true
            }
        }
    }

    private fun bringViewsToFront() {
        selectedAnimatedView.bringToFront()

        badgeViews.forEach {
            it.bringToFront()
        }
    }

    fun select(animate: Boolean = true) {
        updateAnimations()

        if (animate && style.tabAnimationSelected != AnimatedBottomBar.TabAnimation.NONE) {
            selectedAnimatedView.startAnimation(selectedInAnimation)
        } else {
            selectedAnimatedView.visibility = View.VISIBLE
        }

        if (animate && style.tabAnimation != AnimatedBottomBar.TabAnimation.NONE) {
            animatedView.startAnimation(outAnimation)
        } else {
            animatedView.visibility = View.INVISIBLE
        }
    }

    fun deselect(animate: Boolean = true) {
        updateAnimations()

        if (animate && style.tabAnimationSelected != AnimatedBottomBar.TabAnimation.NONE) {
            selectedAnimatedView.startAnimation(selectedOutAnimation)
        } else {
            selectedAnimatedView.visibility = View.INVISIBLE
        }

        if (animate && style.tabAnimation != AnimatedBottomBar.TabAnimation.NONE) {
            animatedView.startAnimation(inAnimation)
        } else {
            animatedView.visibility = View.VISIBLE
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateAnimations()
    }

    private fun updateAnimations() {
        if (style.tabAnimationSelected != AnimatedBottomBar.TabAnimation.NONE) {
            selectedInAnimation = getAnimation(true, AnimationDirection.IN)?.apply {
                setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        selectedAnimatedView.visibility = View.VISIBLE
                    }
                })
            }

            selectedOutAnimation = getAnimation(true, AnimationDirection.OUT)?.apply {
                setAnimationListener(object : AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {
                        selectedAnimatedView.visibility = View.INVISIBLE
                    }
                })
            }
        }

        if (style.tabAnimation != AnimatedBottomBar.TabAnimation.NONE) {
            inAnimation = getAnimation(false, AnimationDirection.IN)?.apply {
                setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        animatedView.visibility = View.VISIBLE
                    }
                })
            }

            outAnimation = getAnimation(false, AnimationDirection.OUT)?.apply {
                setAnimationListener(object : AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {
                        animatedView.visibility = View.INVISIBLE
                    }
                })
            }
        }
    }

    private fun getAnimation(
        selected: Boolean,
        direction: AnimationDirection
    ): Animation? {
        var animation: Animation? = null
        val transformation = getTransformation(if (selected) selectedAnimatedView else animatedView)

        val valueFrom: Float
        val valueTo: Float
        val animationType = if (selected) style.tabAnimationSelected else style.tabAnimation
        if (animationType == AnimatedBottomBar.TabAnimation.SLIDE) {
            if (selected) {
                valueFrom =
                    if (transformation != null) getTranslateY(transformation) else (if (direction == AnimationDirection.IN) height.toFloat() else 0f)
                valueTo = if (direction == AnimationDirection.IN) 0f else height.toFloat()
            } else {
                valueFrom =
                    if (transformation != null) getTranslateY(transformation) else (if (direction == AnimationDirection.IN) -height.toFloat() else 0f)
                valueTo = if (direction == AnimationDirection.IN) 0f else -height.toFloat()
            }

            animation = TranslateAnimation(0f, 0f, valueFrom, valueTo)
        } else if (animationType == AnimatedBottomBar.TabAnimation.FADE) {
            valueFrom =
                transformation?.alpha ?: if (direction == AnimationDirection.IN) 0f else 1f
            valueTo = if (direction == AnimationDirection.IN) 1f else 0f

            animation = AlphaAnimation(valueFrom, valueTo)
        }

        return animation?.apply {
            duration = style.animationDuration.toLong()
            interpolator = style.animationInterpolator
        }
    }

    private fun getTransformation(view: View): Transformation? {
        if (view.animation == null || !view.animation.hasStarted()) {
            return null
        }

        val transformation = Transformation()
        view.animation.getTransformation(view.drawingTime, transformation)
        return transformation
    }

    private fun getTranslateY(transformation: Transformation): Float {
        val v = FloatArray(9)
        transformation.matrix?.getValues(v)
        return v[Matrix.MTRANS_Y]
    }

    private enum class AnimationDirection {
        IN,
        OUT
    }

    private interface AnimationListener : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
    }
}