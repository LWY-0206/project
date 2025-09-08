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
    ): BaseResp<ArrayList<CommentItem>> = service.getComments(postId, page, size)
}
