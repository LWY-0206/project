package com.example.loding.mytest

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.QuestionData

class QuestionRepository {
    private val service: ServiceApi by lazy {
        HttpManager.instance.service(ServiceApi::class.java)
        // 这一参数的作用是让 Retrofit 根据接口类的定义（如 @GET 注解、方法参数等）生成该接口的实现类实例，从而通过实例调用接口中的网络请求方法（如 getQuestions）
    }

    // 在 Kotlin 中，suspend 是用于标记挂起函数的关键字，这类函数可以在不阻塞线程的情况下暂停执行，并在后续某个时刻恢复。
    suspend fun getQuestions(
        start: Int,
        size: Int,
        courseType: Int,
        showType: Int,
        isRand: Int,
    ): BaseResp<QuestionData> = service.getQuestions(start, size, courseType, showType, isRand)

    suspend fun getQuestionsByIds(
        start: Int,
        size: Int,
        ids: List<Int>,
    ): BaseResp<QuestionResponse> = service.getQuestionsByIds(start, size, ids)
}
// class BaseResp<T>(
//    val code: Int? = null,
//    val message: String? = null,
//    val data: T? = null,
// )传入进去T的话就就是正好凑出了标准的返回类
// 符合 Repository 层 “单纯转发网络请求结果” 的设计，无需额外转换即可将响应传递给 ViewModel 层处理。
// 该方法的返回类型为 BaseResp<QuestionData>。
