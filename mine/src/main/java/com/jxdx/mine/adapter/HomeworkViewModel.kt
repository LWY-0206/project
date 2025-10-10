package com.jxdx.mine.adapter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jxdx.mine.SubjectGroup
import kotlinx.coroutines.launch
class HomeworkViewModel(private val repository: HomeworkRepository) : ViewModel() {

    private val _homeworkLiveData = MutableLiveData<List<SubjectGroup>?>()
    val homeworkLiveData: LiveData<List<SubjectGroup>> = _homeworkLiveData as LiveData<List<SubjectGroup>>

    private var currentPage = 1
    private val pageSize = 5

    /**
     * 加载作业列表
     * @param completeAndCorrect 作业状态：0 未提交，1 待批改，2 已完成
     * @param isLoadMore 是否为加载更多
     */
    fun loadHomework(completeAndCorrect: Int, isLoadMore: Boolean = false) {
        // 输出请求参数日志
        val statusText = when (completeAndCorrect) {
            0 -> "未提交"
            1 -> "待批改"
            2 -> "已完成"
            else -> "未知状态"
        }
        Log.d("HomeworkViewModel", "开始加载作业 - 状态: $completeAndCorrect($statusText), 是否加载更多: $isLoadMore")
        
        viewModelScope.launch {
            try {
                if (!isLoadMore) {
                    currentPage = 1
                    Log.d("HomeworkViewModel", "重新加载数据，重置页码为1")
                } else {
                    Log.d("HomeworkViewModel", "加载更多数据，当前页码: $currentPage")
                }

                Log.d("HomeworkViewModel", "调用仓库方法获取作业数据")
                val homeworkList = repository.getHomeworkByStatus(completeAndCorrect, currentPage, pageSize)

                // 处理返回结果
                val resultCount = homeworkList?.size ?: 0
                Log.d("HomeworkViewModel", "获取作业数据成功，返回分组数量: $resultCount")
                
                if (isLoadMore) {
                    val currentList = _homeworkLiveData.value?.toMutableList() ?: mutableListOf()
                    homeworkList?.let { currentList.addAll(it) }
                    _homeworkLiveData.postValue(currentList)
                    Log.d("HomeworkViewModel", "加载更多完成，当前总分组数量: ${currentList.size}")
                } else {
                    _homeworkLiveData.postValue(homeworkList)
                    Log.d("HomeworkViewModel", "首次加载完成，返回分组数量: $resultCount")
                }

                currentPage++
                Log.d("HomeworkViewModel", "页码更新为: $currentPage")
            } catch (e: Exception) {
                Log.e("HomeworkViewModel", "加载作业失败: ${e.message}", e)
                _homeworkLiveData.postValue(emptyList())
            }
        }
    }
}