package com.jxdx.resource.News

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewApi {
    @GET("/api/resource/news")
    suspend fun getNewsList(
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<List<NewsItem>>
    @GET("/api/resource/news/detail")
    suspend fun getNewsDetail(
        @Query("newsId") id: Int,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<NewsDetail>
}