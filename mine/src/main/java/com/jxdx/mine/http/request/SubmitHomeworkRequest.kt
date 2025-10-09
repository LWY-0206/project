package com.jxdx.mine.http.request

//作业提交请求类
data class SubmitHomeworkRequest(
    val homeworkId: Long? = null,
    val subjectId: Int? = null,
    val studentId: Long? = null,
    val studentContent: List<String>? = null
)
