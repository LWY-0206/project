package org.jxxy.debug.h5.utils

import android.content.Context
import com.example.corekit.util.ResourceUtil
import com.lxj.xpopup.util.XPopupUtils

fun composeImage(context: Context, res: Int): Pair<Int, Int>? {
    val image = ResourceUtil.getDrawable(res)
    image?.let {
        val mWidth: Int = XPopupUtils.getScreenWidth(context)
        val mHeight: Int = image.intrinsicWidth / mWidth * image.intrinsicHeight
        return Pair(mWidth, mHeight)
    }
    return null
}