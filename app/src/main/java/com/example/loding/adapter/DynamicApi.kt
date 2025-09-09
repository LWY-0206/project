package com.example.loding.adapter

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.Dynamic
import com.example.loding.entity.DynamicDetail
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DynamicApi {
    @GET("/square/page/post")
    suspend fun getDynamics(
        @Query("page") start: Int? = null,
        @Query("size") size: Int,
    ): BaseResp<List<Dynamic>>

    @GET("square/post/detail/{postId}")
    suspend fun getPostDetail(
        @Header("satoken") source: String,
        @Path("postId") postId: Int,
    ): BaseResp<DynamicDetail>

    // 发布动态的Post
    @POST("/square/post")
    suspend fun postDynamic(
        @Header("satoken") source: String,
        @Body dynamicBody: DynamicBody,
    ): BaseResp<Unit>
}
