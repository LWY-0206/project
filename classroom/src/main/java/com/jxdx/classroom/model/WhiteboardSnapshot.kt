package com.jxdx.classroom.model

/**
 * 白板快照模型
 */
data class WhiteboardSnapshot(
    val id: String,           // 快照ID
    val studentId: String,    // 学生ID
    val studentName: String,  // 学生姓名
    val imageUrl: String,     // 图片URL
    val timestamp: Long,      // 提交时间戳
    val isViewed: Boolean = false // 是否已查看
)
