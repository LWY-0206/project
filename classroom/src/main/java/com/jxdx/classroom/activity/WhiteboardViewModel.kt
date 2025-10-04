package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.model.UserInfo
import com.jxdx.classroom.repository.WhiteboardRepository

class WhiteboardViewModel(application: Application) : BaseViewModel(application) {
    private val repository: WhiteboardRepository by lazy {
        WhiteboardRepository()
    }
    
    // 通用上传接口的LiveData
    val uploadLiveData: ResLiveData<List<String>> by lazy { 
        ResLiveData() 
    }
    
    // 用户信息LiveData
    val userInfoLiveData: ResLiveData<UserInfo> by lazy {
        ResLiveData()
    }
    
    // 当前用户信息缓存
    private var currentUserInfo: UserInfo? = null

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
     * 获取用户信息
     */
    fun getUserInfo() {
        request(
            userInfoLiveData,
            object : LiveDataCallback<UserInfo, UserInfo> {
                override fun success(
                    emit: ResLiveData<UserInfo>,
                    msg: String?,
                    data: UserInfo?
                ) {
                    data?.let {
                        currentUserInfo = it
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<UserInfo>,
                    code: Int?,
                    msg: String?,
                    data: UserInfo?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<UserInfo>,
                    e: ErrorResponse
                ) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.getUserInfo()
        }
    }
    
    /**
     * 获取当前用户信息
     */
    fun getCurrentUserInfo(): UserInfo? = currentUserInfo
    
    /**
     * 获取用户身份标识（用于WebSocket连接）
     */
    fun getUserIdentity(): Int = currentUserInfo?.identity ?: 0
    
    /**
     * 获取用户ID
     */
    fun getUserId(): String = currentUserInfo?.id?.toString() ?: "0"
    
    /**
     * 获取用户名
     */
    fun getUserName(): String = currentUserInfo?.userName ?: "未知用户"
    
}
