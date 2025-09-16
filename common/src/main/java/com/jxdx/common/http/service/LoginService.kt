package com.jxdx.common.http.service

import android.content.Context

interface LoginService {
    fun login(username: String, password: String): Boolean
    fun navigateToLogin(context: Context)
}
