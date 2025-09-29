package com.jxdx.classroom.http

import com.example.corekit.http.bean.BaseResp
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WhiteboardApi {
    @Multipart
    @POST("/common/upload")
    suspend fun uploadFile(
        @Header("satoken") token: String,
        @Part file: MultipartBody.Part,
    ): BaseResp<List<String>>
}
