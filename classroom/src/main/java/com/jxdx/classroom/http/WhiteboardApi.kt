package com.jxdx.classroom.http

import com.example.corekit.http.bean.BaseResp
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface WhiteboardApi {
    @Multipart
    @POST("/common/upload")
    suspend fun uploadFile(
        @Header("satoken") token: String,
        @Part file: MultipartBody.Part,
    ): BaseResp<List<String>>
    
    /**
     * 上传白板快照
     * @param roomId 房间ID
     * @param token 认证token
     * @param file 快照图片文件
     */
    @Multipart
    @POST("/whiteboard/snapshot/{roomId}")
    suspend fun uploadWhiteboardSnapshot(
        @Path("roomId") roomId: String,
        @Header("satoken") token: String,
        @Part file: MultipartBody.Part,
    ): BaseResp<String>
}
