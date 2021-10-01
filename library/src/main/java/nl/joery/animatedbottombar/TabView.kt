package nl.joery.animatedbottombar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import nl.joery.animatedbottombar.utils.dpPx
import nl.joery.animatedbottombar.utils.getResourceId

internal class TabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var animatedView: View
    private lateinit var selectedAnimatedView: View

    private lateinit var style: BottomBarStyle.Tab
    private lateinit var iconBadge: BadgeView
    private lateinit var textBadge: BadgeView

    private lateinit var textView: TextView
    private lateinit var iconView: AppCompatImageView

    private lateinit var iconLayout: ViewGroup
    private lateinit var textLayout: ViewGroup

    private val badgeViews: Array<out BadgeView> by lazy { arrayOf(textBadge, iconBadge) }
    private var _badge: AnimatedBottomBar.Badge? = null

    private var selectedOutAnimation: Animation? = null
    private var selectedInAnimation: Animation? = null
    private var outAnimation: Animation? = null
    private var inAnimation: Animation? = null

    private val transformationMatrixValues = FloatArray(9)
    private val tempTransformation = Transformation()

    private val showSelectedAnimatedViewOnStart = animationListener(onStart = {
        selectedAnimatedView.visibility = View.VISIBLE
    })

    private val hideSelectedAnimatedViewOnEnd = animationListener(onEnd = {
        selectedAnimatedView.visibility = View.INVISIBLE
    })

    private val showAnimatedViewOnStart = animationListener(onStart = {
        animatedView.visibility = View.VISIBLE
    })

    private val hideAnimatedViewOnEnd = animationListener(onEnd = {
        animatedView.visibility = View.INVISIBLE
    })

    var title
        get() = textView.text.toString()
        set(value) {
            textView.text = value
        }

    var icon: Drawable?
        get() = iconView.drawable
        set(value) {
            val newDrawable = value?.constantState?.newDrawable()

            if (newDrawable != null) {
                iconView.setImageDrawable(newDrawable)
            }
        }

    var iconSize: Int = -1
        set(value) {
            field = value
            updateIcon()
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
        initLayout()

        iconBadge.scaleLayout = false
        textBadge.scaleLayout = true
    }

    private fun initLayout() {
        addView(LinearLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL

            textLayout = this

            addView(AppCompatTextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                textView = this
                ellipsize = TextUtils.TruncateAt.END
                isSingleLine = true
            })

            addView(BadgeView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                textBadge = this
                val padding = 4.dpPx
                setPadding(padding, 0, 0, 0)
            })
        })

        addView(LinearLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            iconLayout = this

            val padding = 8.dpPx
            setPadding(0, padding, 0, padding)

            clipToPadding = false
            visibility = View.GONE

            addView(AppCompatImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    gravity = Gravity.CENTER
                }

                orientation = LinearLayout.HORIZONTAL
                iconView = this
                scaleType = ImageView.ScaleType.FIT_CENTER
            })

            addView(BadgeView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                val translation = (-8).dpPx.toFloat()
                translationX = translation
                translationY = translation

                iconBadge = this
            })
        })
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
            AnimatedBottomBar.TabType.TEXT -> iconLayout
            AnimatedBottomBar.TabType.ICON -> textLayout
        }

        selectedAnimatedView = when (style.selectedTabType) {
            AnimatedBottomBar.TabType.TEXT -> textLayout
            AnimatedBottomBar.TabType.ICON -> iconLayout
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
        val tabColor: Int
        val tabColorSelected: Int

        if(isEnabled) {
            tabColor = style.tabColor
            tabColorSelected = style.tabColorSelected
        } else {
            tabColor = style.tabColorDisabled
            tabColorSelected = tabColor
        }

        val iconTint: Int
        val textColor: Int

        when(style.selectedTabType) {
            AnimatedBottomBar.TabType.ICON -> {
                iconTint = tabColorSelected
                textColor = tabColor
            }
            AnimatedBottomBar.TabType.TEXT -> {
                iconTint = tabColor
                textColor = tabColorSelected
            }
        }

        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(iconTint))
        textView.setTextColor(textColor)
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
        textView.run {
            typeface = style.typeface
            setTextSize(TypedValue.COMPLEX_UNIT_PX, style.textSize.toFloat())
        }

        if (style.textAppearance != -1) {
            TextViewCompat.setTextAppearance(textView, style.textAppearance)
        }
    }

    private fun updateIcon() {
        val size = if(iconSize > 0) iconSize else style.iconSize

        iconView.run {
            layoutParams = layoutParams.apply {
                width = size
                height = size
            }
        }
        invalidate()
    }

    private fun updateBadge() {
        val badge = _badge
        if (badge == null) {
            badgeViews.forEach { it.isEnabled = false }
        } else {
            badgeViews.forEach {
                it.text = badge.text

                it.animationType = style.badge.animation
                it.animationDuration = style.badge.animationDuration
                it.setBackgroundColor(badge.backgroundColor ?: style.badge.backgroundColor)
                it.textColor = badge.textColor ?: style.badge.textColor
                it.textSize = badge.textSize ?: style.badge.textSize

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
            selectedInAnimation = getAnimation(true, DIRECTION_IN)?.apply {
                setAnimationListener(showSelectedAnimatedViewOnStart)
            }

            selectedOutAnimation = getAnimation(true, DIRECTION_OUT)?.apply {
                setAnimationListener(hideSelectedAnimatedViewOnEnd)
            }
        }

        if (style.tabAnimation != AnimatedBottomBar.TabAnimation.NONE) {
            inAnimation = getAnimation(false, DIRECTION_IN)?.apply {
                setAnimationListener(showAnimatedViewOnStart)
            }

            outAnimation = getAnimation(false, DIRECTION_OUT)?.apply {
                setAnimationListener(hideAnimatedViewOnEnd)
            }
        }
    }

    private fun getAnimation(
        selected: Boolean,
        direction: Int
    ): Animation? {
        var animation: Animation? = null
        val transformationView = if (selected) selectedAnimatedView else animatedView
        val transformationChanged = getTransformation(transformationView, tempTransformation)

        val valueFrom: Float
        val valueTo: Float
        val animationType = if (selected) style.tabAnimationSelected else style.tabAnimation

        when(animationType) {
            AnimatedBottomBar.TabAnimation.SLIDE -> {
                if (selected) {
                    valueFrom = when {
                        transformationChanged -> getTranslateY(tempTransformation)
                        direction == DIRECTION_IN -> height.toFloat()
                        else -> 0f
                    }
                    valueTo = if (direction == DIRECTION_IN) 0f else height.toFloat()
                } else {
                    valueFrom = when {
                        transformationChanged -> getTranslateY(tempTransformation)
                        direction == DIRECTION_IN -> -height.toFloat()
                        else -> 0f
                    }
                    valueTo = if (direction == DIRECTION_IN) 0f else -height.toFloat()
                }

                animation = TranslateAnimation(0f, 0f, valueFrom, valueTo)
            }
            AnimatedBottomBar.TabAnimation.FADE -> {
                valueFrom = when {
                    transformationChanged -> tempTransformation.alpha
                    direction == DIRECTION_IN -> 0f
                    else -> 1f
                }
                valueTo = if (direction == DIRECTION_IN) 1f else 0f

                animation = AlphaAnimation(valueFrom, valueTo)
            }
        }

        return animation?.apply {
            duration = style.animationDuration.toLong()
            interpolator = style.animationInterpolator
        }
    }

    private fun getTransformation(view: View, outTransformation: Transformation): Boolean {
        val viewAnimation = view.animation
        if (viewAnimation == null || !viewAnimation.hasStarted()) {
            return false
        }

        viewAnimation.getTransformation(view.drawingTime, outTransformation)
        return true
    }

    private fun getTranslateY(transformation: Transformation): Float {
        transformation.matrix.getValues(transformationMatrixValues)
        return transformationMatrixValues[Matrix.MTRANS_Y]
    }

    companion object {
        private const val DIRECTION_IN = 0
        private const val DIRECTION_OUT = 1

        inline fun animationListener(
            crossinline onStart: () -> Unit = {},
            crossinline onEnd: () -> Unit = {}
        ): Animation.AnimationListener {
            return object: Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    onStart()
                }

                override fun onAnimationEnd(animation: Animation?) {
                    onEnd()
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            }
        }
    }
}