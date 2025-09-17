package com.jxdx.resource.Famous
import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class FamousViewModel(application: Application): BaseViewModel(application) {
    private val repository: FamousRepository by lazy{
        Log.d("FamousViewModel", "Initializing repository")
        FamousRepository()
    }
    val famousLiveData: ResLiveData<FamousData> by lazy {
        Log.d("FamousViewModel", "Initializing famousLiveData")
        ResLiveData()
    }
    fun getFamous(profession: String, page: Int, size: Int) {
        Log.d("FamousViewModel", "getFamous by call with profession: $profession, page: $page, size: $size")
        request(
            famousLiveData,
            object: LiveDataCallback<FamousData, FamousData> {
                override fun success(
                    emit: ResLiveData<FamousData>,
                    msg: String?,
                    data: FamousData?
                ) {
                    Log.d("FamousViewModel", "getFamous 成功 with data: $data")
                    data?.let{
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<FamousData>,
                    code: Int?,
                    msg: String?,
                    data: FamousData?)
                {
                    Log.d("FamousViewModel", "getFamous otherCode with code: $code, msg: $msg, data: $data")
                }

                override fun error(
                    emit: ResLiveData<FamousData>,
                    e: ErrorResponse){
                    Log.d("FamousViewModel", "getFamous error with e: $e")
                    emit.error(e)
                }

            }
        ) {
            repository.getFamous(profession, page, size)

        }
    }
}