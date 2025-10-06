package com.jxdx.classroom.group

import java.io.Serializable

/**
 * 题型数据类
 * 用于存储老师提前设置的题型信息
 */
data class QuestionType(
    val id: Int = 0, // 题型ID
    val title: String = "", // 题型标题
    val content: String = "", // 题型内容
    val images: List<String> = emptyList(), // 图片URL列表
    val files: List<String> = emptyList(), // 文件URL列表
    val createTime: Long = System.currentTimeMillis() // 创建时间
) : Serializable