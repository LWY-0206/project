package com.jxdx.square

import android.util.Log
import com.jxdx.common.http.service.HomeService
import com.jxdx.common.http.service.ServiceRegistry
import com.jxdx.common.http.service.SquareService

object SquareModuleInitializer {
    fun init() {
        var service = ServiceRegistry.register(SquareService::class.java, SquareServiceImpl())
        if (service != null) {
            Log.d("---Square", "SquareServiceImpl 已注册")
        }else{
            Log.d("---Square", "SquareServiceImpl 未注册")

        }
    }
}