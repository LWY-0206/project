package com.jxdx.mine.http

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.AllCourse
import com.jxdx.mine.CourseDetail
import com.jxdx.mine.Homework
import com.jxdx.mine.PageData
import com.jxdx.mine.UserInfo
import retrofit2.Call
import retrofit2.http.Body

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //根据ID获取用户信息
    @GET("/user/info/{userId}")
    fun getUserInfo(@Path("userId") userId: Int): Call<BaseResp<UserInfo>>
    @GET("/user/info")
    fun getUserInfo(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>
    @POST("/user/updateProfile")
    fun updateProfile(
        @Body profile: String,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>

    @GET("/api/stu/homework")
    fun getHomework(
        @Query ("page") page: Int,
        @Query ("size") size: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<PageData<Homework>>>

    ////查看所有课程
    @GET("/api/student/courses/list")
    fun getAllCourse(
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<List<AllCourse>>>

    //获取课程详情
    @GET("/api/student/courses/detail")
    fun getCourseDetail(
        @Query("subjectId") subjectId: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<CourseDetail>>
}




































