package com.jxdx.classroom.model

/**
 * 学生信息模型
 */
data class Student(
    val id: String,           // 学生ID
    val name: String,         // 学生姓名
    val avatar: String? = null, // 头像URL
    val isOnline: Boolean = false, // 是否在线
    val hasSubmitted: Boolean = false, // 是否已提交答案
    val lastActiveTime: Long = 0L // 最后活跃时间
)
