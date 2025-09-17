package com.jxdx.resource.famousChat

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FamousChatApi {
    @GET("/api/celebrityChat/init")
    suspend fun initChatSession(
        @Query("celebrityId") celebrityId: Int,
        @Header("satoken") satoken: String = "363a547e-4f76-4da9-9e56-cff752475e00"
    ): BaseResp<ChatSession>

    @POST("/api/celebrityChat/chat")
    suspend fun sendMessage(
        @Body request: SendMessageRequest,
        @Header("satoken") satoken: String = "363a547e-4f76-4da9-9e56-cff752475e00"
    ): BaseResp<ChatResponse>
}