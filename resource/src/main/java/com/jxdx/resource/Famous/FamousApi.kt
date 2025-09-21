package com.jxdx.resource.Famous

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
        @Header("satoken") satoken: String = "0f6b8b4c-6d39-47dc-8da8-0c6addd377d0"
    ): BaseResp<FamousData>

    @GET("/api/celebrity/user/favorites")
    suspend fun  getFavoriteList(
        @Query("page") page:Int,
        @Query("size") size:Int,
    @Header("satoken") satoken: String = "0f6b8b4c-6d39-47dc-8da8-0c6addd377d0"
    ): BaseResp<FamousData>


}