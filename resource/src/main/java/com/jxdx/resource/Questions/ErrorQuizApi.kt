// ErrorQuizApi.kt
package com.jxdx.resource.Questions

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ErrorQuizApi {
    @GET("api/questions/error")
    suspend fun getErrorQuestions(
        @Query("subjectId") subjectId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<ErrorQuizData>

    // 恢复或开始练习
    @POST("/api/questions/practice/resume")
    suspend fun resumePractice(
        @Query("subjectId") subjectId: Int,
        @Query("questionType") questionType: Int,
        @Query("questionCount") questionCount: Int,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<PracticeSession>

    // 提交答案
    @POST("/api/questions/practice/submit")
    suspend fun submitAnswer(
        @Query("sessionId") sessionId: Int,
        @Query("questionId") questionId: Int,
        @Query("isCorrect") isCorrect: Boolean,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<SubmitResponse>
}