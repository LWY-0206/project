package org.jxxy.debug.util

import android.content.Context

object UiUtil {
    fun getStatusBarHeight(context: Context?): Int {
        if (context == null) {
            return 0
        }
        var statusBarHeight = 0
        try {
            val c = Class.forName("com.android.internal.R\$dimen")
            val obj = c.newInstance()
            val field = c.getField("status_bar_height")
            val x = field[obj].toString().toInt()
            statusBarHeight = context.resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return statusBarHeight
    }
}
