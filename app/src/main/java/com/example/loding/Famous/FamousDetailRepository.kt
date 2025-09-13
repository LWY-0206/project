package com.example.loding.Famous

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class FamousDetailRepository {
    private val service: FamousDetailApi by lazy {
        HttpManager.instance.service(FamousDetailApi::class.java)
    }

    suspend fun getFamousDetail(celebrityId: Int): BaseResp<FamousDetail> {
        Log.d("FamousDetailRepository", "获取名人详情: celebrityId=$celebrityId")
        return service.getFamousDetail(celebrityId)
    }
}