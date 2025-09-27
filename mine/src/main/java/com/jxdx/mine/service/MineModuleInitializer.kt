package com.jxdx.mine.service

import android.util.Log
import com.jxdx.common.http.service.MineService
import com.jxdx.common.http.service.ServiceRegistry

object MineModuleInitializer {
    fun init(){
        var service = ServiceRegistry.register(MineService::class.java, MineServiceImpl())
        if (service!=null) {
            Log.d("---MineModule", "MineFragmentServiceImpl 已注册")
        }else{
            Log.d("---MineModule", "MineFragmentServiceImpl 未注册")
        }
    }
}