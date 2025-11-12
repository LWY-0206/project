package com.jxdx.square.entity

import com.example.corekit.recyclerview.MultipleType

data class ApplicationMessage(
    val id: Int,
    // 申请ID
    val applicationId: Int,
    // 申请人用户ID
    val applicantId: Int,
    // 用户头像URL
    val applicantAvatar: String,
    // 用户名
    var applicantName: String,
    // 时间
    var createTime: String,
    // 说明
    var remark: String,
    // 状态：0-待确认，1-已确认，2-已拒绝
    val status: Int,
) : MultipleType {
    override fun viewType(): Int = 1
}
