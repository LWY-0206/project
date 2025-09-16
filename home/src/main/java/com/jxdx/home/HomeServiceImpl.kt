package com.jxdx.home

import android.content.Context
import android.content.Intent
import com.jxdx.common.http.service.HomeService
class HomeServiceImpl: HomeService {
    override fun navigateToHome(context: Context) {
        val intent = Intent(context, Home::class.java)
        context.startActivity(intent)
    }
}