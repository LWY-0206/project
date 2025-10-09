package com.jxdx.resource.Recommendation

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RecommendationApi {
    @GET("/api/resource/list")
    suspend fun getRecommendationList(
        @Query("modelType") modelType: Int,
        @Header("satoken") satoken: String= TokenManager.getToken().toString()
    ): BaseResp<List<RecommendationItem>>
    @GET("/api/resource/detail")
    suspend fun getRecommendationDetail(
        @Query("resourceId") resourceId:Int,
        @Header("satoken") satoken: String= TokenManager.getToken().toString()
    ): BaseResp<RecommendationDetail>
}