package com.jxdx.square.entity

data class DynamicDetail(
    // 帖子ID
    val id: Int,
    // 用户ID
    val userId: Int,
    // 用户名
    val userName: String,
    // 头像URL
    val avatarUrl: String,
    // 帖子标题
    val title: String,
    // 帖子内容
    val content: String,
    // 内容图片URL列表
    val contentImageUrls: List<String>,
    // 点赞数
    val likeCount: Int,
    // 评论数
    val commentCount: Int,
    // 打赏数
    val rewardCount: Int,
    // 创建时间
    val createTime: String,
    // 更新时间
    val updateTime: String,
)
