package com.jxdx.resource.FirstPage
data class ScheduleItem(
    val id: String,
    val week: String,           // 第几周
    val weekday: String,        // 星期几
    val currentTime: String,    // 当前时间
    val courseName: String,     // 课程名称
    val courseTime: String,     // 上课时间
    val location: String,       // 上课地点
    val courseType: String,     // 课程类型
    val isCurrent: Boolean = false // 是否是当前课程
)