package com.example.loding.app

import android.util.Log
import com.example.corekit.common.BaseApplication
import com.jxdx.home.HomeModuleInitializer
import com.jxdx.login.LoginModuleInitializer
import com.jxdx.mine.service.MineModuleInitializer
import com.jxdx.resource.ResourceModuleInitializer

class MyApp : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        Log.d("---MyApp", "MyApp.onCreate() 执行了")
        // 初始化模块，注册服务
        LoginModuleInitializer.init()
        MineModuleInitializer.init()
        HomeModuleInitializer.init()
        ResourceModuleInitializer.init()
    }
    companion object {
        private const val BASE_URL = "http://121.41.176.238:8080/"
        private const val ICON_FONT = "iconfont.ttf" // 字体文件路径
    }

    override fun httpBaseUrl(): String = BASE_URL

    override fun iconFontPath(): String = ICON_FONT
}