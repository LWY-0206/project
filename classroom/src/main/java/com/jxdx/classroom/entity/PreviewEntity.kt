package com.jxdx.classroom.entity

import java.util.Date

/**
 * 预习任务实体类
 */
data class PreviewTask(
    val id: Int,
    val title: String,
    val content: String,
    val publishTime: String,
    val deadline: String,
    val completionRate: Int,
    val isCompleted: Boolean,
    val subjectId: Int,
    val teacherId: Int
)

/**
 * 学生预习状态实体类
 */
data class StudentPreviewStatus(
    val studentId: String,
    val studentName: String,
    val completionRate: Int,
    val previewTime: String,
    val lastUpdateTime: String,
    val status: PreviewStatus
)

/**
 * 预习状态枚举
 */
enum class PreviewStatus {
    EXCELLENT,  // 优秀 (≥90%)
    GOOD,       // 良好 (80-89%)
    AVERAGE,    // 一般 (60-79%)
    POOR        // 较差 (<60%)
}
