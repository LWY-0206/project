package com.jxdx.resource.Questions

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ErrorQuizApi {
    @GET("api/questions/error")
    suspend fun getErrorQuestions(
        @Query("subjectId") subjectId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<ErrorQuizData>
}