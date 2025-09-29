package com.jxdx.resource.FirstPage
data class RecommendationResponse(
    val code: Int,
    val message: String,
    val data: RecommendationData
)
data class RecommendationData(
    val RecommendationList: List<RecommendationItem>
)
data class RecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val type: String, // "video" 或 "article"
    val duration: String? = null, // 视频时长，如 "05:30"
    val author: String = "官方推荐",
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    var isFavorited: Boolean = false
) {
    // 格式化统计数字的扩展函数
    private fun formatCount(count: Int): String {
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> "%.1fk".format(count / 1000.0)
            else -> "%.1fw".format(count / 10000.0)
        }
    }

    // 获取格式化后的统计信息
    fun getFormattedStats(): String {
        var stats = "${formatCount(viewCount)}浏览"
        if (likeCount > 0) {
            stats += " · ${formatCount(likeCount)}点赞"
        }
        return stats
    }

    // 判断是否为视频
    fun isVideo(): Boolean = type == "video"
}