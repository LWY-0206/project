package org.jxxy.debug.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class CenterLayoutManager : RecyclerView.LayoutManager() {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        val maxWidth = width + paddingLeft
        val list = ArrayList<Pair<View, Rect>>()
        var usedWidth = 0
        var usedHeight = paddingTop
        var maxHeight = 0
        for (i in 0 until itemCount) {
            val child = recycler.getViewForPosition(i)
            measureChildWithMargins(child, 0, 0)
            if (usedWidth + child.measuredWidth > maxWidth) {
                a(maxWidth, usedWidth, list)
                list.clear()
                usedHeight += maxHeight
                usedWidth = 0
                maxHeight = 0
            }
            list.add(Pair(child, Rect(usedWidth, usedHeight, usedWidth + child.measuredWidth, usedHeight + child.measuredHeight)))
            usedWidth += child.measuredWidth
            maxHeight = max(maxHeight, child.measuredHeight)
        }
        a(maxWidth, usedWidth, list)
    }
    fun a(maxWidth: Int, usedWidth: Int, list: ArrayList<Pair<View, Rect>>) {
        val offset = (maxWidth - usedWidth) / 2
        for ((index, pair) in list.withIndex()) {
            addView(pair.first)
            val r = pair.second
            layoutDecoratedWithMargins(pair.first, r.left + offset * 1, r.top, r.right + offset * 1, r.bottom)
        }
    }
    override fun isAutoMeasureEnabled(): Boolean = true
}
