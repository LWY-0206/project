package org.jxxy.debug.http.service

import android.content.Context
import com.example.corekit.common.CommonServiceManager


interface LoginService {

    //先公用一个是否登录的函数吧
    fun isLogin() : Boolean

    fun goLogin(context: Context)

    fun goSetPassword(context: Context)


}

//直接在这拿到由 login 模块中实现的 LoginService 接口并使用 @AutoService 注解注册的实现类 LoginServiceImpl
fun isLogin() : Boolean = CommonServiceManager.service<LoginService>()?.isLogin() ?: false
//调用 CommonServiceManager 的 service 方法，传入 LoginService 类型，试图获取 LoginService 的实现实例
//调用该实例的 isLogin 方法
//最后做判空处理

fun goLogin(context: Context){
    CommonServiceManager.service<LoginService>()?.goLogin(context)
}
fun goSetPassword(context: Context){
    CommonServiceManager.service<LoginService>()?.goSetPassword(context)
}


inline fun loginCheck(context: Context,ifLoginUnit: () -> Unit){
    if(isLogin()){
        ifLoginUnit()
    }else{
        goLogin(context)
    }
}

