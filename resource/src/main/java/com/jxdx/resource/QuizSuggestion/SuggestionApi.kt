package com.jxdx.resource.QuizSuggestion

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SuggestionApi {
    @GET("/api/questions/summary")
    suspend fun getQuizSuggestion(
        @Query("subjectId") subjectId: Int,
        @Query("questionIds") questionIds: List<Int>,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<SuggestionData>
}