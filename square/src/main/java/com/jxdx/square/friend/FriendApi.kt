package com.jxdx.square.friend

import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.UserInfo
import com.jxdx.square.entity.ApplicationMessage
import com.jxdx.square.entity.Friend
import com.jxdx.square.friend.ApplicationBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendApi {
    @GET("/com/jxdx/square/friend/list")
    suspend fun getFriends(
        @Header("satoken") source: String,
    ): BaseResp<List<Friend>>

    @GET("/user/info/{userId}")
    suspend fun getUserInfo(
        @Header("satoken") source: String,
        @Path("userId") userId: String,
    ): BaseResp<UserInfo>

    @POST("/com/jxdx/square/friend/add")
    suspend fun addFriend(
        @Header("satoken") source: String,
        @Body applicationBody: ApplicationBody,
    ): BaseResp<String>

    @GET("/com/jxdx/square/friend/applications")
    suspend fun getApplications(
        @Header("satoken") source: String,
    ): BaseResp<List<ApplicationMessage>>
}
