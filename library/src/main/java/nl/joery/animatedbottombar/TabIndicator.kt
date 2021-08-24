package nl.joery.animatedbottombar

import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.util.FloatProperty
import android.util.Property
import androidx.recyclerview.widget.RecyclerView
import nl.joery.animatedbottombar.utils.fixDurationScale

internal class TabIndicator(
    private val bottomBar: AnimatedBottomBar,
    private val parent: RecyclerView,
    private val adapter: TabAdapter
) : RecyclerView.ItemDecoration() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val corners = FloatArray(8)
    private val animator = ObjectAnimator().apply {
        target = this@TabIndicator
        setProperty(CURRENT_LEFT_PROPERTY)
        fixDurationScale()
    }

    private var lastSelectedIndex: Int = RecyclerView.NO_POSITION
    private var currentLeft: Float = 0f

    private val indicatorRect = RectF()
    private val roundRectPath = Path()

    private val shouldRender: Boolean
        get() = bottomBar.indicatorStyle.indicatorAppearance != AnimatedBottomBar.IndicatorAppearance.INVISIBLE

    init {
        applyStyle()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (adapter.selectedIndex == RecyclerView.NO_POSITION || !shouldRender) {
            return
        }

        val isAnimating = animator.isRunning
        val animatedFraction = animator.animatedFraction
        val lastView = parent.getChildAt(lastSelectedIndex)
        val newView = parent.getChildAt(adapter.selectedIndex) ?: return

        val newViewWidth = newView.width.toFloat()
        val newViewLeft = newView.left.toFloat()

        var currentWidth = newViewWidth

        when(bottomBar.indicatorAnimation) {
            AnimatedBottomBar.IndicatorAnimation.SLIDE -> {
                if (isAnimating && lastView != null) {
                    val lastViewWidth = lastView.width.toFloat()
                    currentWidth =
                        lastViewWidth + (newViewWidth - lastViewWidth) * animatedFraction
                } else {
                    currentLeft = newViewLeft
                }

                drawIndicator(c, currentLeft, currentWidth)
            }
            AnimatedBottomBar.IndicatorAnimation.FADE -> {
                if (isAnimating && lastView != null) {
                    val newAlpha = 255f * animatedFraction
                    val lastAlpha = 255f - newAlpha

                    drawIndicator(
                        c,
                        lastView.left.toFloat(),
                        lastView.width.toFloat(),
                        lastAlpha.toInt()
                    )
                    drawIndicator(
                        c,
                        newViewLeft,
                        newViewWidth,
                        newAlpha.toInt()
                    )
                } else {
                    drawIndicator(c, newViewLeft, newViewWidth)
                }
            }
            else -> {
                drawIndicator(c, newViewLeft, newViewWidth)
            }
        }
    }

    private fun drawIndicator(c: Canvas, viewLeft: Float, viewWidth: Float, alpha: Int = 255) {
        val indicatorMargin = bottomBar.indicatorStyle.indicatorMargin
        indicatorRect.set(
            viewLeft + indicatorMargin,
            getTop(),
            viewLeft + viewWidth - indicatorMargin,
            getBottom()
        )

        paint.alpha = alpha

        when(bottomBar.indicatorStyle.indicatorAppearance) {
            AnimatedBottomBar.IndicatorAppearance.SQUARE -> {
                c.drawRect(indicatorRect, paint)
            }
            AnimatedBottomBar.IndicatorAppearance.ROUND -> {
                val path = roundRectPath
                path.rewind()
                path.addRoundRect(
                    indicatorRect,
                    corners,
                    Path.Direction.CW
                )
                c.drawPath(path, paint)
            }
            else -> {
            }
        }
    }

    private fun getTop(): Float {
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                0f
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                (parent.height - bottomBar.indicatorStyle.indicatorHeight).toFloat()
        }
    }

    private fun updateCorners() {
        val indicatorStyle = bottomBar.indicatorStyle
        val radius = indicatorStyle.indicatorHeight.toFloat()

        when (indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP -> {
                corners.run {
                    this[0] = 0f
                    this[1] = 0f
                    this[2] = 0f
                    this[3] = 0f

                    this[4] = radius
                    this[5] = radius
                    this[6] = radius
                    this[7] = radius
                }
            }
            AnimatedBottomBar.IndicatorLocation.BOTTOM -> {
                corners.run {
                    this[0] = radius
                    this[1] = radius
                    this[2] = radius
                    this[3] = radius

                    this[4] = 0f
                    this[5] = 0f
                    this[6] = 0f
                    this[7] = 0f
                }
            }
        }
    }

    private fun getBottom(): Float {
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                bottomBar.indicatorStyle.indicatorHeight.toFloat()
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                parent.height.toFloat()
        }
    }

    fun setSelectedIndex(lastIndex: Int, newIndex: Int, animate: Boolean) {
        if (animator.isRunning) {
            animator.cancel()
        }

        if (!shouldRender) {
            return
        }

        val newView = parent.getChildAt(newIndex)
        if (!animate || lastIndex == -1 || newView == null) {
            parent.invalidate()
            return
        }

        lastSelectedIndex = lastIndex

        animator.run {
            setFloatValues(currentLeft, newView.left.toFloat())
            duration = bottomBar.tabStyle.animationDuration.toLong()
            interpolator = bottomBar.tabStyle.animationInterpolator

            start()
        }
    }

    fun applyStyle() {
        paint.color = bottomBar.indicatorStyle.indicatorColor
        updateCorners()

        if (shouldRender) {
            parent.invalidate()
        }
    }

    companion object {
        private val CURRENT_LEFT_PROPERTY = if(Build.VERSION.SDK_INT >= 24) {
            object: FloatProperty<TabIndicator>("currentLeft") {
                override fun get(o: TabIndicator): Float = o.currentLeft
                override fun setValue(o: TabIndicator, value: Float) {
                    o.currentLeft = value
                    o.parent.invalidate()
                }
            }
        } else {
            object: Property<TabIndicator, Float>(Float::class.java, "currentLeft") {
                override fun get(o: TabIndicator): Float = o.currentLeft
                override fun set(o: TabIndicator, value: Float) {
                    o.currentLeft = value
                    o.parent.invalidate()
                }
            }
        }
    }
}