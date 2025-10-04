package com.jxdx.square.friend

import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.UserInfo
import com.jxdx.square.entity.ApplicationMessage
import com.jxdx.square.entity.Friend
import com.jxdx.square.friend.ApplicationBody
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @GET("/friend/applications")
    suspend fun getApplications(
        @Header("satoken") source: String,
    ): BaseResp<List<ApplicationMessage>>

    @POST("/friend/accept/{applicationId}")
    suspend fun acceptFriend(
        @Header("satoken") source: String,
        @Path("applicationId") applicationId: Int,
    ): BaseResp<String>

    @POST("/friend/reject/{applicationId}")
    suspend fun rejectFriend(
        @Header("satoken") source: String,
        @Path("applicationId") applicationId: Int,
    ): BaseResp<String>

    @DELETE("/friend/{friendId}")
    suspend fun deleteFriend(
        @Header("satoken") source: String,
        @Path("friendId") friendId: Int,
    ): BaseResp<String>
}
