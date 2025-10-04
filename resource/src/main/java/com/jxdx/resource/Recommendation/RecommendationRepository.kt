package com.jxdx.resource.Recommendation

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class RecommendationRepository {
    private  val service: RecommendationApi by lazy{
        HttpManager.instance.service(RecommendationApi::class.java)
    }
    suspend fun getRecommendation(): BaseResp<List<RecommendationItem>> {
        return service.getRecommendationList()
    }
}