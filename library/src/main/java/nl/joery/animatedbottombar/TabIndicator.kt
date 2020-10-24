package nl.joery.animatedbottombar

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.recyclerview.widget.RecyclerView
import nl.joery.animatedbottombar.utils.fixDurationScale
import kotlin.math.abs


internal class TabIndicator(
    private val bottomBar: AnimatedBottomBar,
    private val parent: RecyclerView,
    private val adapter: TabAdapter
) :
    RecyclerView.ItemDecoration() {
    private lateinit var paint: Paint
    private var corners: FloatArray? = null
    private var animator: ValueAnimator? = null

    private var lastSelectedIndex: Int = RecyclerView.NO_POSITION
    private var currentLeft: Float = 0f

    private val indicatorRect: RectF = RectF(0f, 0f, 0f, 0f)

    private val shouldRender: Boolean
        get() = bottomBar.indicatorStyle.indicatorAppearance != AnimatedBottomBar.IndicatorAppearance.INVISIBLE

    init {
        applyStyle()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        if (adapter.selectedIndex == RecyclerView.NO_POSITION || !shouldRender) {
            return
        }

        val isAnimating = animator?.isRunning == true
        val animatedFraction = animator?.animatedFraction ?: 1f
        val lastView = parent.getChildAt(lastSelectedIndex)
        val newView = parent.getChildAt(adapter.selectedIndex) ?: return
        var currentWidth = newView.width.toFloat()

        if (bottomBar.indicatorAnimation == AnimatedBottomBar.IndicatorAnimation.SLIDE) {
            if (isAnimating && lastView != null) {
                currentWidth = lastView.width + (newView.width - lastView.width) * animatedFraction
                currentLeft = animator?.animatedValue as Float
            } else {
                currentLeft = newView.left.toFloat()
            }

            drawIndicator(c, currentLeft, currentWidth)
        } else if (bottomBar.indicatorAnimation == AnimatedBottomBar.IndicatorAnimation.FADE) {
            if (isAnimating && lastView != null) {
                val alpha = 255
                val lastAlpha = alpha - alpha * animatedFraction
                val newAlpha = alpha * animatedFraction
                drawIndicator(
                    c,
                    lastView.left.toFloat(),
                    lastView.width.toFloat(),
                    lastAlpha.toInt()
                )
                drawIndicator(c, newView.left.toFloat(), newView.width.toFloat(), newAlpha.toInt())
            } else {
                drawIndicator(c, newView.left.toFloat(), newView.width.toFloat())
            }
        } else {
            drawIndicator(c, newView.left.toFloat(), newView.width.toFloat())
        }
    }

    private fun drawIndicator(c: Canvas, viewLeft: Float, viewWidth: Float, alpha: Int = 255) {
        indicatorRect.set(
            viewLeft + bottomBar.indicatorStyle.indicatorMargin,
            getTop(),
            viewLeft + viewWidth - bottomBar.indicatorStyle.indicatorMargin,
            getBottom()
        )

        paint.alpha = when {
            alpha < 0 -> abs(alpha)
            alpha > 255 -> 255 - (alpha - 255)
            else -> alpha
        }

        if (bottomBar.indicatorStyle.indicatorAppearance == AnimatedBottomBar.IndicatorAppearance.SQUARE) {
            c.drawRect(
                indicatorRect, paint
            )

        } else if (bottomBar.indicatorStyle.indicatorAppearance == AnimatedBottomBar.IndicatorAppearance.ROUND) {
            val path = Path()
            path.addRoundRect(
                indicatorRect,
                corners!!,
                Path.Direction.CW
            )
            c.drawPath(path, paint)
        }
    }

    private fun getTop(): Float {
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                0f
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                parent.height - bottomBar.indicatorStyle.indicatorHeight.toFloat()
        }
    }

    private fun getCorners(): FloatArray? {
        val radius = bottomBar.indicatorStyle.indicatorHeight.toFloat()
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                floatArrayOf(
                    0f, 0f,
                    0f, 0f,
                    radius, radius,
                    radius, radius
                )
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                floatArrayOf(
                    radius, radius,
                    radius, radius,
                    0f, 0f,
                    0f, 0f
                )
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
        if (animator?.isRunning == true) {
            animator?.cancel()
        }

        if (!shouldRender) {
            return
        }

        val newView = parent.getChildAt(newIndex)
        if (!animate || lastIndex == -1 || newView == null) {
            parent.postInvalidate()
            return
        }

        lastSelectedIndex = lastIndex

        animator = ValueAnimator.ofFloat(currentLeft, newView.left.toFloat()).apply {
            duration = bottomBar.tabStyle.animationDuration.toLong()
            interpolator = bottomBar.tabStyle.animationInterpolator
            fixDurationScale()
            addUpdateListener {
                parent.postInvalidate()
            }
            start()
        }
    }

    fun applyStyle() {
        paint = Paint().apply {
            color = bottomBar.indicatorStyle.indicatorColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        corners = getCorners()

        if (shouldRender) {
            parent.postInvalidate()
        }
    }
}