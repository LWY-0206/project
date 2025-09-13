package com.jxdx.login

import android.content.Context
import android.content.Intent
import com.example.corekit.http.TokenManager
import com.google.auto.service.AutoService
import org.jxxy.debug.http.service.LoginService
import kotlin.jvm.java


@AutoService(LoginService::class)
class LoginServiceImpl : LoginService {
    override fun isLogin(): Boolean {
        return TokenManager.getToken() != null
    }

    override fun goLogin(context: Context){
//        val intent = Intent(context, LoginActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//        context.startActivity(intent)
    }

    override fun goSetPassword(context: Context) {
//        context.startActivity(Intent(context,ChangePasswordActivity::class.java))
    }
}