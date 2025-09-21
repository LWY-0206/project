package com.jxdx.resource.famousChat

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface FamousChatApi {
    @GET("/api/celebrityChat/init")
    suspend fun initChatSession(
        @Query("celebrityId") celebrityId: Int,
        @Header("satoken") satoken: String = "e4bf8921-b19b-4933-a386-1dc82a5ea501"
    ): BaseResp<ChatResponse>
    @POST("/api/celebrityChat/chat")
    @Headers("Timeout-Second: 60")
    suspend fun sendMessage(
        @Body request: SendMessageRequest,
        @Header("satoken") satoken: String = "e4bf8921-b19b-4933-a386-1dc82a5ea501"
    ): BaseResp<ChatResponse>
}