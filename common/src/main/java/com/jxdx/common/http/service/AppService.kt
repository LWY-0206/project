package org.jxxy.debug.http.service

import android.content.Context
import com.example.corekit.common.CommonServiceManager


interface AppService{
    fun getAppContext() : Context

    fun goToMainActivity(context: Context?)

    fun gotoMembershipActivity(context: Context?)
}

fun getAppContext() : Context? = CommonServiceManager.service<AppService>()?.getAppContext()

fun goToMainActivity(context: Context?){
    CommonServiceManager.service<AppService>()?.goToMainActivity(context)
}
fun gotoMembershipActivity(context: Context?){
    CommonServiceManager.service<AppService>()?.gotoMembershipActivity(context)
}