package com.jxdx.resource.Recommendation

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.Header

class RecommendationRepository {
    private  val service: RecommendationApi by lazy{
        HttpManager.instance.service(RecommendationApi::class.java)
    }
    suspend fun getRecommendation(modelType: Int): BaseResp<List<RecommendationItem>> {
        return service.getRecommendationList(modelType)
    }
    suspend fun getRecommeddationDetail( resourceId:Int):BaseResp<RecommendationDetail>{
        return service.getRecommendationDetail(resourceId)
    }
}