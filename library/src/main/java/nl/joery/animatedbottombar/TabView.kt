package nl.joery.animatedbottombar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.view_tab.view.*


internal class TabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var animatedView: View
    private lateinit var activeAnimatedView: View

    private var activeOutAnimation: Animation? = null
    private var activeInAnimation: Animation? = null
    private var outAnimation: Animation? = null
    private var inAnimation: Animation? = null

    private lateinit var style: BottomBarStyle.Tab

    init {
        View.inflate(context, R.layout.view_tab, this)
    }

    fun applyStyle(style: BottomBarStyle.Tab) {
        BottomBarStyle.StyleUpdateType.values().forEach {
            applyStyle(it, style)
        }
    }

    fun applyStyle(type: BottomBarStyle.StyleUpdateType, style: BottomBarStyle.Tab) {
        this.style = style

        when (type) {
            BottomBarStyle.StyleUpdateType.TAB_TYPE ->
                updateTabType()
            BottomBarStyle.StyleUpdateType.ANIMATIONS ->
                updateAnimations()
            BottomBarStyle.StyleUpdateType.COLORS -> {
                updateActiveColors()
                updateColors()
            }
            BottomBarStyle.StyleUpdateType.RIPPLE -> {
                updateRipple()
            }
            BottomBarStyle.StyleUpdateType.TEXT_APPEARANCE -> {
                updateTextAppearance()
            }
        }
    }

    private fun updateTabType() {
        animatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> imageView
            AnimatedBottomBar.TabType.ICON -> textView
        }
        animatedView.visibility = View.VISIBLE

        activeAnimatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> textView
            AnimatedBottomBar.TabType.ICON -> imageView
        }
        activeAnimatedView.visibility = View.INVISIBLE
        activeAnimatedView.bringToFront()
    }

    private fun updateActiveColors() {
        if (style.selectedTabType == AnimatedBottomBar.TabType.ICON) {
            ImageViewCompat.setImageTintList(
                imageView,
                ColorStateList.valueOf(style.tabColorSelected)
            )
        }

        if (style.selectedTabType == AnimatedBottomBar.TabType.TEXT) {
            textView.setTextColor(style.tabColorSelected)
        }
    }

    private fun updateColors() {
        if (style.selectedTabType == AnimatedBottomBar.TabType.ICON) {
            textView.setTextColor(style.tabColor)
        }

        if (style.selectedTabType == AnimatedBottomBar.TabType.TEXT) {
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(style.tabColor))
        }
    }

    private fun updateTextAppearance() {
        TextViewCompat.setTextAppearance(textView, style.textAppearance)
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

    fun setText(text: String) {
        textView.text = text
    }

    fun setIcon(drawable: Drawable?) {
        val newDrawable = drawable?.constantState?.newDrawable()

        if (newDrawable != null) {
            imageView.setImageDrawable(newDrawable)
        }
    }

    fun select(animate: Boolean = true) {
        if (animate && style.animationTypeSelected != AnimatedBottomBar.TabAnimationType.NONE) {
            activeAnimatedView.startAnimation(activeInAnimation)
        } else {
            activeAnimatedView.visibility = View.VISIBLE
        }

        if (animate && style.animationType != AnimatedBottomBar.TabAnimationType.NONE) {
            animatedView.startAnimation(outAnimation)
        } else {
            animatedView.visibility = View.INVISIBLE
        }
    }

    fun deselect(animate: Boolean = true) {
        if (animate && style.animationTypeSelected != AnimatedBottomBar.TabAnimationType.NONE) {
            activeAnimatedView.startAnimation(activeOutAnimation)
        } else {
            activeAnimatedView.visibility = View.INVISIBLE
        }

        if (animate && style.animationType != AnimatedBottomBar.TabAnimationType.NONE) {
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
        activeInAnimation = getActiveAnimation(AnimationDirection.IN)?.apply {
            duration = style.animationDuration
            interpolator = style.animationInterpolator
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationStart(animation: Animation?) {
                    activeAnimatedView.visibility = View.VISIBLE
                }

            })
        }

        activeOutAnimation = getActiveAnimation(AnimationDirection.OUT)?.apply {
            duration = style.animationDuration
            interpolator = style.animationInterpolator
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    activeAnimatedView.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }

        inAnimation = getAnimation(AnimationDirection.IN)?.apply {
            duration = style.animationDuration
            interpolator = style.animationInterpolator
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationStart(animation: Animation?) {
                    animatedView.visibility = View.VISIBLE
                }
            })
        }

        outAnimation = getAnimation(AnimationDirection.OUT)?.apply {
            duration = style.animationDuration
            interpolator = style.animationInterpolator
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    animatedView.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }
    }

    private fun getActiveAnimation(direction: AnimationDirection): Animation? {
        if (style.animationTypeSelected == AnimatedBottomBar.TabAnimationType.SLIDE) {
            val deltaYFrom = if (direction == AnimationDirection.IN) height.toFloat() else 0f
            val deltaYTo = if (direction == AnimationDirection.IN) 0f else height.toFloat()
            return TranslateAnimation(0f, 0f, deltaYFrom, deltaYTo)
        } else if (style.animationTypeSelected == AnimatedBottomBar.TabAnimationType.FADE) {
            val alphaFrom = if (direction == AnimationDirection.IN) 0f else 1f
            val alphaTo = if (direction == AnimationDirection.IN) 1f else 0f
            return AlphaAnimation(alphaFrom, alphaTo)
        }

        return null
    }

    private fun getAnimation(direction: AnimationDirection): Animation? {
        if (style.animationType == AnimatedBottomBar.TabAnimationType.SLIDE) {
            val deltaYFrom = if (direction == AnimationDirection.IN) -height.toFloat() else 0f
            val deltaYTo = if (direction == AnimationDirection.IN) 0f else -height.toFloat()
            return TranslateAnimation(0f, 0f, deltaYFrom, deltaYTo)
        } else if (style.animationType == AnimatedBottomBar.TabAnimationType.FADE) {
            val alphaFrom = if (direction == AnimationDirection.IN) 0f else 1f
            val alphaTo = if (direction == AnimationDirection.IN) 1f else 0f
            return AlphaAnimation(alphaFrom, alphaTo)
        }

        return null
    }

    private enum class AnimationDirection {
        IN,
        OUT
    }
}