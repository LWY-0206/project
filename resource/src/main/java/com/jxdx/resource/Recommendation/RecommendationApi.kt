package com.jxdx.resource.Recommendation

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header

interface RecommendationApi {
    @GET("/api/resource/list")
    suspend fun getRecommendationList(
        @Header("satoken") satoken: String= TokenManager.getToken().toString()
    ): BaseResp<List<RecommendationItem>>
}