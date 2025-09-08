package com.example.loding.mytest

import com.example.loding.entity.PageInfo
import com.example.loding.entity.Question

data class QuestionResponse(
    val questions: List<Question>,
    val page: PageInfo,
)
