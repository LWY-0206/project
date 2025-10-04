package com.jxdx.classroom.http

import com.example.corekit.http.TokenManager
import com.example.corekit.http.interceptor.HeaderInterceptor
import com.example.corekit.http.interceptor.LogInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://121.41.176.238:8080"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // 设置合理的超时时间，与MyApp中的HttpManager保持一致
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            // 添加HeaderInterceptor以确保请求头信息完整
            .addInterceptor(HeaderInterceptor({
                // 提供必要的请求头信息，包括token
                val headers = mutableMapOf<String, String?>()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                headers["satoken"] = TokenManager.getToken() ?: ""
                headers
            }))
            // 添加LogInterceptor以便调试
            .addInterceptor(LogInterceptor())
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // 使用配置好的OkHttpClient
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}