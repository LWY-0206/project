@file:Suppress("ktlint:standard:filename")
package com.jxdx.square.entity

import com.example.corekit.recyclerview.MultipleType
import kotlin.collections.List
// "data": [
// {
//    "userId": 1005,
//    "userName": "张三6",
//    "content": "大家觉得这个怎么样？",
//    "likeCount": 23,
//    "commentCount": 7,
//    "rewardCount": 3,
//    "avatarUrl": "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/20250905/6sXJ/1024X768/1667459511305837.jpg",
//    "contentImageUrls": [
//    "https://oss.example.com/posts/question1.jpg"
//    ],
//    "title": "征求意见",
//    "updateTime": "2025-09-05 16:33:35",
//    "createTime": "2025-09-05 16:33:40"
// },

data class Dynamic(
    val id: Int,
    // 用户id
    val userId: Int,
    // 用户头像资源id
    val avatarUrl: String,
    // 用户名
    val userName: String,
    // 发布时间
    val createTime: String,
    // 动态内容
    val content: String,
    // 图片资源列表（最多3张）
    val contentImageUrls: List<String>,
    // 点赞数
    var likeCount: Int,
    // 打赏数
    var rewardCount: Int,
    // 评论数
    var commentCount: Int,
    // 第一条评论预览（可为空）
    val title: String?,
    val updateTime: String?,
//    // 完整评论列表（默认为空列表）
//    val comments: List<CommentItem> = emptyList(),
    // 是否点赞
    val isLiked: Boolean,
) : MultipleType {
    // 固定返回图文混排的类型标识（与Adapter中定义的TYPE_IMAGE_TEXT对应）
    override fun viewType(): Int = 3
}
