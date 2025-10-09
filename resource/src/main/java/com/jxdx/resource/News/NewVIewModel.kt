package com.jxdx.resource.News

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class NewVIewModel (application: Application): BaseViewModel(application) {
    private val repository: NewRepository by lazy {
        NewRepository()
    }
    val newsLiveData: ResLiveData<List<NewsItem>> by lazy {
        ResLiveData()
    }
    val detailLiveData: ResLiveData<NewsDetail> by lazy {
        ResLiveData()
    }

    fun getNewsList() {
        request(newsLiveData, object : LiveDataCallback<List<NewsItem>, List<NewsItem>> {
            override fun success(
                emit: ResLiveData<List<NewsItem>>,
                msg: String?,
                data: List<NewsItem>?
            ) {
                data?.let {
                    emit.success(it)
                }
            }

            override fun otherCode(
                emit: ResLiveData<List<NewsItem>>,
                code: Int?,
                msg: String?,
                data: List<NewsItem>?
            ) {
                Log.d(
                    "NewVIewModel",
                    "getNewsList otherCode with code: $code, msg: $msg, data: $data"
                )
            }

            override fun error(
                emit: ResLiveData<List<NewsItem>>,
                e: ErrorResponse
            ) {
                Log.d("NewVIewModel", "getNewsList error with e: $e")
            }

        }) {
            repository.getNewsList()
        }
    }

    fun getNewsDetail(id: Int) {
        request(detailLiveData, object : LiveDataCallback<NewsDetail, NewsDetail> {
            override fun success(
                emit: ResLiveData<NewsDetail>,
                msg: String?,
                data: NewsDetail?
            ) {
                data?.let {
                    emit.success(it)
                }
            }

            override fun otherCode(
                emit: ResLiveData<NewsDetail>,
                code: Int?,
                msg: String?,
                data: NewsDetail?
            ) {
                Log.d(
                    "NewVIewModel",
                    "getNewsDetail otherCode with code: $code, msg: $msg, data: $data"
                )
            }

            override fun error(
                emit: ResLiveData<NewsDetail>,
                e: ErrorResponse
            ) {
                Log.d("NewVIewModel", "getNewsDetail error with e: $e")
            }
        }) {
            repository.getNewsDetail(id)
        }
    }
}