package com.jxdx.square.entity

import com.example.corekit.recyclerview.MultipleType

data class CommentMessageItem(
    // 评论用户ID
    val userId: Int,
    // 评论用户名称
    val userName: String,
    // 用户头像资源ID
    val avatarResId: Int = 0,
    // 用户头像URL（网络图片）
    val avatarUrl: String? = null,
    // 评论内容
    val content: String,
    // 评论时间
    val time: String,
    // 被评论的动态ID
    val dynamicId: Int,
    // 评论ID
    val commentId: Int? = null
) : MultipleType {
    override fun viewType(): Int = 5
}