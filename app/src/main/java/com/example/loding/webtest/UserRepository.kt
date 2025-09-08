package com.example.loding.webtest

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class UserRepository {
    private val service: UserApi by lazy {
        HttpManager.instance.service(UserApi::class.java)
        // 这一参数的作用是让 Retrofit 根据接口类的定义（如 @GET 注解、方法参数等）生成该接口的实现类实例，从而通过实例调用接口中的网络请求方法（如 getQuestions）
    }

    suspend fun getUsers(
        type: Int,
        a: Int,
        b: Int,
    ): BaseResp<UserData> = service.getUsers(type, a, b)
}
