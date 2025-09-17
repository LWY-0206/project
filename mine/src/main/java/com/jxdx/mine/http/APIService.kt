package com.jxdx.mine.http

import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.UserInfo
import retrofit2.Call

import retrofit2.http.GET

import retrofit2.http.Path

interface APIService {
    //根据ID获取用户信息
    @GET("/user/info/{userId}")
    fun getUserInfo(@Path("userId") userId: Int): Call<BaseResp<UserInfo>>
}




































