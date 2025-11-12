package com.jxdx.common.utilimport
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager

class SoftInputUtil {

    // 软键盘高度
    private var softInputHeight = 0
    private var softInputHeightChanged = false

    // 导航栏是否显示
    private var isNavigationBarShow = false
    private var navigationHeight = 0

    // 任意需要调整高度的视图
    private var anyView: View? = null
    // 软键盘状态改变监听器
    private var listener: ISoftInputChanged? = null
    // 软键盘是否显示
    private var isSoftInputShowing = false

    // 软键盘状态改变接口
    interface ISoftInputChanged {
        fun onChanged(isSoftInputShow: Boolean, softInputHeight: Int, viewOffset: Int)
    }

    // 附加软键盘监听器
    fun attachSoftInput(anyView: View?, listener: ISoftInputChanged?) {
        if (anyView == null || listener == null) return

        val rootView = anyView.rootView ?: return

        navigationHeight = getNavigationBarHeight(anyView.context)

        this.anyView = anyView
        this.listener = listener

        rootView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val rootHeight = rootView.height
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            if (rootHeight - rect.bottom == navigationHeight) {
                isNavigationBarShow = true
            } else if (rootHeight - rect.bottom == 0) {
                isNavigationBarShow = false
            }

            val isSoftInputShow: Boolean
            val mutableHeight = if (isNavigationBarShow) navigationHeight else 0
            if (rootHeight - mutableHeight > rect.bottom) {
                isSoftInputShow = true
                val softInputHeight = rootHeight - mutableHeight - rect.bottom
                if (this.softInputHeight != softInputHeight) {
                    softInputHeightChanged = true
                    this.softInputHeight = softInputHeight
                } else {
                    softInputHeightChanged = false
                }
            } else {
                isSoftInputShow = false
                softInputHeightChanged = false
            }

            val location = IntArray(2)
            anyView.getLocationOnScreen(location)

            if (isSoftInputShowing != isSoftInputShow || (isSoftInputShow && softInputHeightChanged)) {
                listener.onChanged(
                    isSoftInputShow,
                    softInputHeight,
                    location[1] + anyView.height - rect.bottom
                )
                isSoftInputShowing = isSoftInputShow
            }
        }
    }

    companion object {
        // 获取导航栏高度
        fun getNavigationBarHeight(context: Context?): Int {
            if (context == null) return 0
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return resources.getDimensionPixelSize(resourceId)
        }

        // 显示软键盘
        fun showSoftInput(view: View?) {
            view ?: return
            val inputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, 0)
        }

        // 隐藏软键盘
        fun hideSoftInput(view: View?) {
            view ?: return
            val inputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
