package com.jxdx.resource.Schools

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SchoolApi {
    @GET("/api/school")
    suspend fun getSchoolsByName(
        @Query("schoolName") schoolName: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = "0f6b8b4c-6d39-47dc-8da8-0c6addd377d0"
    ): BaseResp<SchoolData>
    @GET("/api/school/select/score")
    suspend fun getSchoolsByScore(
        @Query("score") score: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = "0f6b8b4c-6d39-47dc-8da8-0c6addd377d0"
    ): BaseResp<SchoolData>
}