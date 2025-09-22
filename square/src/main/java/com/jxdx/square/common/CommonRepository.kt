package com.jxdx.square.common

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class CommonRepository {
    private val common: CommonApi by lazy {
        HttpManager.instance.service(CommonApi::class.java)
    }

    suspend fun uploadFile(file: String): BaseResp<List<String>> {
        // 创建File对象
        val fileObj = File(file)

        // 创建RequestBody
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileObj)

        // 创建MultipartBody.Part
        val filePart = MultipartBody.Part.createFormData("fileList", fileObj.name, requestBody)
        // 返回封装好的数据
        return common.uploadFile(
            TokenManager.getToken().toString(),
            filePart,
        )
    }
}
