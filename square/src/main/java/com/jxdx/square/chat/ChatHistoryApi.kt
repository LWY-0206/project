package com.jxdx.square.chat

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 聊天历史记录API接口
 */
interface ChatHistoryApi {
    
    /**
     * 获取聊天历史记录
     * @param otherUserId 对方用户ID
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     */
    @GET("/api/userChat/history")
    suspend fun getChatHistory(
        @Query("otherUserId") otherUserId: Int,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): BaseResp<ChatHistoryResponse>
}

/**
 * 聊天历史记录响应数据
 */
data class ChatHistoryResponse(
    val messages: List<ChatHistoryMessage>
)

/**
 * 聊天历史记录消息数据模型
 */
data class ChatHistoryMessage(
    val id: Int,
    val fromUserId: Int,
    val fromUserName: String,
    val fromUserAvatar: String,
    val toUserId: Int,
    val toUserName: String,
    val toUserAvatar: String,
    val content: String,
    val messageType: Int,
    val status: Int,
    val sendTime: String,
    val isSentByCurrentUser: Boolean
)
