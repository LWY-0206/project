package com.jxdx.resource

import android.util.Log
import com.jxdx.common.http.service.ResourceService
import com.jxdx.common.http.service.ServiceRegistry
import com.jxdx.resource.resource.Resource

object ResourceModuleInitializer {
    fun init() {
        var service = ServiceRegistry.register(ResourceService::class.java, ResourceFragmentServiceImpl())
        if (service!=null) {
            Log.d("---ResourceModule", "ResourceFragmentServiceImpl 已注册")
        }else{
            Log.d("---ResourceModule", "ResourceFragmentServiceImpl 未注册")

        }
    }
}