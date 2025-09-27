package com.jxdx.classroom.http

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.AllCourse
import com.jxdx.classroom.UserInfo
import com.jxdx.classroom.entity.ClassLive
import com.jxdx.classroom.entity.SelectClass
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    //获取当前用户信息
    @GET("/user/info")
    fun getUserInfo(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>

    //查看所有课程
    @GET("/api/student/courses/list")
    fun getAllCourse(
        @Header("satoken")satoken: String= TokenManager.getToken()?:""
    ): Call<BaseResp<List<AllCourse>>>

    @GET("/live/room/student/page")
    suspend fun getClassRoom(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): BaseResp<ArrayList<ClassLive>>

    @GET("/api/teacher/subject")
    suspend fun getSubject(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): BaseResp<ArrayList<SelectClass>>
}