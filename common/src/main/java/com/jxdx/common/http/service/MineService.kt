package org.jxxy.debug.http.service

import android.content.Context
import com.example.corekit.common.CommonServiceManager


interface MineService {
    fun goToStartActivity(context: Context)

    fun goVideoChat(context: Context)
}

fun goToStartActivity(context: Context){
    CommonServiceManager.service<MineService>()?.goToStartActivity(context)
}