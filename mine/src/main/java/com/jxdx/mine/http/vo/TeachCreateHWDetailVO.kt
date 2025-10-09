package com.jxdx.mine.http.vo

//老师作业详情VO
data class TeachCreateHWDetailVO(
    val homeworkId: Long? = null,
    val subject: String? = null,
    val homeworkName: String? = null,
    val homeworkContent: String? = null,
    val deadTime: String? = null,
    val createdTime: String? = null,
    val updateTime: String? = null,
    val imageUrls: List<String>? = null
)
