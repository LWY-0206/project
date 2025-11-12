package com.jxdx.square.entity

import com.example.corekit.recyclerview.MultipleType

data class LikeMessageItem(
    // 点赞用户ID
    val userId: Int,
    // 点赞用户名称
    val userName: String,
    // 用户头像资源ID
    val avatarResId: Int = 0,
    // 用户头像URL（网络图片）
    val avatarUrl: String? = null,
    // 点赞内容描述（如："点赞了你的评论"）
    val content: String,
    // 点赞时间
    val time: String,
    // 被点赞的动态ID
    val dynamicId: Int? = null,
    // 被点赞的评论ID（如果是评论点赞）
    val commentId: Int? = null
) : MultipleType {
    override fun viewType(): Int = 4
}