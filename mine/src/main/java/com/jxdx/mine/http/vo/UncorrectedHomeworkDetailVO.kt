package com.jxdx.mine.http.vo

import java.io.Serializable

/**
 * 未批改作业详情数据类
 * 对应接口: /api/teach/homework/uncorrect/list
 */
data class UncorrectedHomeworkDetailVO(
    val homeworkId: Long?,           // 作业ID
    val studentId: Long?,            // 学生ID
    val studentName: String?,        // 学生姓名
    val submitContent: List<String>?, // 提交内容（数组类型）
    val submitTime: String?,         // 提交时间
    val score: Int?,                 // 分数
    val comment: String?             // 评语
) : Serializable
