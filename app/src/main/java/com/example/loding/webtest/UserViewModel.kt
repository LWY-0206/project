package com.example.loding.webtest

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class UserViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: UserRepository by lazy {
        Log.d("TAG", ":2 ")
        UserRepository()
    }
    // 更改为直接使用 UserData 类型
    val userLiveData: ResLiveData<UserData> by lazy { ResLiveData() }

    fun getUsers(
        type: Int,
        a: Int,
        b: Int,
    ) {
        request(
            userLiveData,
            object : LiveDataCallback<UserData, UserData> {
                override fun success(
                    emit: ResLiveData<UserData>,
                    msg: String?,
                    data: UserData?,
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<UserData>,
                    code: Int?,
                    msg: String?,
                    data: UserData?,
                ) {
                    // 即使code不匹配，也将原始数据传递给error方法的第二个参数
                    // 这样UI层仍然可以访问到正确的数据
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<UserData>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
        ) {
            repository.getUsers(type, a, b)
        }
    }
}
