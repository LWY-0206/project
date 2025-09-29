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
     * @param isLoadMore 是否为加载更多
     */
    fun loadHomework(completeAndCorrect: Int, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!isLoadMore) currentPage = 1

                val homeworkList = repository.getHomeworkByStatus(completeAndCorrect, currentPage, pageSize)

                if (isLoadMore) {
                    val currentList = _homeworkLiveData.value?.toMutableList() ?: mutableListOf()
                    homeworkList?.let { currentList.addAll(it) }
                    _homeworkLiveData.postValue(currentList)
                } else {
                    _homeworkLiveData.postValue(homeworkList)
                }

                currentPage++
            } catch (e: Exception) {
                Log.e("HomeworkViewModel", "加载作业失败", e)
                _homeworkLiveData.postValue(emptyList())
            }
        }
    }
}