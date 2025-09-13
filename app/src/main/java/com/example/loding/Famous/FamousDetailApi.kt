package com.example.loding.Famous

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FamousDetailApi {
    @GET("/api/celebrity/detail")
    suspend fun getFamousDetail(
        @Query("celebrityId") celebrityId: Int,
        @Header("satoken") satoken: String = "e1738e55-b473-46f7-bbdd-3c0aa19f9627"
    ): BaseResp<FamousDetail>
}