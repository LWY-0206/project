
package com.jxdx.square.chat

import com.example.corekit.recyclerview.MultipleType

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
    }
}