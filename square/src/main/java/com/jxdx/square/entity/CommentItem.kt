package com.jxdx.square.entity

import com.example.corekit.recyclerview.MultipleType

data class CommentItem(
    // 评论人名称
    val commentUserName: String,
    // 用户头像URL
    var avatarUrl: String,
    // 评论内容
    var content: String,
    // 点赞数
    var likeCount: Int,
    // 是否已点赞
    var isLiked: Boolean = false,
    // 时间
    var createTime: String,
) : MultipleType {
    // 固定返回图文混排的类型标识（与Adapter中定义的TYPE_IMAGE_TEXT对应）
    override fun viewType(): Int = 3
}
