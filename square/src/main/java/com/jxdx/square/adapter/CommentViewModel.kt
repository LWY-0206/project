package com.jxdx.square.adapter

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.square.entity.CommentItem

class CommentViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: CommentRepository by lazy {
        Log.d("TAG", ":2 ")
        CommentRepository()
    }
    val commentLiveData: ResLiveData<ArrayList<CommentItem>> by lazy { ResLiveData() }

    fun getComments(
        postId: Int,
        page: Int,
        size: Int,
    ) {
        /**
         * @param questionsLiveData 用于向观察者发送响应结果的 [ResLiveData] 对象。
         * @param LiveDataCallback 响应结果的回调接口。
         * @param requestLambda 发送请求的 Lambda 表达式。
         */
        request(
            commentLiveData,
            object : LiveDataCallback<ArrayList<CommentItem>, ArrayList<CommentItem>> {
                override fun success(
                    emit: ResLiveData<ArrayList<CommentItem>>,
                    msg: String?,
                    data: ArrayList<CommentItem>?,
                ) {
                    // 直接使用返回的评论列表，如果为空则返回空列表
                    emit.success(data ?: ArrayList())
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<CommentItem>>,
                    code: Int?,
                    msg: String?,
                    data: ArrayList<CommentItem>?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<ArrayList<CommentItem>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.getComments(postId, page, size)
            },
        )
    }
}
