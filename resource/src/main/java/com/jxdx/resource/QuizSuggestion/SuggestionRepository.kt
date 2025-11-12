package com.jxdx.resource.QuizSuggestion

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class SuggestionRepository {
    private val service: SuggestionApi by lazy{
        HttpManager.instance.service(SuggestionApi::class.java)
    }
    suspend fun getQuizSuggestion(subjectId: Int, questionIds: List<Int>): BaseResp<SuggestionData> {
        return service.getQuizSuggestion(subjectId, questionIds)
    }
}