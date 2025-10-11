package com.jxdx.classroom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxdx.classroom.R
import com.jxdx.classroom.entity.TeachingTool
import com.jxdx.classroom.entity.ToolCategory
import kotlinx.coroutines.*

class ToolSelectionViewModel : ViewModel() {

    private val _teachingTools = MutableLiveData<List<TeachingTool>>()
    val teachingTools: LiveData<List<TeachingTool>> = _teachingTools

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadTeachingTools(subjectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 模拟网络请求延迟
                delay(800)
                
                // 模拟数据
                val mockTools = generateMockTools(subjectId)
                _teachingTools.value = mockTools
            } catch (e: Exception) {
                _errorMessage.value = "加载教学工具失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSelectedTools(selectedTools: List<TeachingTool>) {
        viewModelScope.launch {
            try {
                // 模拟保存延迟
                delay(500)
                
                // 这里可以调用API保存选择的工具
                Log.d("ToolSelectionViewModel", "保存选择的工具: ${selectedTools.map { it.name }}")
                
                _saveResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = "保存工具选择失败: ${e.message}"
                _saveResult.value = false
            }
        }
    }

    private fun generateMockTools(subjectId: Int): List<TeachingTool> {
        return listOf(
            TeachingTool(
                id = 1,
                name = "数学工具",
                description = "几何绘图、函数图像、计算器等",
                iconResId = R.drawable.ic_math_tool,
                category = ToolCategory.MATH,
                isAvailable = true
            ),
            TeachingTool(
                id = 2,
                name = "白板工具",
                description = "手写绘图、文字标注、图形绘制",
                iconResId = R.drawable.ic_whiteboard,
                category = ToolCategory.DRAWING,
                isAvailable = true
            ),
            TeachingTool(
                id = 3,
                name = "屏幕共享",
                description = "分享屏幕内容、演示操作",
                iconResId = R.drawable.ic_screen_share,
                category = ToolCategory.PRESENTATION,
                isAvailable = true
            ),
            TeachingTool(
                id = 4,
                name = "互动答题",
                description = "实时答题、投票、小测验",
                iconResId = R.drawable.ic_quiz,
                category = ToolCategory.INTERACTION,
                isAvailable = true
            ),
            TeachingTool(
                id = 5,
                name = "视频播放",
                description = "播放教学视频、动画演示",
                iconResId = R.drawable.ic_video_play,
                category = ToolCategory.MEDIA,
                isAvailable = true
            ),
            TeachingTool(
                id = 6,
                name = "音频播放",
                description = "播放音频文件、语音讲解",
                iconResId = R.drawable.ic_audio,
                category = ToolCategory.MEDIA,
                isAvailable = true
            ),
            TeachingTool(
                id = 7,
                name = "文档展示",
                description = "展示PDF、PPT等文档",
                iconResId = R.drawable.ic_document,
                category = ToolCategory.PRESENTATION,
                isAvailable = true
            ),
            TeachingTool(
                id = 8,
                name = "小组讨论",
                description = "分组讨论、协作学习",
                iconResId = R.drawable.ic_group,
                category = ToolCategory.INTERACTION,
                isAvailable = true
            ),
            TeachingTool(
                id = 9,
                name = "思维导图",
                description = "创建和编辑思维导图",
                iconResId = R.drawable.ic_mind_map,
                category = ToolCategory.DRAWING,
                isAvailable = false // 暂时不可用
            ),
            TeachingTool(
                id = 10,
                name = "3D模型",
                description = "展示3D模型和立体图形",
                iconResId = R.drawable.ic_3d_model,
                category = ToolCategory.MATH,
                isAvailable = false // 暂时不可用
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
