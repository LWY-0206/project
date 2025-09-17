package com.jxdx.square.entity

import com.example.corekit.recyclerview.MultipleType

data class MessageItem(
    // 头像资源ID，也可以是网络图片URL
    val avatarResId: Int,
    // 名称
    val name: String,
    // 时间
    val time: String,
    // 最近一条消息
    val message: String,
) : MultipleType {
    override fun viewType(): Int = 3
}
