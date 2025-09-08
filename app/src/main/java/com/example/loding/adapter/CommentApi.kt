package com.example.loding.adapter

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.CommentItem
import retrofit2.http.GET
import retrofit2.http.Query

interface CommentApi {
    @GET("square/post/comment")
    suspend fun getComments(
        @Query("postId") postId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): BaseResp<ArrayList<CommentItem>>
}
