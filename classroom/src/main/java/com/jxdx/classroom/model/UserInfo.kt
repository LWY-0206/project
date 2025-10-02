package com.jxdx.classroom.model

/**
 * 用户信息模型
 */
data class UserInfo(
    val id: Int,                    // 用户ID
    val userName: String,           // 用户名
    val avatarUrl: String,          // 头像URL
    val phone: String,              // 手机号
    val profile: String,            // 个人简介
    val status: Int,                // 状态
    val identity: Int,              // 身份标识（0=学生，1=老师）
    val className: String           // 班级名称
) {
    /**
     * 是否为老师
     */
    fun isTeacher(): Boolean = identity == 1
    
    /**
     * 是否为学生
     */
    fun isStudent(): Boolean = identity == 0
    
    /**
     * 获取身份描述
     */
    fun getIdentityDescription(): String = if (isTeacher()) "老师" else "学生"
}
