package com.jxdx.classroom


import android.util.Log
import com.jxdx.common.http.service.ClassService
import com.jxdx.common.http.service.ServiceRegistry


object ClassModuleInitializer {
    fun init() {
        var service = ServiceRegistry.register(ClassService::class.java, ClassServiceImpl())
        if (service != null) {
            Log.d("---ResourceModule", "ClassServiceImpl 已注册")
        } else {
            Log.d("---ResourceModule", "ClassServiceImpl 未注册")

        }

    }
}
