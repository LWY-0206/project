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
        @Header("satoken") satoken: String = "f646b77d-257e-4fd2-aaa7-e13263932d50"
    ): BaseResp<FamousData>

    @GET("/api/celebrity/user/favorites")
    suspend fun  getFavoriteList(
        @Query("page") page:Int,
        @Query("size") size:Int,
    @Header("satoken") satoken: String = "f646b77d-257e-4fd2-aaa7-e13263932d50"
    ): BaseResp<FamousData>


}