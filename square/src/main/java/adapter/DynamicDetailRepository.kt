package com.example.loding.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.DynamicDetail

class DynamicDetailRepository {
    private val service: DynamicApi by lazy {
        HttpManager.instance.service(DynamicApi::class.java)
    }

    suspend fun getDPostDetails(postId: Int): BaseResp<DynamicDetail> =
        service.getPostDetail("6a819474-1cf4-42b2-b12e-794c4b472820", postId)
}
