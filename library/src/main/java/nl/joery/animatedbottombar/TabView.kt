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


internal class TabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var animatedView: View
    private lateinit var selectedAnimatedView: View

    private var selectedOutAnimation: Animation? = null
    private var selectedInAnimation: Animation? = null
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
                updateColors()
            }
            BottomBarStyle.StyleUpdateType.RIPPLE -> {
                updateRipple()
            }
            BottomBarStyle.StyleUpdateType.TEXT -> {
                updateText()
            }
        }
    }

    private fun updateTabType() {
        animatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> imageView
            AnimatedBottomBar.TabType.ICON -> textView
        }

        selectedAnimatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> textView
            AnimatedBottomBar.TabType.ICON -> imageView
        }
        selectedAnimatedView.bringToFront()

        if (selectedAnimatedView.visibility == View.VISIBLE) {
            animatedView.visibility = View.VISIBLE
            selectedAnimatedView.visibility = View.INVISIBLE
        } else {
            animatedView.visibility = View.INVISIBLE
            selectedAnimatedView.visibility = View.VISIBLE
        }
    }

    private fun updateColors() {
        if (style.selectedTabType == AnimatedBottomBar.TabType.ICON) {
            ImageViewCompat.setImageTintList(
                imageView,
                ColorStateList.valueOf(style.tabColorSelected)
            )
            textView.setTextColor(style.tabColor)
        } else if (style.selectedTabType == AnimatedBottomBar.TabType.TEXT) {
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(style.tabColor))
            textView.setTextColor(style.tabColorSelected)
        }
    }

    private fun updateText() {
        textView.typeface = style.typeface
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.textSize.toFloat())

        if (style.textAppearance != -1) {
            TextViewCompat.setTextAppearance(textView, style.textAppearance)
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
            selectedInAnimation = getSelectedAnimation(AnimationDirection.IN)?.apply {
                duration = style.animationDuration.toLong()
                interpolator = style.animationInterpolator
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        selectedAnimatedView.visibility = View.VISIBLE
                    }

                })
            }

            selectedOutAnimation = getSelectedAnimation(AnimationDirection.OUT)?.apply {
                duration = style.animationDuration.toLong()
                interpolator = style.animationInterpolator
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        selectedAnimatedView.visibility = View.INVISIBLE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
            }
        }

        if (style.tabAnimation != AnimatedBottomBar.TabAnimation.NONE) {
            inAnimation = getAnimation(AnimationDirection.IN)?.apply {
                duration = style.animationDuration.toLong()
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
                duration = style.animationDuration.toLong()
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
    }

    private fun getSelectedAnimation(direction: AnimationDirection): Animation? {
        val transformation = getTransformation(selectedAnimatedView)
        if (style.tabAnimationSelected == AnimatedBottomBar.TabAnimation.SLIDE) {
            val deltaYFrom =
                if (transformation != null) getTranslateY(transformation) else (if (direction == AnimationDirection.IN) height.toFloat() else 0f)
            val deltaYTo = if (direction == AnimationDirection.IN) 0f else height.toFloat()
            return TranslateAnimation(0f, 0f, deltaYFrom, deltaYTo)
        } else if (style.tabAnimationSelected == AnimatedBottomBar.TabAnimation.FADE) {
            val alphaFrom =
                transformation?.alpha ?: if (direction == AnimationDirection.IN) 0f else 1f
            val alphaTo = if (direction == AnimationDirection.IN) 1f else 0f
            return AlphaAnimation(alphaFrom, alphaTo)
        }

        return null
    }

    private fun getAnimation(direction: AnimationDirection): Animation? {
        val transformation = getTransformation(animatedView)
        if (style.tabAnimation == AnimatedBottomBar.TabAnimation.SLIDE) {
            val deltaYFrom =
                if (transformation != null) getTranslateY(transformation) else (if (direction == AnimationDirection.IN) -height.toFloat() else 0f)
            val deltaYTo = if (direction == AnimationDirection.IN) 0f else -height.toFloat()

            return TranslateAnimation(0f, 0f, deltaYFrom, deltaYTo)
        } else if (style.tabAnimation == AnimatedBottomBar.TabAnimation.FADE) {
            val alphaFrom =
                transformation?.alpha ?: if (direction == AnimationDirection.IN) 0f else 1f
            val alphaTo = if (direction == AnimationDirection.IN) 1f else 0f
            return AlphaAnimation(alphaFrom, alphaTo)
        }

        return null
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
}