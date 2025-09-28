package com.jxdx.classroom.entity

/**
 * 创建直播房间请求实体类
 */
data class CreateLiveRoomRequest(
    val subjectId: Int,
    val roomName: String,
    val description: String,
    val startTime: String,
    val classIds: List<Int>
)
