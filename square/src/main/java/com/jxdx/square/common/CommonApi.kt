package com.jxdx.square.common

import com.example.corekit.http.bean.BaseResp
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CommonApi {
    @Multipart
    @POST("/common/upload")
    suspend fun uploadFile(
        @Header("satoken") source: String,
        @Part file: MultipartBody.Part,
    ): BaseResp<List<String>>
}
