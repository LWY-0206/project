package com.example.loding.app

import android.util.Log
import com.example.corekit.common.BaseApplication
import com.example.corekit.http.HttpManager
import com.jxdx.classroom.ClassModuleInitializer
import com.jxdx.home.HomeModuleInitializer
import com.jxdx.login.LoginModuleInitializer
import com.jxdx.mine.service.MineModuleInitializer
import com.jxdx.resource.ResourceModuleInitializer
import com.jxdx.square.SquareModuleInitializer



class MyApp : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        Log.d("---MyApp", "MyApp.onCreate() 执行了")
        // 初始化模块，注册服务
        LoginModuleInitializer.init()
        MineModuleInitializer.init()
        HomeModuleInitializer.init()
        ResourceModuleInitializer.init()
        SquareModuleInitializer.init()
        ClassModuleInitializer.init()
        with(HttpManager.Builder()){
            baseUrl(BASE_URL)
            this.timeout(25)
            HttpManager.init(this)
        }
    }
    companion object {
        private const val BASE_URL = "http://121.41.176.238:8080/"
        private const val ICON_FONT = "iconfont.ttf" // 字体文件路径
    }

    override fun httpBaseUrl(): String = BASE_URL

    override fun iconFontPath(): String = ICON_FONT

}