@file:Suppress("ktlint:standard:filename")

package com.jxdx.square.adapter

import com.example.corekit.common.BaseApplication

class MyApplication : BaseApplication() {
    companion object {
        private const val BASE_URL = "http://47.99.43.189:8056/"
        private const val ICON_FONT = "iconfont.ttf" // 字体文件路径
    }

    // 提供网络请求的基础URL
    override fun httpBaseUrl(): String = BASE_URL

    // 提供字体文件路径（如需使用自定义字体）
    override fun iconFontPath(): String = ICON_FONT
}
