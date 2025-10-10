package com.jxdx.mine.http.request

/**
 * 批改作业请求数据类
 * 对应接口: /api/teach/homework/correct
 */
data class ReviewHomeworkRequest(
    val homeworkId: Long,        // 作业ID
    val studentId: Long,         // 学生ID
    val teacherComment: String,  // 教师评语
    val score: Int               // 分数
)
