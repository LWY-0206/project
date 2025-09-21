package com.jxdx.resource.famousChat

import android.R

data class ChatInitResponse(
    val code: Int,
    val message: String,
    val data: ChatSession
)

data class ChatSession(
    val sessionId: String,
    val celebrityName: String,
    val avatarUrl: String
)

data class ChatMessage(
    val messageId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val avatarUrl: String? = null
)

data class SendMessageRequest(
    val content: String,
    val celebrityId: Int
)

data class ChatResponse(
    val code: Int,
    val message: String,
    val data: ChatContent
)

data class ChatContent(
    val content: String
)