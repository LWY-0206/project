package com.example.corekit.http.listener

import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData

interface LiveDataCallback<LiveData, Response> {
    fun success(emit: ResLiveData<LiveData>, msg: String?, data: Response?)
    fun otherCode(emit: ResLiveData<LiveData>, code: Int?, msg: String?, data: Response?)
    fun error(emit: ResLiveData<LiveData>, e: ErrorResponse)
}
