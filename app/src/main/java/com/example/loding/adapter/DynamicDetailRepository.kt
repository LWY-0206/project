package com.example.loding.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.DynamicDetail

class DynamicDetailRepository {
    private val service: DynamicApi by lazy {
        HttpManager.instance.service(DynamicApi::class.java)
    }

    suspend fun getDPostDetails(postId: Int): BaseResp<DynamicDetail> =
        service.getPostDetail("eb811e0a-8289-4be1-9f99-41e7f8990775", postId)
}
