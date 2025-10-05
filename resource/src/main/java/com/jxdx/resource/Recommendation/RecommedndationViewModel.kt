package com.jxdx.resource.Recommendation

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class RecommendationViewModel(application: Application) : BaseViewModel(application) {

    private val repository: RecommendationRepository by lazy {
        RecommendationRepository()
    }

    val recommendationLiveData: ResLiveData<List<RecommendationItem>> by lazy {
        ResLiveData()
    }

    /**
     * 获取推荐列表
     */
    fun getRecommendationList() {
        request(recommendationLiveData, object : LiveDataCallback<List<RecommendationItem>, List<RecommendationItem>> {
            override fun success(
                emit: ResLiveData<List<RecommendationItem>>,
                msg: String?,
                data: List<RecommendationItem>?
            ) {
                data?.let {
                    emit.success(it)
                    Log.d("RecommendationViewModel", "获取推荐列表成功，数量: ${it.size}")
                } ?: run {
                    Log.d("RecommendationViewModel", "获取推荐列表成功但数据为空")
                    emit.success(emptyList())
                }
            }

            override fun otherCode(
                emit: ResLiveData<List<RecommendationItem>>,
                code: Int?,
                msg: String?,
                data: List<RecommendationItem>?
            ) {
                Log.d("RecommendationViewModel", "获取推荐列表其他状态码: code=$code, msg=$msg")
            }

            override fun error(
                emit: ResLiveData<List<RecommendationItem>>,
                e: ErrorResponse
            ) {
                Log.e("RecommendationViewModel", "获取推荐列表错误: ${e.message}", null)
                emit.error(e)
            }
        }) {
            repository.getRecommendation()
        }
    }

    /**
     * 刷新推荐列表
     */
    fun refreshRecommendationList() {
        getRecommendationList()
    }

    /**
     * 处理收藏点击
     */
}