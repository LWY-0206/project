package com.jxdx.mine.http.vo

import java.io.Serializable

/**
 * AI批改作业结果数据类
 * 对应接口: /api/teach/homework/correct/ai 的返回结果
 */
data class AiReviewResultVO(
    val score: Int?,                // AI给出的分数
    val comment: String?,           // AI给出的评语
    val suggestions: List<String>?, // AI给出的建议
    val confidence: Float?           // AI批改的置信度
) : Serializable
