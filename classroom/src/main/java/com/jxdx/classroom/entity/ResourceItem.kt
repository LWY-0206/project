package com.jxdx.classroom.entity

/**
 * 资源项实体类
 * 表示一个可选择的资料项
 */
data class ResourceItem(
    val id: String,
    val name: String,
    val type: Type,
    val url: String,
    val thumbnailUrl: String? = null,
    val size: Long = 0,
    val uploadTime: Long = System.currentTimeMillis(),
    val description: String? = null
) {
    
    /**
     * 资源类型枚举
     */
    enum class Type {
        IMAGE,      // 图片
        DOCUMENT,   // 文档
        VIDEO,      // 视频
        AUDIO       // 音频
    }
    
    /**
     * 获取文件大小描述
     */
    fun getSizeDescription(): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)}MB"
            else -> "${size / (1024 * 1024 * 1024)}GB"
        }
    }
    
    /**
     * 获取类型描述
     */
    fun getTypeDescription(): String {
        return when (type) {
            Type.IMAGE -> "图片"
            Type.DOCUMENT -> "文档"
            Type.VIDEO -> "视频"
            Type.AUDIO -> "音频"
        }
    }
}
