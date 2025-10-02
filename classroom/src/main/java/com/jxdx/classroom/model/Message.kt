package com.jxdx.classroom.model

/**
 * 消息模型
 */
data class Message(
    val id: String,           // 消息ID
    val content: String,      // 消息内容
    val senderId: String,     // 发送者ID
    val senderName: String,   // 发送者姓名
    val senderType: SenderType, // 发送者类型
    val messageType: MessageType, // 消息类型
    val timestamp: Long,      // 时间戳
    val imageUrl: String? = null, // 图片URL（如果是图片消息）
    val isRead: Boolean = false // 是否已读
)

/**
 * 发送者类型
 */
enum class SenderType {
    TEACHER,    // 教师
    STUDENT,    // 学生
    SYSTEM      // 系统
}

/**
 * 消息类型
 */
enum class MessageType {
    TEXT,           // 文本消息
    IMAGE,          // 图片消息
    QUESTION,       // 题目
    ANSWER,         // 答案
    WHITEBOARD,     // 白板快照
    SYSTEM          // 系统消息
}
