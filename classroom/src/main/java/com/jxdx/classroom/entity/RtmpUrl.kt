package com.jxdx.classroom.entity

/**
 * 直播间详细信息实体类
 * 包含直播间的所有相关信息
 */
data class RtmpUrl(
    val liveId: Int,                    // 直播间ID
    val userId: Int,                    // 用户ID
    val teacherName: String,            // 教师姓名
    val subjectId: Int,                 // 学科ID
    val subjectName: String?,           // 学科名称（可为空）
    val status: Int,                    // 状态：0-未开始，1-正在直播
    val roomName: String,               // 房间名称
    val description: String,            // 描述
    val rtmpUrl: String,                // RTMP推流地址
    val startTime: String?,             // 开始时间（可为空）
    val classIds: List<Int>,            // 班级ID列表
    val classNames: List<String>        // 班级名称列表
)