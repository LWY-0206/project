package com.jxdx.mine.http.request

//编辑作业请求类
data class EditHomeworkRequest(
    val homeworkId: Long? = null,
    val subjectId: Int? = null,
    val homeworkName: String? = null,
    val deadTime: String? = null,
    val homeworkContent: String? = null,
    val imageUrls: List<String>? = null
)
