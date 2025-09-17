package com.jxdx.square.adapter

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class PostCommentViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: CommentRepository by lazy {
        Log.d("TAG", ":2 ")
        CommentRepository()
    }
    val postCommentLiveData: ResLiveData<Unit> by lazy { ResLiveData() }

    fun postComment(commentBody: CommentBody) {
        request(
            postCommentLiveData,
            object : LiveDataCallback<Unit, Unit> {
                override fun success(
                    emit: ResLiveData<Unit>,
                    msg: String?,
                    data: Unit?,
                ) {
                    emit.success(Unit)
                }

                override fun otherCode(
                    emit: ResLiveData<Unit>,
                    code: Int?,
                    msg: String?,
                    data: Unit?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<Unit>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.postComment(commentBody)
            },
        )
    }
}
