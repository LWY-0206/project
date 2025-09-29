package com.jxdx.classroom.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * 自定义白板绘制View
 * 支持手绘、橡皮擦、清空等功能
 */
class WhiteboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 画笔
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = Color.BLACK
        strokeWidth = 5f
    }

    // 橡皮擦画笔
    private val eraserPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 20f
        color = Color.WHITE  // 使用白色来模拟橡皮擦效果
    }

    // 绘制路径
    private val path = Path()
    private val paths = mutableListOf<PathInfo>()
    
    // 触摸相关
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val touchTolerance = 4f

    // 绘制模式
    private var isEraserMode = false

    // 路径信息数据类
    private data class PathInfo(
        val path: Path,
        val paint: Paint,
        val isEraser: Boolean = false
    )

    /**
     * 设置画笔颜色
     */
    fun setPaintColor(color: Int) {
        paint.color = color
        isEraserMode = false
    }

    /**
     * 设置画笔粗细
     */
    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
    }

    /**
     * 切换到橡皮擦模式
     */
    fun setEraserMode(enabled: Boolean) {
        isEraserMode = enabled
        if (enabled) {
            // 橡皮擦模式下，使用固定的较大粗细
            eraserPaint.strokeWidth = 20f
        }
    }

    /**
     * 清空画板
     */
    fun clearCanvas() {
        paths.clear()
        invalidate()
    }

    /**
     * 获取画板内容为Bitmap
     */
    fun getCanvasBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制背景
        canvas.drawColor(Color.WHITE)
        
        // 绘制所有路径
        for (pathInfo in paths) {
            if (pathInfo.isEraser) {
                // 橡皮擦路径：绘制白色路径来"擦除"
                canvas.drawPath(pathInfo.path, pathInfo.paint)
            } else {
                canvas.drawPath(pathInfo.path, pathInfo.paint)
            }
        }
        
        // 绘制当前路径
        if (!path.isEmpty) {
            if (isEraserMode) {
                // 橡皮擦模式：绘制白色路径
                canvas.drawPath(path, eraserPaint)
            } else {
                canvas.drawPath(path, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        lastTouchX = x
        lastTouchY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - lastTouchX)
        val dy = abs(y - lastTouchY)
        
        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(
                lastTouchX,
                lastTouchY,
                (x + lastTouchX) / 2,
                (y + lastTouchY) / 2
            )
            lastTouchX = x
            lastTouchY = y
        }
    }

    private fun touchUp() {
        path.lineTo(lastTouchX, lastTouchY)
        
        // 保存当前路径
        val newPath = Path(path)
        val newPaint = if (isEraserMode) {
            Paint(eraserPaint)
        } else {
            Paint(paint)
        }
        paths.add(PathInfo(newPath, newPaint, isEraserMode))
        
        // 重置当前路径
        path.reset()
    }
}
