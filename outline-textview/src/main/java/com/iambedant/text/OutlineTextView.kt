package com.iambedant.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.ceil


/**
 * 带有文字描边效果的 TextView
 * 考虑了描边宽度对控件尺寸的影响 @KUMO
 */
class OutlineTextView : AppCompatTextView {

    private val defaultStrokeWidth = 0F
    private var isDrawing: Boolean = false

    private var strokeColor: Int = 0
    private var strokeWidth: Float = 0.toFloat()

    private var offsets = arrayOf<Pair<Float, Float>>()
    private val textBounds = Rect()

    constructor(context: Context) : super(context) {
        initResources(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initResources(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initResources(context, attrs)
    }

    private fun initResources(context: Context?, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context?.obtainStyledAttributes(attrs, R.styleable.outlineAttrs)
            strokeColor = a!!.getColor(
                R.styleable.outlineAttrs_outlineColor,
                currentTextColor)
            strokeWidth = a.getFloat(R.styleable.outlineAttrs_outlineWidth,
                defaultStrokeWidth)

            a.recycle()
        } else {
            strokeColor = currentTextColor
            strokeWidth = defaultStrokeWidth
        }
        setStrokeWidth(strokeWidth)
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
        invalidate()
    }

    /**
     *  给定 sp 值设置描边宽度
     */
    fun setStrokeWidth(width: Float) {
        strokeWidth = width.toPX()
        buildOffset()
        updatePadding()
        requestLayout()
        invalidate()
    }

    private fun buildOffset() {
        // 使用多次绘制来增强描边效果
        // 在不同方向上偏移绘制，形成完整的描边
        offsets = arrayOf(
            Pair(-strokeWidth, -strokeWidth),
            Pair(0f, -strokeWidth),
            Pair(strokeWidth, -strokeWidth),
            Pair(-strokeWidth, 0f),
            Pair(strokeWidth, 0f),
            Pair(-strokeWidth, strokeWidth),
            Pair(0f, strokeWidth),
            Pair(strokeWidth, strokeWidth)
        )
    }

    /**
     * 更新内边距，确保描边不会被裁剪
     */
    private fun updatePadding() {
        // 计算需要额外添加的内边距，确保描边完全显示
        val extraPadding = ceil(strokeWidth).toInt()
        
        // 保留原有内边距，并添加额外的内边距用于描边
        setPadding(
            paddingLeft + extraPadding,
            paddingTop + extraPadding,
            paddingRight + extraPadding,
            paddingBottom + extraPadding
        )
    }

    fun setStrokeWidth(unit: Int, width: Float) {
        strokeWidth = TypedValue.applyDimension(
            unit, width, context.resources.displayMetrics)
        buildOffset()
        updatePadding()
        requestLayout()
        invalidate()
    }

    override fun invalidate() {
        if (isDrawing) return
        super.invalidate()
    }

    /**
     * 重写测量方法，考虑描边宽度对尺寸的影响
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        if (strokeWidth > 0) {
            // 计算描边需要的额外空间
            val extraWidth = ceil(strokeWidth * 2).toInt()
            val extraHeight = ceil(strokeWidth * 2).toInt()
            
            // 设置新的测量尺寸，包含描边所需的额外空间
            setMeasuredDimension(
                measuredWidth + extraWidth,
                measuredHeight + extraHeight
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (strokeWidth > 0) {
            isDrawing = true
            
            // 保存原始文本颜色和画笔样式
            val originalTextColor = currentTextColor
            val originalPaintStyle = paint.style
            val originalStrokeWidth = paint.strokeWidth
            
            // 设置描边效果
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            setTextColor(strokeColor)
            
            // 保存画布状态
            canvas.save()
            
            // 绘制描边
            for ((dx, dy) in offsets) {
                canvas.translate(dx, dy)
                super.onDraw(canvas)
                canvas.translate(-dx, -dy)
            }
            
            // 恢复画布状态
            canvas.restore()
            
            // 绘制原始文本
            paint.style = Paint.Style.FILL
            setTextColor(originalTextColor)
            super.onDraw(canvas)
            
            // 恢复原始画笔状态
            paint.style = originalPaintStyle
            paint.strokeWidth = originalStrokeWidth
            
            isDrawing = false
        } else {
            super.onDraw(canvas)
        }
    }
}
