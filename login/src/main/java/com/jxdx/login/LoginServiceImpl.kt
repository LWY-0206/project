package com.jxdx.login

import android.content.Context
import android.content.Intent
import com.jxdx.common.http.service.LoginService
import com.jxdx.login.Login.LoginActivity


class LoginServiceImpl : LoginService {
    override fun login(username: String, password: String): Boolean {
        return username == "admin" && password == "123456"
    }

    override fun navigateToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
}