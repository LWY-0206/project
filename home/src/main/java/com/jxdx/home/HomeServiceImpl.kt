package com.jxdx.home

import android.content.Context
import android.content.Intent
import com.jxdx.common.http.service.HomeService
class HomeServiceImpl: HomeService {
    override fun navigateToHome(context: Context) {
        val intent = Intent(context, HomeActivity::class.java)
        context.startActivity(intent)
    }

    override fun navigateToTeacherHome(context: Context) {
        var intent = Intent(context, TeacherHomeActivity::class.java)
        context.startActivity(intent)
    }
}