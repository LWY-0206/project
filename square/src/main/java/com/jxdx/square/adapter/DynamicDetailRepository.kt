package com.jxdx.square.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.DynamicDetail

class DynamicDetailRepository {
    private val service: DynamicApi by lazy {
        HttpManager.instance.service(DynamicApi::class.java)
    }

    suspend fun getDPostDetails(postId: Int): BaseResp<DynamicDetail> =
        service.getPostDetail(TokenManager.getToken().toString(), postId)
}
