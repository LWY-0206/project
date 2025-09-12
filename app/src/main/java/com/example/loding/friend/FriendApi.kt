package com.example.loding.friend

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.Friend
import retrofit2.http.GET
import retrofit2.http.Header

interface FriendApi {
    @GET("/friend/list")
    suspend fun getFriends(
        @Header("satoken") source: String,
    ): BaseResp<List<Friend>>
}
