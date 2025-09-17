package com.jxdx.resource.famousChat

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
class FamousChatRepository {
    private val service: FamousChatApi by lazy {
        HttpManager.instance.service(FamousChatApi::class.java)
    }

    suspend fun initChatSession(celebrityId: Int): BaseResp<ChatSession> {
        Log.d("ChatRepository", "初始化聊天会话: celebrityId=$celebrityId")
        return service.initChatSession(celebrityId)
    }

    suspend fun sendMessage(sessionId: String, message: String, celebrityId: Int): BaseResp<ChatResponse> {
        Log.d("ChatRepository", "发送消息: sessionId=$sessionId, message=$message")
        val request = SendMessageRequest(sessionId, message, celebrityId)
        return service.sendMessage(request)
    }
}