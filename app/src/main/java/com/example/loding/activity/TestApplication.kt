package com.example.loding.activity

import com.example.corekit.common.BaseApplication

class TestApplication : BaseApplication() {
    companion object {
        private const val BASE_URL = "http://121.41.176.238:8080/"
        private const val ICON_FONT = "iconfont.ttf" // 字体文件路径
    }

    override fun httpBaseUrl(): String = BASE_URL

    override fun iconFontPath(): String = ICON_FONT
}