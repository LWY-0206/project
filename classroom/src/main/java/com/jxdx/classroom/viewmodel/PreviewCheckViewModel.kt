package com.jxdx.classroom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxdx.classroom.entity.PreviewStatus
import com.jxdx.classroom.entity.PreviewTask
import com.jxdx.classroom.entity.StudentPreviewStatus
import kotlinx.coroutines.*

class PreviewCheckViewModel : ViewModel() {

    private val _previewTasks = MutableLiveData<List<PreviewTask>>()
    val previewTasks: LiveData<List<PreviewTask>> = _previewTasks

    private val _studentPreviewStatus = MutableLiveData<List<StudentPreviewStatus>>()
    val studentPreviewStatus: LiveData<List<StudentPreviewStatus>> = _studentPreviewStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadPreviewTasks(subjectId: Int, teacherId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 模拟网络请求延迟
                delay(1000)
                
                // 模拟数据
                val mockTasks = generateMockTasks(subjectId, teacherId)
                _previewTasks.value = mockTasks
            } catch (e: Exception) {
                _errorMessage.value = "加载预习任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getStudentPreviewStatus(taskId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 模拟网络请求延迟
                delay(800)
                
                // 模拟数据
                val mockStudents = generateMockStudents()
                _studentPreviewStatus.value = mockStudents
            } catch (e: Exception) {
                _errorMessage.value = "加载学生预习情况失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateMockTasks(subjectId: Int, teacherId: Int): List<PreviewTask> {
        return listOf(
            PreviewTask(
                id = 1,
                title = "第一章：函数与极限预习",
                content = "请预习函数的概念、极限的定义和计算方法，重点理解极限的几何意义...",
                publishTime = "2024-01-15",
                deadline = "2024-01-20",
                completionRate = 85,
                isCompleted = false,
                subjectId = subjectId,
                teacherId = teacherId
            ),
            PreviewTask(
                id = 2,
                title = "第二章：导数与微分预习",
                content = "预习导数的定义、几何意义、基本求导法则，理解微分的概念...",
                publishTime = "2024-01-10",
                deadline = "2024-01-18",
                completionRate = 92,
                isCompleted = true,
                subjectId = subjectId,
                teacherId = teacherId
            ),
            PreviewTask(
                id = 3,
                title = "第三章：积分学预习",
                content = "预习不定积分和定积分的概念、性质和计算方法...",
                publishTime = "2024-01-08",
                deadline = "2024-01-16",
                completionRate = 78,
                isCompleted = false,
                subjectId = subjectId,
                teacherId = teacherId
            ),
            PreviewTask(
                id = 4,
                title = "第四章：多元函数预习",
                content = "预习多元函数的概念、偏导数、全微分等基本概念...",
                publishTime = "2024-01-05",
                deadline = "2024-01-14",
                completionRate = 65,
                isCompleted = false,
                subjectId = subjectId,
                teacherId = teacherId
            )
        )
    }

    private fun generateMockStudents(): List<StudentPreviewStatus> {
        return listOf(
            StudentPreviewStatus(
                studentId = "2024001",
                studentName = "张三",
                completionRate = 95,
                previewTime = "2小时30分钟",
                lastUpdateTime = "2024-01-18 14:30",
                status = PreviewStatus.EXCELLENT
            ),
            StudentPreviewStatus(
                studentId = "2024002",
                studentName = "李四",
                completionRate = 88,
                previewTime = "2小时15分钟",
                lastUpdateTime = "2024-01-18 13:45",
                status = PreviewStatus.GOOD
            ),
            StudentPreviewStatus(
                studentId = "2024003",
                studentName = "王五",
                completionRate = 75,
                previewTime = "1小时45分钟",
                lastUpdateTime = "2024-01-18 12:20",
                status = PreviewStatus.AVERAGE
            ),
            StudentPreviewStatus(
                studentId = "2024004",
                studentName = "赵六",
                completionRate = 45,
                previewTime = "45分钟",
                lastUpdateTime = "2024-01-17 16:10",
                status = PreviewStatus.POOR
            ),
            StudentPreviewStatus(
                studentId = "2024005",
                studentName = "钱七",
                completionRate = 92,
                previewTime = "2小时20分钟",
                lastUpdateTime = "2024-01-18 15:00",
                status = PreviewStatus.EXCELLENT
            ),
            StudentPreviewStatus(
                studentId = "2024006",
                studentName = "孙八",
                completionRate = 82,
                previewTime = "1小时55分钟",
                lastUpdateTime = "2024-01-18 11:30",
                status = PreviewStatus.GOOD
            ),
            StudentPreviewStatus(
                studentId = "2024007",
                studentName = "周九",
                completionRate = 68,
                previewTime = "1小时20分钟",
                lastUpdateTime = "2024-01-17 20:15",
                status = PreviewStatus.AVERAGE
            ),
            StudentPreviewStatus(
                studentId = "2024008",
                studentName = "吴十",
                completionRate = 38,
                previewTime = "30分钟",
                lastUpdateTime = "2024-01-16 18:45",
                status = PreviewStatus.POOR
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
