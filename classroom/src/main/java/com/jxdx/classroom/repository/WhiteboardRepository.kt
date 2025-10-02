package com.jxdx.classroom.repository

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.http.WhiteboardApi
import com.jxdx.classroom.model.UserInfo
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
     * 获取用户信息
     */
    suspend fun getUserInfo(): BaseResp<UserInfo> {
        val result = whiteboardApi.getUserInfo(
            TokenManager.getToken().toString()
        )
        
        // 输出用户信息
        val userInfo = result.data
        if (userInfo != null) {
            Log.d(TAG, "获取用户信息成功: ${userInfo.userName}, 身份: ${userInfo.getIdentityDescription()}")
        }
        
        return result
    }
    
}
