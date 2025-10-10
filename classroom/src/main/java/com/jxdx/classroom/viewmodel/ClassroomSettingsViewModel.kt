package com.jxdx.classroom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxdx.classroom.entity.ClassroomSetting
import kotlinx.coroutines.*

class ClassroomSettingsViewModel : ViewModel() {

    private val _classroomSettings = MutableLiveData<ClassroomSetting>()
    val classroomSettings: LiveData<ClassroomSetting> = _classroomSettings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadSettings(subjectId: Int, teacherId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 模拟网络请求延迟
                delay(500)
                
                // 模拟数据 - 这里可以从本地存储或服务器加载
                val settings = ClassroomSetting.getDefaultSettings(subjectId, teacherId)
                _classroomSettings.value = settings
                
                Log.d("ClassroomSettingsViewModel", "加载课堂设置成功")
            } catch (e: Exception) {
                _errorMessage.value = "加载课堂设置失败: ${e.message}"
                Log.e("ClassroomSettingsViewModel", "加载设置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSettings(settings: ClassroomSetting) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 模拟保存延迟
                delay(800)
                
                // 这里可以调用API保存设置
                Log.d("ClassroomSettingsViewModel", "保存课堂设置: $settings")
                
                // 模拟保存成功
                _saveResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = "保存课堂设置失败: ${e.message}"
                _saveResult.value = false
                Log.e("ClassroomSettingsViewModel", "保存设置失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
