package com.jxdx.resource.News

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val coverUrl: String,
    val source: String,
    val publishTime: String,
    val viewCount: Int,
    val content: String = "",
    val category: String = "校园新闻"
) {
    // 格式化时间显示
    fun getFormattedTime(): String {
        return when {
            publishTime.contains("小时") -> publishTime
            else -> "$publishTime 前"
        }
    }

    // 格式化阅读量
    fun getFormattedViews(): String {
        return when {
            viewCount < 1000 -> "${viewCount}阅读"
            else -> "%.1fk阅读".format(viewCount / 1000.0)
        }
    }
}