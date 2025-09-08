package com.example.loding.mytest

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.example.loding.entity.Question
import com.example.loding.entity.QuestionData

class QuestionViewModel(
    application: Application,
) : BaseViewModel(application) {
    // 于后续调用其提供的数据获取方法（如 getQuestions），实现 ViewModel 与数据仓库层的交互。
    private val repository: QuestionRepository by lazy {
        QuestionRepository()
    }

    // 封装了 ArrayList<Question> 类型数据的可观察数据容器，用于在 MVVM 架构中传递题目列表数据，同时可能包含加载状态、错误信息等附加信息。
    val questionsLiveData: ResLiveData<ArrayList<Question>> by lazy { ResLiveData() }

    /**
     * @param questionsLiveData 用于向观察者发送响应结果的 [ResLiveData] 对象。
     * @param LiveDataCallback 响应结果的回调接口。
     * @param requestLambda 发送请求的 Lambda 表达式。
     */
    fun getQuestions(
        start: Int,
        size: Int,
        courseType: Int,
        showType: Int,
        isRand: Int,
    ) {
        request(
            questionsLiveData,
            object : LiveDataCallback<ArrayList<Question>, QuestionData> { // 修改泛型参数
                override fun success(
                    emit: ResLiveData<ArrayList<Question>>,
                    msg: String?,
                    data: QuestionData?, // 单个对象
                ) {
                    val allQuestions = ArrayList<Question>()
                    data?.questions?.let { allQuestions.addAll(it) }
                    emit.success(allQuestions)
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<Question>>,
                    code: Int?,
                    msg: String?,
                    data: QuestionData?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<ArrayList<Question>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
        ) {
            repository.getQuestions(start, size, courseType, showType, isRand) // Lambda 移出括号
        }
    }

    fun getQuestionsByIds(
        start: Int,
        size: Int,
        ids: List<Int>,
    ) {
        request(
            questionsLiveData,
            object : LiveDataCallback<ArrayList<Question>, QuestionResponse> {
                override fun success(
                    emit: ResLiveData<ArrayList<Question>>,
                    msg: String?,
                    data: QuestionResponse?,
                ) {
                    val allQuestions = ArrayList<Question>()
                    data?.questions?.let { allQuestions.addAll(it) } // 修复：补充addAll参数
                    emit.success(allQuestions)
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<Question>>,
                    code: Int?,
                    msg: String?,
                    data: QuestionResponse?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<ArrayList<Question>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
        ) {
            repository.getQuestionsByIds(start, size, ids) // 补充请求 Lambda
        }
    }
}
