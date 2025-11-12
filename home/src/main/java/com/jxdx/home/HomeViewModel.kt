package com.jxdx.home


import android.util.Log
import com.jxdx.common.http.service.LoginService
import com.jxdx.common.http.service.ServiceRegistry


class HomeViewModel{
    fun checkLogin() {
        Log.d("---HomeViewModel", "开始检查登录")
        val loginService = ServiceRegistry.get(LoginService::class.java)
        Log.d("---HomeViewModel", "获取到的 loginService = $loginService")


        if (loginService == null) {
            Log.e("---HomeViewModel", "LoginService 未注册")
            return
        }

        val result = loginService.login("admin", "123456")
        Log.d("---HomeViewModel", "登录结果: $result")
    }
}