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
    ): BaseResp<ArrayList<CommentItem>> = service.getComments("e3c55f2f-023d-41ed-876b-d144db691e14", postId, page, size)

    suspend fun postComment(commentBody: CommentBody): BaseResp<Unit> =
        service.postComment("e3c55f2f-023d-41ed-876b-d144db691e14", commentBody)
}
