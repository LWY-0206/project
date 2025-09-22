package com.jxdx.login.Login

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.login.LoginResponse
import com.jxdx.login.UserInfo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("/user/login")
    fun login(
        @Body loginRequest: LoginRequest,
    ): Call<BaseResp<LoginResponse>>
    @POST("/user/register")
    fun register(
        @Body registerRequest: RegisterRequest,
    ): Call<BaseResp<UserInfo>>
    @Multipart
    @POST("/common/upload")
    fun uploadAvatar(
        @Part fileList: MultipartBody.Part,
    ): Call<BaseResp<List<String>>>
    @GET("/user/info/{userId}")
    fun getUserInfo(@Path("userId") userId: Int,
                    @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>
    @GET("/user/info")
    fun getUserInfo(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>
    @POST("/user/logout")
    fun logout():Call<BaseResp<Any>>
}
