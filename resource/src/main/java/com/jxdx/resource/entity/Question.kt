package com.jxdx.resource.entity

/**
 * 问题实体类，用于存储问题相关数据
 */
data class Question(
    // 问题ID，唯一标识
    val id: Int,
    // 问题内容（题干）
    val content: String,
    // 课程类型（可能用于区分不同课程的题目，如2代表某类课程）
    val courseType: Int,
    // 展示类型（可能用于区分题型，如3代表判断题）
    val showType: Int,
    // 选项列表（选择题有值，判断题可能为空数组）
    val chooses: List<String>,
    // 正确答案索引（如[0]可能代表"正确"，[1]代表"错误"，根据实际业务定义）
    val answers: List<Int>,
)