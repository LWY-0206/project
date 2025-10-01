package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.repository.WhiteboardRepository

class WhiteboardViewModel(application: Application) : BaseViewModel(application) {
    private val repository: WhiteboardRepository by lazy {
        WhiteboardRepository()
    }
    
    // 通用上传接口的LiveData
    val uploadLiveData: ResLiveData<List<String>> by lazy { 
        ResLiveData() 
    }
    
    // 白板快照上传接口的LiveData
    val snapshotUploadLiveData: ResLiveData<String> by lazy { 
        ResLiveData() 
    }

    /**
     * 上传白板图片到通用上传接口
     */
    fun uploadWhiteboardImage(file: String) {
        request(
            uploadLiveData,
            object : LiveDataCallback<List<String>, List<String>> {
                override fun success(
                    emit: ResLiveData<List<String>>,
                    msg: String?,
                    data: List<String>?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<String>>,
                    code: Int?,
                    msg: String?,
                    data: List<String>?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<String>>,
                    e: ErrorResponse
                ) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.uploadWhiteboardImage(file)
        }
    }
    
    /**
     * 上传白板快照到专门的快照接口
     * @param roomId 房间ID
     * @param file 快照图片文件路径
     */
    fun uploadWhiteboardSnapshot(roomId: String, file: String) {
        request(
            snapshotUploadLiveData,
            object : LiveDataCallback<String, String> {
                override fun success(
                    emit: ResLiveData<String>,
                    msg: String?,
                    data: String?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<String>,
                    code: Int?,
                    msg: String?,
                    data: String?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<String>,
                    e: ErrorResponse
                ) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.uploadWhiteboardSnapshot(roomId, file)
        }
    }
}
