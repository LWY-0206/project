package org.jxxy.debug.widget

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class FlexLayoutManager : RecyclerView.LayoutManager() {
    companion object {
        const val LINE_NO_LIMIT = 0
    }

    var listener: LayoutListener? = null
    var maxLines = LINE_NO_LIMIT
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun isAutoMeasureEnabled(): Boolean = true
    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        detachAndScrapAttachedViews(recycler)
        val maxWidth = width - paddingLeft
        var lineStart = paddingStart
        var lineTop = paddingTop
        var lineMaxHeight = 0
        var lineCount = 1
        var lastChildWidth = 0
        var overPosition = -1
        for (i in 0 until itemCount) {
            val child = recycler.getViewForPosition(i)
            val lp = child.layoutParams as RecyclerView.LayoutParams

            measureChildWithMargins(child, 0, 0)
            val childWidth = getDecoratedMeasuredWidth(child) + lp.leftMargin + lp.rightMargin
            val childHeight = getDecoratedMeasuredHeight(child) + lp.topMargin + lp.bottomMargin
            lastChildWidth = childWidth
            val right = lineStart + lastChildWidth
            if (right <= maxWidth) {
                // 不需要换行
                addView(child)
                layoutDecoratedWithMargins(child, lineStart + lp.leftMargin, lineTop + lp.topMargin, right, lineTop + childHeight)
                lineStart = right
                lineMaxHeight = max(lineMaxHeight, childHeight)
            } else if (maxLines == LINE_NO_LIMIT || lineCount < maxLines) {
                // 换行
                lineTop += lineMaxHeight
                addView(child)
                layoutDecoratedWithMargins(child, paddingStart + lp.leftMargin, lineTop + lp.topMargin, paddingStart + childWidth, lineTop + childHeight)
                lineStart = childWidth
                lineMaxHeight = childHeight
                lineCount++
            } else {
                overPosition = i
                // 溢出的会经历getViewForPosition里进行re-bind or create，所以需要recycle
                recycler.recycleView(child)
                // 达到最大行号
                break
            }
        }
        if (overPosition != -1) {
            listener?.onOverFlowLines(lastChildWidth, overPosition - 1)
        }
    }

    interface LayoutListener {
        fun onOverFlowLines(lastChildWidth: Int, position: Int)
    }
}
