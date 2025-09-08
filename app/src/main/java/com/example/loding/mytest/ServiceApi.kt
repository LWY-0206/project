package com.example.loding.mytest

import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.QuestionData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// 定义网络请求接口
// * 获取问题列表
// * @param start 起始位置（分页用，如第1条、第6条等）
// * @param size 每页数量（如5条/页）
// * @param courseType 课程类型（根据后端定义，可能是课程类型等）
// * @param showType 显示类型（根据后端定义，可能是题目类型等）
// * @param isRand 是否随机（0-否，1-是）
interface ServiceApi {
    @GET("question/byPage")
    suspend fun getQuestions(
        @Query("start") start: Int,
        @Query("size") size: Int,
        @Query("courseType") courseType: Int,
        @Query("showType") showType: Int,
        @Query("isRand") isRand: Int,
    ): BaseResp<QuestionData>

    @POST("question/byIds")
    suspend fun getQuestionsByIds(
        @Query("start") start: Int = 1,
        @Query("size") size: Int = 5,
        @Body ids: List<Int>, // 请求体：ID集合
    ): BaseResp<QuestionResponse> // 响应类型，BaseResp为文档中定义的通用响应类
}
// 多个question集合的封装
