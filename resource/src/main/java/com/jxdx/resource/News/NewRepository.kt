package com.jxdx.resource.News

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class NewRepository {
    private val service: NewApi by lazy {
        HttpManager.instance.service(NewApi::class.java)
    }
    suspend fun getNewsList(): BaseResp<List<NewsItem>> {
        return service.getNewsList()
    }
}