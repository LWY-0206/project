package com.example.loding.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.Dynamic

class DynamicRepository {
    private val service: DynamicApi by lazy {
        HttpManager.instance.service(DynamicApi::class.java)
    }

    suspend fun getDynamics(
        page: Int? = null,
        size: Int,
    ): BaseResp<List<Dynamic>> = service.getDynamics(page, size)

    suspend fun postDynamic(dynamicBody: DynamicBody): BaseResp<Unit> =
        service.postDynamic("eb811e0a-8289-4be1-9f99-41e7f8990775", dynamicBody)
}
