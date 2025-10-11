package com.jxdx.mine.adapter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jxdx.mine.Homework
import com.jxdx.mine.SubjectGroup
import kotlinx.coroutines.launch
class HomeworkViewModel(private val repository: HomeworkRepository) : ViewModel() {

    private val _homeworkLiveData = MutableLiveData<List<SubjectGroup>?>()
    val homeworkLiveData: LiveData<List<SubjectGroup>> = _homeworkLiveData as LiveData<List<SubjectGroup>>

    private val _hasMoreData = MutableLiveData<Boolean>()
    val hasMoreData: LiveData<Boolean> = _hasMoreData

    private var currentPage = 1
    private val pageSize = 5
    private var totalPages = 0

    /**
     * 加载作业列表
     * @param completeAndCorrect 作业状态：0 未提交，1 待批改，2 已完成
     * @param isLoadMore 是否为加载更多
     */
    fun loadHomework(completeAndCorrect: Int, isLoadMore: Boolean = false) {
        // 检查是否还有更多数据
        if (isLoadMore && currentPage > totalPages && totalPages > 0) {
            Log.d("HomeworkViewModel", "没有更多数据可加载")
            return
        }
        
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
                    totalPages = 0
                    _hasMoreData.postValue(true)
                    Log.d("HomeworkViewModel", "重新加载数据，重置页码为1")
                } else {
                    Log.d("HomeworkViewModel", "加载更多数据，当前页码: $currentPage")
                }

                Log.d("HomeworkViewModel", "调用仓库方法获取作业数据")
                val result = repository.getHomeworkByStatus(completeAndCorrect, currentPage, pageSize)
                val homeworkList = result?.first
                val pages = result?.second ?: 0

                // 处理返回结果
                val resultCount = homeworkList?.size ?: 0
                Log.d("HomeworkViewModel", "获取作业数据成功，返回分组数量: $resultCount, 总页数: $pages")
                
                if (isLoadMore && homeworkList != null) {
                    val currentList = _homeworkLiveData.value?.toMutableList() ?: mutableListOf()
                    
                    // 合并相同科目的分组
                    val mergedList = mergeSameSubjectGroups(currentList, homeworkList)
                    
                    _homeworkLiveData.postValue(mergedList)
                    Log.d("HomeworkViewModel", "加载更多完成，合并后分组数量: ${mergedList.size}")
                } else {
                    _homeworkLiveData.postValue(homeworkList)
                    Log.d("HomeworkViewModel", "首次加载完成，返回分组数量: $resultCount")
                }

                // 更新分页状态
                totalPages = pages
                _hasMoreData.postValue(currentPage < totalPages)
                
                // 只有在请求成功后才递增页码
                currentPage++
                Log.d("HomeworkViewModel", "页码更新为: $currentPage, 总页数: $totalPages, 还有更多数据: ${currentPage <= totalPages}")
                
            } catch (e: Exception) {
                Log.e("HomeworkViewModel", "加载作业失败: ${e.message}", e)
                _homeworkLiveData.postValue(emptyList())
                _hasMoreData.postValue(false)
                // 请求失败时不递增页码
            }
        }
    }
    
    /**
     * 合并相同科目的分组
     */
    private fun mergeSameSubjectGroups(existingGroups: List<SubjectGroup>, newGroups: List<SubjectGroup>): List<SubjectGroup> {
        val mergedGroups = mutableListOf<SubjectGroup>()
        
        // 创建现有分组的映射，按科目名称索引
        val existingMap = existingGroups.associateBy { it.subjectName }
        
        // 处理新分组
        newGroups.forEach { newGroup ->
            val existingGroup = existingMap[newGroup.subjectName]
            
            if (existingGroup != null) {
                // 如果存在相同科目的分组，合并作业列表并去重
                val mergedHomeworkList = mutableListOf<Homework>()
                mergedHomeworkList.addAll(existingGroup.homeworkList)
                
                // 只添加不重复的作业（根据homeworkId去重）
                newGroup.homeworkList.forEach { newHomework ->
                    if (!existingGroup.homeworkList.any { it.homeworkId == newHomework.homeworkId }) {
                        mergedHomeworkList.add(newHomework)
                    }
                }
                
                // 创建新的合并分组
                mergedGroups.add(
                    SubjectGroup(
                        subjectName = newGroup.subjectName,
                        isExpanded = existingGroup.isExpanded, // 保留原有展开状态
                        homeworkList = mergedHomeworkList
                    )
                )
            } else {
                // 如果是新科目，直接添加
                mergedGroups.add(newGroup)
            }
        }
        
        // 添加未在新分组中的现有分组
        existingGroups.forEach { existingGroup ->
            if (!newGroups.any { it.subjectName == existingGroup.subjectName }) {
                mergedGroups.add(existingGroup)
            }
        }
        
        return mergedGroups
    }
}