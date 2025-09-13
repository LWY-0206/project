package com.example.loding.Questions

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
        @Header("satoken") satoken: String = "3a192a5e-40f1-4125-9ab2-e186c4b4374a"
    ): BaseResp<ErrorQuizData>
}