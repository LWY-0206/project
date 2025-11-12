package com.jxdx.home

import android.util.Log
import com.jxdx.common.http.service.HomeService
import com.jxdx.common.http.service.ServiceRegistry

object HomeModuleInitializer {
    fun init() {
        var service = ServiceRegistry.register(HomeService::class.java, HomeServiceImpl())
        if (service != null) {
            Log.d("---HomeModule", "HomeServiceImpl 已注册")
        }else{
            Log.d("---HomeModule", "HomeServiceImpl 未注册")

        }
    }
}