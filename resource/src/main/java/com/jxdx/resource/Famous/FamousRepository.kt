package com.jxdx.resource.Famous

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class FamousRepository {
    private val service: FamousApi by lazy{
        Log.d("创建示例", "FamousApi")
        HttpManager.instance.service(FamousApi::class.java)
    }
    suspend fun getFamous(
        profession: String,
        page: Int,
        size: Int
    ): BaseResp<FamousData> {
        return service.getFamous(profession, page, size)
    }
}