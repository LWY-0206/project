package com.example.loding.Schools

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class SchoolListViewModel(application: Application) : BaseViewModel(application) {
    private val repository: SchoolRepository by lazy {
        Log.d("SchoolListViewModel", "Initializing SchoolRepository")
        SchoolRepository()
    }

    val schoolLiveData: ResLiveData<SchoolData> by lazy {
        Log.d("SchoolListViewModel", "Initializing schoolLiveData")
        ResLiveData()
    }

    fun getSchools(query: String, page: Int, size: Int) {
        Log.d("SchoolListViewModel", "getSchools called with query=$query, page=$page, size=$size")

        // 自动判断搜索类型
        val isScoreSearch = isScoreQuery(query)

        if (isScoreSearch) {
            // 按成绩搜索
            try {
                val score = query.toInt()
                getSchoolsByScore(score, page, size)
            } catch (e: NumberFormatException) {
                Log.e("SchoolListViewModel", "成绩转换失败: $query", e)
                // 即使不是纯数字，也尝试按名称搜索
                getSchoolsByName(query, page, size)
            }
        } else {
            // 按名称搜索
            getSchoolsByName(query, page, size)
        }
    }

    private fun getSchoolsByName(schoolName: String, page: Int, size: Int) {
        Log.d("SchoolListViewModel", "按名称搜索: $schoolName, page=$page, size=$size")

        request(
            schoolLiveData,
            object : LiveDataCallback<SchoolData, SchoolData> {
                override fun success(
                    emit: ResLiveData<SchoolData>,
                    msg: String?,
                    data: SchoolData?
                ) {
                    Log.d("SchoolListViewModel", "按名称搜索成功: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<SchoolData>,
                    code: Int?,
                    msg: String?,
                    data: SchoolData?
                ) {
                    Log.w("SchoolListViewModel", "按名称搜索其他代码: code=$code, msg=$msg, data=$data")
                    // 即使是错误状态，如果有数据也返回
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<SchoolData>,
                    e: ErrorResponse
                ) {
                    Log.e("SchoolListViewModel", "按名称搜索错误: ${e.message},${emit.data}", e.cause)
                    // 即使是错误状态，如果有数据也返回
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("SchoolListViewModel", "执行按名称搜索仓库调用")
            repository.getSchoolsByName(schoolName, page, size)
        }
    }

    private fun getSchoolsByScore(score: Int, page: Int, size: Int) {
        Log.d("SchoolListViewModel", "按成绩搜索: $score, page=$page, size=$size")

        request(
            schoolLiveData,
            object : LiveDataCallback<SchoolData, SchoolData> {
                override fun success(
                    emit: ResLiveData<SchoolData>,
                    msg: String?,
                    data: SchoolData?
                ) {
                    Log.d("SchoolListViewModel", "按成绩搜索成功: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<SchoolData>,
                    code: Int?,
                    msg: String?,
                    data: SchoolData?
                ) {
                    Log.w("SchoolListViewModel", "按成绩搜索其他代码: code=$code, msg=$msg, data=$data")
                    // 即使是错误状态，如果有数据也返回
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<SchoolData>,
                    e: ErrorResponse
                ) {
                    Log.e("SchoolListViewModel", "按成绩搜索错误: ${e.message},${emit.data}", e.cause)
                    // 即使是错误状态，如果有数据也返回
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("SchoolListViewModel", "执行按成绩搜索仓库调用")
            repository.getSchoolsByScore(score, page, size)
        }
    }

    // 判断查询是否为成绩搜索
    private fun isScoreQuery(query: String): Boolean {
        // 空查询按名称处理
        if (query.isEmpty()) return false

        // 尝试转换为数字
        return try {
            query.toInt()
            // 如果是纯数字，则认为是成绩搜索
            true
        } catch (e: NumberFormatException) {
            // 如果不是纯数字，则按名称搜索
            false
        }
    }
}