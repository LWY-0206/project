package com.jxdx.classroom.entity

/**
 * 教学工具实体类
 */
data class TeachingTool(
    val id: Int,
    val name: String,
    val description: String,
    val iconResId: Int,
    val category: ToolCategory,
    val isSelected: Boolean = false,
    val isAvailable: Boolean = true
)

/**
 * 工具分类枚举
 */
enum class ToolCategory {
    MATH,           // 数学工具
    DRAWING,        // 绘图工具
    PRESENTATION,   // 演示工具
    INTERACTION,     // 互动工具
    MEDIA           // 媒体工具
}
