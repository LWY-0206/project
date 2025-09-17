package com.jxdx.square.adapter

import com.example.corekit.http.HttpManager
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
    ): BaseResp<ArrayList<CommentItem>> = service.getComments("6a819474-1cf4-42b2-b12e-794c4b472820", postId, page, size)

    suspend fun postComment(commentBody: CommentBody): BaseResp<Unit> =
        service.postComment("6a819474-1cf4-42b2-b12e-794c4b472820", commentBody)
}
