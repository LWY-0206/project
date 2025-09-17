package com.jxdx.resource.entity

data class QuestionData(
    val questions: ArrayList<Question>, // 对应后端data.questions（题目数组，与你的Question类匹配）
    val page: PageInfo,
)
