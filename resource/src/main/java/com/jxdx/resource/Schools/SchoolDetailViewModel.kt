package com.jxdx.resource.Schools

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class SchoolDetailViewModel(application: Application) : BaseViewModel(application) {
    private val repository: SchoolDetailRepository by lazy {
        Log.d("SchoolDetailViewModel", "Initializing SchoolDetailRepository")
        SchoolDetailRepository()
    }

    val schoolDetailLiveData: ResLiveData<SchoolDetail> by lazy {
        Log.d("SchoolDetailViewModel", "Initializing schoolDetailLiveData")
        ResLiveData()
    }

    fun getSchoolDetail(schoolId: Int) {
        Log.d("SchoolDetailViewModel", "getSchoolDetail called with schoolId=$schoolId")

        request(
            schoolDetailLiveData,
            object : LiveDataCallback<SchoolDetail, SchoolDetail> {
                override fun success(
                    emit: ResLiveData<SchoolDetail>,
                    msg: String?,
                    data: SchoolDetail?
                ) {
                    Log.d("SchoolDetailViewModel", "Request success: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<SchoolDetail>,
                    code: Int?,
                    msg: String?,
                    data: SchoolDetail?
                ) {
                    Log.w("SchoolDetailViewModel", "Request otherCode: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<SchoolDetail>,
                    e: ErrorResponse
                ) {
                    Log.e("SchoolDetailViewModel", "Request error: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("SchoolDetailViewModel", "Executing repository call")
            repository.getSchoolDetail(schoolId)
        }
    }
}