package nl.joery.animatedbottombar

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
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
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val textBounds = Rect()
    private val backgroundRoundRectBounds = RectF()
    private val horizontalPadding: Int = 6.dpPx

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener {
            if (!scaleLayout) {
                when (animationType) {
                    AnimatedBottomBar.BadgeAnimation.NONE -> {
                    }
                    AnimatedBottomBar.BadgeAnimation.SCALE -> {
                        val scale = it.animatedValue as Float
                        scaleX = scale
                        scaleY = scale
                    }
                    AnimatedBottomBar.BadgeAnimation.FADE -> {
                        alpha = it.animatedValue as Float
                    }
                }
            }

            requestLayout()
            postInvalidate()
        }

        addListener(object : Animator.AnimatorListener {
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
    }

    var animationType: AnimatedBottomBar.BadgeAnimation = AnimatedBottomBar.BadgeAnimation.SCALE

    var scaleLayout: Boolean = false

    private var _text: String? = null
    var text: String?
        get() = _text
        set(value) {
            _text = value

            updateTextBounds()
            invalidate()
        }

    private var _animationDuration: Int = 0
    var animationDuration: Int
        get() = _animationDuration
        set(value) {
            _animationDuration = value
            invalidate()
        }

    private var _backgroundColor: Int = Color.WHITE
    val backgroundColor: Int
        @ColorInt get() = _backgroundColor

    private var _textColor: Int = Color.WHITE
    var textColor: Int
        @ColorInt get() = _textColor
        set(@ColorInt value) {
            _textColor = value

            textPaint.color = value
            invalidate()
        }

    private var _textSize: Int = Color.WHITE
    var textSize: Int
        @Dimension get() = _textSize
        set(@Dimension value) {
            _textSize = value

            textPaint.textSize = value.toFloat()
            updateTextBounds()
            invalidate()
        }

    init {
        isEnabled = false
    }

    private fun updateTextBounds() {
        val text = _text ?: return

        textPaint.getTextBounds(text, 0, text.length, textBounds)
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        drawText(canvas)
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
            val middleX = ((measuredWidth + paddingLeft) / 2).toFloat()
            val middleY = ((measuredHeight + paddingTop) / 2).toFloat()

            var savepoint = 0
            if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
                savepoint = canvas.save()
                val fr = fraction
                canvas.scale(fr, fr, middleX, middleY)
            }

            canvas.drawCircle(
                    middleX,
                    middleY,
                    4.dpPx.toFloat(),
                    backgroundPaint
            )

            if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
                canvas.restoreToCount(savepoint)
            }
        } else {
            val radius = 8.dpPx.toFloat()

            backgroundRoundRectBounds.set(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (measuredWidth - paddingRight).toFloat(),
                (measuredHeight - paddingBottom).toFloat()
            )

            canvas.drawRoundRect(backgroundRoundRectBounds, radius, radius, backgroundPaint)
        }
    }

    private fun drawText(canvas: Canvas) {
        val text = _text ?: return

        val middleX = (measuredWidth + paddingLeft) / 2f
        val middleY = (measuredHeight + paddingTop) / 2f

        var savepoint = 0
        if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
            val fr = fraction
            savepoint = canvas.save()
            canvas.scale(fr, fr, middleX, middleY)
        }

        val rect = textBounds
        val x = middleX - rect.width() / 2 - rect.left
        val y = middleY + rect.height() / 2 - rect.bottom

        canvas.drawText(text, x, y, textPaint)

        if (animationType == AnimatedBottomBar.BadgeAnimation.SCALE) {
            canvas.restoreToCount(savepoint)
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

        animator.run {
            duration = _animationDuration.toLong()

            if(isEnabled) {
                start()
            } else {
                reverse()
            }
        }
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        _backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    private val fraction: Float
        get() {
            var fraction = 1f

            if (scaleLayout) {
                val a = animator
                fraction = if (a.isRunning) {
                    a.animatedValue as Float
                } else {
                    if (isEnabled) 1f else 0f
                }
            }

            return fraction
        }
}