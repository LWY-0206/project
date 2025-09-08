package com.example.loding.adapter

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.Dynamic
import com.example.loding.entity.DynamicDetail
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DynamicApi {
    @GET("square/post")
    suspend fun getDynamics(
        @Query("page") start: Int? = null,
        @Query("size") size: Int,
    ): BaseResp<List<Dynamic>>

    @GET("square/post/detail/{id}")
    suspend fun getPostDetail(
        @Path("id") id: Int,
    ): BaseResp<DynamicDetail>
}
