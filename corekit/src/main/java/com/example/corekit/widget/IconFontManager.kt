package com.example.corekit.widget

import android.graphics.Typeface
import com.example.corekit.common.BaseApplication

object IconFontManager {
    var iconAsset: Typeface? = null
        private set

    fun initAsset(path: String) {
        iconAsset = Typeface.createFromAsset(
            BaseApplication.context().assets,
            path
        )
    }
}
