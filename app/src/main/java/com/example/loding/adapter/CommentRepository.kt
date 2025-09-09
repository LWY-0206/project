package com.example.loding.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.CommentItem

class CommentRepository {
    private val service: CommentApi by lazy {
        HttpManager.instance.service(CommentApi::class.java)
    }

    suspend fun getComments(
        postId: Int,
        page: Int,
        size: Int,
    ): BaseResp<ArrayList<CommentItem>> = service.getComments("eb811e0a-8289-4be1-9f99-41e7f8990775", postId, page, size)

    suspend fun postComment(commentBody: CommentBody): BaseResp<Unit> =
        service.postComment("eb811e0a-8289-4be1-9f99-41e7f8990775", commentBody)
}
