package com.jxdx.login.Login

import com.example.corekit.http.bean.BaseResp
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/user/login")
    fun login(@Body loginRequest: LoginRequest): Call<BaseResp<UserInfo>>
    @POST("/user/register")
    fun register(@Body registerRequest: RegisterRequest): Call<BaseResp<UserInfo>>
    @Multipart
    @POST("/common/upload")
    fun uploadAvatar(@Part fileList: MultipartBody.Part): Call<BaseResp<List<String>>>
}