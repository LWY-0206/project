package com.example.loding.app

import android.app.Application
import android.util.Log
import com.jxdx.home.HomeModuleInitializer
import com.jxdx.login.LoginModuleInitializer
import com.jxdx.mine.service.MineModuleInitializer

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("---MyApp", "MyApp.onCreate() 执行了")
        // 初始化模块，注册服务
        LoginModuleInitializer.init()
        MineModuleInitializer.init()
        HomeModuleInitializer.init()
    }
}