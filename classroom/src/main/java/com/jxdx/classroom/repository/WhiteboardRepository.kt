package com.jxdx.classroom.repository

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.http.WhiteboardApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class WhiteboardRepository {
    private val TAG = "WhiteboardRepository"
    
    private val whiteboardApi: WhiteboardApi by lazy {
        HttpManager.instance.service(WhiteboardApi::class.java)
    }

    /**
     * 上传白板图片到通用上传接口
     */
    suspend fun uploadWhiteboardImage(file: String): BaseResp<List<String>> {
        // 创建File对象
        val fileObj = File(file)

        // 创建RequestBody
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileObj)

        // 创建MultipartBody.Part
        val filePart = MultipartBody.Part.createFormData("fileList", fileObj.name, requestBody)
        
        // 发送请求
        val result = whiteboardApi.uploadFile(
            TokenManager.getToken().toString(),
            filePart,
        )
        
        // 只输出返回的URL
        val data = result.data
        if (data != null && data.isNotEmpty()) {
            Log.d(TAG, "返回的图片URL: ${data[0]}")
        }
        
        return result
    }
    
    /**
     * 上传白板快照到专门的快照接口
     * @param roomId 房间ID
     * @param file 快照图片文件路径
     */
    suspend fun uploadWhiteboardSnapshot(roomId: String, file: String): BaseResp<String> {
        // 创建File对象
        val fileObj = File(file)

        // 创建RequestBody
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileObj)

        // 创建MultipartBody.Part，参数名为"file"
        val filePart = MultipartBody.Part.createFormData("file", fileObj.name, requestBody)
        
        // 发送请求到白板快照接口
        val result = whiteboardApi.uploadWhiteboardSnapshot(
            roomId,
            TokenManager.getToken().toString(),
            filePart
        )
        
        // 输出返回的快照URL
        val data = result.data
        if (data != null) {
            Log.d(TAG, "白板快照上传成功，返回URL: $data")
        }
        
        return result
    }
}
