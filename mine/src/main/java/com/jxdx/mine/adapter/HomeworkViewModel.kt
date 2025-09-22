package com.jxdx.mine.adapter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jxdx.mine.homework.SubjectGroup
import kotlinx.coroutines.launch

class HomeworkViewModel(private val repository: HomeworkRepository) : ViewModel() {

    private val _homeworkLiveData = MutableLiveData<List<SubjectGroup>>()
    val homeworkLiveData: LiveData<List<SubjectGroup>> = _homeworkLiveData

    /**
     * 加载对应状态的作业
     */
    fun loadHomework(status: Int) {
        viewModelScope.launch {
            try {
                val homeworkList = repository.getHomeworkByStatus(status)
                Log.d("---",homeworkList.toString())
                _homeworkLiveData.postValue(homeworkList)
            } catch (e: Exception) {
                // 处理异常，这里可以添加错误日志或错误状态通知
                _homeworkLiveData.postValue(emptyList())
            }
        }
    }
}
