package com.example.loding.Famous

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class FamousDetailViewModel(application: Application) : BaseViewModel(application) {
    private val repository: FamousDetailRepository by lazy {
        Log.d("FamousDetailViewModel", "Initializing FamousDetailRepository")
        FamousDetailRepository()
    }

    val famousDetailLiveData: ResLiveData<FamousDetail> by lazy {
        Log.d("FamousDetailViewModel", "Initializing famousDetailLiveData")
        ResLiveData()
    }

    fun getFamousDetail(celebrityId: Int) {
        Log.d("FamousDetailViewModel", "getFamousDetail called with celebrityId=$celebrityId")

        request(
            famousDetailLiveData,
            object : LiveDataCallback<FamousDetail, FamousDetail> {
                override fun success(
                    emit: ResLiveData<FamousDetail>,
                    msg: String?,
                    data: FamousDetail?
                ) {
                    Log.d("FamousDetailViewModel", "Request success: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<FamousDetail>,
                    code: Int?,
                    msg: String?,
                    data: FamousDetail?
                ) {
                    Log.w("FamousDetailViewModel", "Request otherCode: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<FamousDetail>,
                    e: ErrorResponse
                ) {
                    Log.e("FamousDetailViewModel", "Request error: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("FamousDetailViewModel", "Executing repository call")
            repository.getFamousDetail(celebrityId)
        }
    }
}