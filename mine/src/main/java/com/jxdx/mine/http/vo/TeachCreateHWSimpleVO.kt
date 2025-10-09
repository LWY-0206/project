package com.jxdx.mine.http.vo

//老师作业简单信息VO
data class TeachCreateHWSimpleVO(
    val homeworkId: Long? = null,
    val subjectName: String? = null,
    val homeworkName: String? = null,
    val deadTime: String? = null,
    val createTime: String? = null
)
