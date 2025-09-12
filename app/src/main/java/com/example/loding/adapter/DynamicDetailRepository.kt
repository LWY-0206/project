package com.example.loding.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.DynamicDetail

class DynamicDetailRepository {
    private val service: DynamicApi by lazy {
        HttpManager.instance.service(DynamicApi::class.java)
    }

    suspend fun getDPostDetails(postId: Int): BaseResp<DynamicDetail> =
        service.getPostDetail("e3c55f2f-023d-41ed-876b-d144db691e14", postId)
}
