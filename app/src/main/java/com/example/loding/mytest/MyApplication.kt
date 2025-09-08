package com.example.loding.mytest

import com.example.corekit.common.BaseApplication

// 遇到网络接口不会的就来看
class MyApplication : BaseApplication() {
    companion object {
        private const val BASE_URL = "121.41.176.238:8080/"
        private const val ICON_FONT = "iconfont.ttf" // 字体文件路径
    }

    // 提供网络请求的基础URL
    override fun httpBaseUrl(): String = BASE_URL

    // 提供字体文件路径（如需使用自定义字体）
    override fun iconFontPath(): String = ICON_FONT
}
