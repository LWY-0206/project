package com.jxdx.classroom.group

import android.util.Log
import com.google.gson.Gson


/**
 * 广播工具类
 * 用于处理广播消息的发送和接收逻辑
 */
object BroadcastUtils {
    private const val TAG = "BroadcastUtils"
    private val gson = Gson()
    
    /**
     * 广播类型
     */
    object BroadcastType {
        const val MESSAGE = "message" // 广播消息
        const val QUESTION_TYPE = "question_type" // 广播题型
    }
    
    /**
     * 构建广播消息
     */
    fun buildBroadcastMessage(
        type: String,
        content: String,
        questionType: QuestionType? = null
    ): Message {
        // 创建广播数据
        val broadcastData = mapOf(
            "type" to type,
            "content" to content,
            "questionType" to questionType
        )
        
        // 转换为JSON字符串
        val jsonContent = gson.toJson(broadcastData)
        
        // 创建消息对象
        return Message(
            id = "broadcast_${System.currentTimeMillis()}",
            senderId = "teacher",
            senderName = "教师",
            content = jsonContent,
            timestamp = System.currentTimeMillis(),
            isFromTeacher = true
        )
    }
    
    /**
     * 解析接收到的广播消息
     */
    fun parseBroadcastMessage(message: String): BroadcastMessage? {
        return try {
            val parsed = gson.fromJson(message, BroadcastData::class.java)
            when (parsed.type) {
                BroadcastType.MESSAGE -> {
                    BroadcastMessage.MessageMessage(parsed.content ?: "")
                }
                BroadcastType.QUESTION_TYPE -> {
                    val questionTypeJson = gson.toJsonTree(parsed.questionType).asJsonObject
                    val questionType = gson.fromJson(questionTypeJson, QuestionType::class.java)
                    BroadcastMessage.QuestionTypeMessage(
                        parsed.content ?: "",
                        questionType
                    )
                }
                else -> {
                    Log.w(TAG, "未知的广播类型: ${parsed.type}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析广播消息失败: ${e.message}")
            null
        }
    }
    
    /**
     * 广播消息封装类
     */
    sealed class BroadcastMessage {
        data class MessageMessage(val content: String) : BroadcastMessage()
        data class QuestionTypeMessage(val content: String, val questionType: QuestionType) : BroadcastMessage()
    }
    
    /**
     * 内部数据类，用于解析JSON
     */
    private data class BroadcastData(
        val type: String?,
        val content: String?,
        val questionType: Map<String, Any>?
    )
}