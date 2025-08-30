package com.example.corekit.http.listener

import com.example.corekit.http.bean.ErrorResponse

interface CommonCallback<Response> {
    fun success(msg: String?, data: Response?)
    fun otherCode(code: Int?, msg: String?, data: Response?)
    fun error(e: ErrorResponse? = null)
}
