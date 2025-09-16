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
    ): BaseResp<List<Dynamic>> = service.getDynamics("6a819474-1cf4-42b2-b12e-794c4b472820", page, size)

    suspend fun postDynamic(dynamicBody: DynamicBody): BaseResp<Unit> =
        service.postDynamic("6a819474-1cf4-42b2-b12e-794c4b472820", dynamicBody)

    // 修复参数名：从commentId改为postId，与API接口定义一致
    suspend fun likeDynamic(postId: Int): BaseResp<Unit> = service.likeDynamic("6a819474-1cf4-42b2-b12e-794c4b472820", postId)
}
