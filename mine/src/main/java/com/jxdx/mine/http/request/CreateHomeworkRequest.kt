package com.jxdx.mine.http.request

//创建作业请求类
data class CreateHomeworkRequest(
    val subjectId: Int? = null,
    val homeworkName: String? = null,
    val deadTime: String? = null,
    val homeworkContent: String? = null,
    val imageUrls: List<String>? = null
)
