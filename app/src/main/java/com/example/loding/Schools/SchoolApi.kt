package com.example.loding.Schools

import com.example.corekit.http.bean.BaseResp
import com.example.loding.Questions.ErrorQuizData
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SchoolApi {
    @GET("/api/school")
    suspend fun getSchoolsByName(
        @Query("schoolName") schoolName: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = "bc562136-4e77-40b1-a644-50c999ff0b19"
    ): BaseResp<SchoolData>
    @GET("/api/school/select/score")
    suspend fun getSchoolsByScore(
        @Query("score") score: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = "bc562136-4e77-40b1-a644-50c999ff0b19"
    ): BaseResp<SchoolData>
}