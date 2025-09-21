package com.jxdx.resource.Questions

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
        @Header("satoken") satoken: String = "f646b77d-257e-4fd2-aaa7-e13263932d50"
    ): BaseResp<ErrorQuizData>
}