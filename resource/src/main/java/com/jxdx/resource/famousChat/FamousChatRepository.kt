package com.jxdx.resource.famousChat

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class FamousChatRepository {
    private val service: FamousChatApi by lazy {
        HttpManager.instance.service(FamousChatApi::class.java)
    }

    suspend fun initChatSession(celebrityId: Int): BaseResp<ChatResponse> {
        Log.d("ChatRepository", "初始化聊天会话: celebrityId=$celebrityId")
        return service.initChatSession(celebrityId)
    }

    suspend fun sendMessage( message: String, celebrityId: Int): BaseResp<ChatResponse> {
        Log.d("ChatRepository", "发送消息:message=$message")
        val request = SendMessageRequest(
            content = message,
            celebrityId = celebrityId
        )
        Log.d("ChatRepository", "发送消息:request=$request")
        return service.sendMessage( request)
    }
}