package com.jxdx.classroom.http

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.com.jxdx.classroom.entity.ClassLive
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("/user/info")
    fun getUserInfo(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>

    @GET("/live/room/student/page")
    suspend fun getClassRoom(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): BaseResp<ArrayList<ClassLive>>
}