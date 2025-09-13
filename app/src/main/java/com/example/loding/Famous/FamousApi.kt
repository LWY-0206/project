package com.example.loding.Famous

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FamousApi {
    @GET("/api/celebrity")
    suspend fun getFamous(
        @Query("profession") profession: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("satoken") satoken: String = "e1738e55-b473-46f7-bbdd-3c0aa19f9627"
    ): BaseResp<FamousData>


}