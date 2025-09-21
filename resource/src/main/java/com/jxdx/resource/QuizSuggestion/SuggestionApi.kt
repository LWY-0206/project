package com.jxdx.resource.QuizSuggestion

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SuggestionApi {
    @GET("/api/questions/summary")
    suspend fun getQuizSuggestion(
        @Query("subjectId") subjectId: Int,
        @Query("questionIds") questionIds: List<Int>,
        @Header("satoken") satoken: String = "0f6b8b4c-6d39-47dc-8da8-0c6addd377d0"
    ): BaseResp<SuggestionData>
}