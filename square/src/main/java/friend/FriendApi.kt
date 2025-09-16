package com.example.loding.friend

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.Friend
import com.jxdx.square.entity.UserInfo
import friend.ApplicationBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendApi {
    @GET("/friend/list")
    suspend fun getFriends(
        @Header("satoken") source: String,
    ): BaseResp<List<Friend>>

    @GET("/user/info/{userId}")
    suspend fun getUserInfo(
        @Header("satoken") source: String,
        @Path("userId") userId: String,
    ): BaseResp<UserInfo>

    @POST("/friend/add")
    suspend fun addFriend(
        @Header("satoken") source: String,
        @Body applicationBody: ApplicationBody,
    ): BaseResp<String>
}
