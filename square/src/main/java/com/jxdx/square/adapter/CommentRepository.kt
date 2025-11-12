package com.jxdx.square.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.CommentItem

class CommentRepository {
    private val service: CommentApi by lazy {
        HttpManager.instance.service(CommentApi::class.java)
    }

    suspend fun getComments(
        postId: Int,
        page: Int,
        size: Int,
    ): BaseResp<ArrayList<CommentItem>> = service.getComments(TokenManager.getToken().toString(), postId, page, size)

    suspend fun postComment(commentBody: CommentBody): BaseResp<Unit> =
        service.postComment(TokenManager.getToken().toString(), commentBody)
}