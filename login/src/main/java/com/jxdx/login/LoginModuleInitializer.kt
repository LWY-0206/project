package com.jxdx.login

import android.util.Log
import com.jxdx.common.http.service.LoginService
import com.jxdx.common.http.service.ServiceRegistry

object LoginModuleInitializer {
    fun init() {
        var service = ServiceRegistry.register(LoginService::class.java, LoginServiceImpl())
        if (service!=null) {
            Log.d("---LoginModule", "LoginServiceImpl 已注册")
        }else{
            Log.d("---LoginModule", "LoginServiceImpl 未注册")

        }
    }
}
