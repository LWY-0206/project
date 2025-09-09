package com.example.loding.adapter

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.CommentItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentApi {
    @GET("square/post/comment")
    suspend fun getComments(
        @Header("satoken") source: String,
        @Query("postId") postId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): BaseResp<ArrayList<CommentItem>>

    @POST("square/post/comment")
    suspend fun postComment(
        @Header("satoken") source: String,
        @Body commentBody: CommentBody,
    ): BaseResp<Unit>
}
