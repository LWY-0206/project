package com.jxdx.mine.http.request

/**
 * AI批改作业请求数据类
 * 对应接口: /api/teach/homework/correct/ai
 */
data class AiReviewHomeworkRequest(
    val homeworkId: Long,           // 作业ID
    val subjectId: Int,             // 科目ID
    val studentId: Long,            // 学生ID
    val studentContent: List<String> // 学生提交内容
)
