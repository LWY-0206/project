package com.example.corekit.http

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import com.example.corekit.R
import com.example.corekit.http.bean.BaseResp
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ErrorType
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.CommonCallback
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.util.ResourceUtil
import com.example.corekit.util.nullOrNot
import com.example.corekit.util.toast
import retrofit2.HttpException
import java.io.EOFException
import java.net.ConnectException
import java.net.UnknownHostException

const val CODE_SUCCESS = 0
const val CODE_LOGIN = 10086
fun <T, D> BaseResp<D>.process(
    resLiveData: ResLiveData<T>,
    callback: LiveDataCallback<T, D>
): Boolean {
    when (code) {
        CODE_SUCCESS -> callback.success(resLiveData, this.message, this.data)
        CODE_LOGIN -> {
            message.toast()
            TokenManager.gotoLogin()
            return false
        }
        else ->
            callback.otherCode(resLiveData, this.code, this.message, this.data)
    }
    return true
}

fun <D> BaseResp<D>.process(
    callback: CommonCallback<D>
): Boolean {
    when (code) {
        CODE_SUCCESS -> callback.success(this.message, this.data)
        CODE_LOGIN -> {
            TokenManager.gotoLogin()
            return false
        }
        else ->
            callback.otherCode(this.code, this.message, this.data)
    }
    return true
}

inline fun <D> CoroutineScope.request(
    callback: CommonCallback<D>,
    crossinline block: suspend () -> BaseResp<D>?
) {
    this.launch(exceptionHandler(callback) + Dispatchers.IO) {
        val response = async {
            block.invoke()
        }
        response.await().nullOrNot({
            callback.error(ErrorResponse.analysisError())
        }) {
            it.process(callback)
        }
    }
}

inline fun <T, D> BaseViewModel.request(
    resLiveData: ResLiveData<T>,
    callback: LiveDataCallback<T, D>,
    crossinline block: suspend () -> BaseResp<D>?
) {
    viewModelScope.launch(exceptionHandler(resLiveData, callback) + Dispatchers.IO) {
        val response = async {
            block.invoke()
        }
        response.await().nullOrNot({
            callback.error(resLiveData, ErrorResponse.analysisError())
        }) {
            it.process(resLiveData, callback)
        }
    }
}

inline fun BaseViewModel.post(crossinline block: () -> Unit) {
    viewModelScope.post(block)
}

inline fun CoroutineScope.post(crossinline block: () -> Unit) {
    this.launch(Dispatchers.Main.immediate) {
        block.invoke()
    }
}

fun <T, D> exceptionHandler(
    resLiveData: ResLiveData<T>,
    callback: LiveDataCallback<T, D>?
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
        callback?.error(resLiveData, getErrorResponse(e))
    }
}

fun <D> exceptionHandler(
    callback: CommonCallback<D>?
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
        callback?.error(getErrorResponse(e))
    }
}

private fun getErrorResponse(e: Throwable): ErrorResponse {
    Logger.e(e, "网络请求发生异常")
    val response = when (e) {
        is ErrorResponse -> e

        is ConnectException, is UnknownHostException -> {
            // 网络错误
            ErrorResponse(
                ErrorType.NETWORK_ERROR,
                -1,
                ResourceUtil.getString(R.string.network_error_retry_hint)
            )
        }
        is HttpException -> {
            // 服务器异常
            ErrorResponse(
                ErrorType.SERVICE_ERROR,
                e.code(),
                ResourceUtil.getString(R.string.network_error_retry_timeout)
            )
        }
        is JsonParseException, is MalformedJsonException, is EOFException -> {
            // 其它异常
            ErrorResponse(
                ErrorType.OTHER_ERROR,
                -2, ResourceUtil.getString(R.string.network_unknow_error)
            )
        }
        else -> {
            // 其它异常
            ErrorResponse(
                ErrorType.OTHER_ERROR,
                -3,
                ResourceUtil.getString(R.string.network_unknow_error)
            )
        }
    }
    response.message.toast()
    return response
}
