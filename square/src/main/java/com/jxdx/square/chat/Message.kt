
package com.jxdx.square.chat

import com.example.corekit.recyclerview.MultipleType
import java.text.SimpleDateFormat
import java.util.Locale

// 实现MultipleType接口，用于适配器多类型展示
data class Message(
    val id: String,
    val content: String,
    val type: Int,
    val time: Long,
    val senderId: Int,
    val receiverId: Int,
    val avatarUrl: String,
) : MultipleType {
    override fun viewType(): Int = type

    companion object {
        // 消息类型
        const val TYPE_SEND = 1 // 发送的消息（右侧）
        const val TYPE_RECEIVE = 2 // 接收的消息（左侧）
        
        /**
         * 从ChatHistoryMessage转换为Message
         */
        fun fromHistoryMessage(historyMessage: ChatHistoryMessage): Message {
            return Message(
                id = historyMessage.id.toString(),
                content = historyMessage.content,
                type = if (historyMessage.isSentByCurrentUser) TYPE_SEND else TYPE_RECEIVE,
                time = parseTimeString(historyMessage.sendTime),
                senderId = historyMessage.fromUserId,
                receiverId = historyMessage.toUserId,
                avatarUrl = if (historyMessage.isSentByCurrentUser) {
                    historyMessage.fromUserAvatar
                } else {
                    historyMessage.fromUserAvatar
                }
            )
        }
        
        /**
         * 解析时间字符串为时间戳
         */
        private fun parseTimeString(timeString: String): Long {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.parse(timeString)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}