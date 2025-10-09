package com.jxdx.mine.http.vo

//已发布作业详情VO
data class TeachCreateHWDetailVO(
    val homeworkId: Long? = null,
    val subject: String? = null, // 科目，可能为null
    val homeworkName: String? = null,
    val homeworkContent: String? = null,
    val sendTime: String? = null,
    val deadTime: String? = null,
    val imageUrls: List<String>? = null
)