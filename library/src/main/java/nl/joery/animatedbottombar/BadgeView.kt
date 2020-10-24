package nl.joery.animatedbottombar

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import nl.joery.animatedbottombar.utils.dpPx
import kotlin.math.max


class BadgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val backgroundPaint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()
    private val horizontalPadding: Int = 6.dpPx

    private var animator: ValueAnimator? = null

    var animationType: AnimatedBottomBar.BadgeAnimation = AnimatedBottomBar.BadgeAnimation.SCALE

    var scaleLayout: Boolean = false

    private var _text: String? = null
    var text: String?
        get() = _text
        set(value) {
            _text = value
            postInvalidate()
        }

    private var _animationDuration: Int = 0
    var animationDuration: Int
        get() = _animationDuration
        set(value) {
            _animationDuration = value
            postInvalidate()
        }

    private var _backgroundColor: Int = Color.WHITE
    var backgroundColor: Int = 0
        @ColorInt get() = _backgroundColor
        private set

    private var _textColor: Int = Color.WHITE
    var textColor: Int
        @ColorInt get() = _textColor
        set(@ColorInt value) {
            _textColor = value
            updatePaint()
        }

    private var _textSize: Int = Color.WHITE
    var textSize: Int
        @Dimension get() = _textSize
        set(@ColorInt value) {
            _textSize = value
            updatePaint()
        }

    init {
        isEnabled = false
    }

    private fun updatePaint() {
        textPaint.apply {
            textSize = _textSize.toFloat()
            color = _textColor
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            isDither = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        backgroundPaint.apply {
            isAntiAlias = true
            isDither = true
            color = _backgroundColor
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        val c = canvas!!
        drawBackground(c)
        drawText(c)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textWidth = if (text == null) 0f else textPaint.measureText(text)
        val newWidth =
            max(textWidth.toInt() + horizontalPadding, 16.dpPx) + paddingLeft + paddingRight
        val newHeight = 16.dpPx + paddingTop + paddingBottom

        if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec((newWidth * fraction).toInt(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((newHeight * fraction).toInt(), MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
            )
        }
    }

    private fun drawBackground(canvas: Canvas) {
        if (text == null) {
            val middleX = (measuredWidth + paddingLeft) / 2f
            val middleY = (measuredHeight + paddingTop) / 2f

            if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
                canvas.scale(fraction, fraction, middleX, middleY)
            }
            canvas.drawCircle(
                middleX,
                middleY,
                4.dpPx.toFloat(),
                backgroundPaint
            )
            if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
                canvas.scale(1f, 1f)
            }
        } else {
            val radius = 8.dpPx.toFloat()

            val rect = RectF(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                measuredWidth.toFloat() - paddingRight.toFloat(),
                measuredHeight.toFloat() - paddingBottom.toFloat()
            )
            canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        }
    }

    private fun drawText(canvas: Canvas) {
        if (_text == null) {
            return
        }

        val middleX = (measuredWidth + paddingLeft) / 2f
        val middleY = (measuredHeight + paddingTop) / 2f

        if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
            canvas.scale(fraction, fraction, middleX, middleY)
        }

        val rect = Rect()
        textPaint.getTextBounds(_text, 0, _text!!.length, rect)

        val x = middleX - rect.width() / 2f - rect.left
        val y = middleY + rect.height() / 2f - rect.bottom

        canvas.drawText(_text!!, x, y, textPaint)

        if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
            canvas.scale(1f, 1f)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        val lastEnabled = isEnabled
        super.setEnabled(enabled)

        if (lastEnabled == enabled) {
            return
        }

        if (animationType == AnimatedBottomBar.BadgeAnimation.NONE) {
            visibility = if (enabled) VISIBLE else GONE
            return
        }

        animator = if (enabled) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
        animator?.duration = _animationDuration.toLong()
        animator?.addUpdateListener {
            if (!scaleLayout) {
                when (animationType) {
                    AnimatedBottomBar.BadgeAnimation.NONE -> {
                    }
                    AnimatedBottomBar.BadgeAnimation.SCALE -> {
                        scaleX = animator!!.animatedValue as Float
                        scaleY = animator!!.animatedValue as Float
                    }
                    AnimatedBottomBar.BadgeAnimation.FADE -> {
                        alpha = animator!!.animatedValue as Float
                    }
                }
            }

            requestLayout()
            postInvalidate()
        }
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isEnabled) {
                    visibility = GONE
                }

                when (animationType) {
                    AnimatedBottomBar.BadgeAnimation.NONE -> {
                    }
                    AnimatedBottomBar.BadgeAnimation.SCALE -> {
                        scaleX = 1f
                        scaleY = 1f
                    }
                    AnimatedBottomBar.BadgeAnimation.FADE -> {
                        alpha = 1f
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                visibility = VISIBLE
            }
        })
        animator?.start()
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        _backgroundColor = color
        updatePaint()
    }

    private val fraction: Float
        get() {
            var fraction = 1f

            if (scaleLayout) {
                fraction = if (animator!!.isRunning) {
                    animator!!.animatedValue as Float
                } else {
                    if (isEnabled) 1f else 0f
                }
            }

            return fraction
        }
}