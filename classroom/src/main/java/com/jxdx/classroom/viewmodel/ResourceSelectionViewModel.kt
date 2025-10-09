package com.jxdx.classroom.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxdx.classroom.entity.ResourceItem
import kotlinx.coroutines.*
import org.json.JSONArray

/**
 * 资料选择ViewModel
 * 管理资料数据的状态和业务逻辑
 */
class ResourceSelectionViewModel : ViewModel() {
    
    private val _resources = MutableLiveData<List<ResourceItem>>()
    val resources: LiveData<List<ResourceItem>> = _resources
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // SharedPreferences相关常量
    private val PREFS_NAME = "exercise_images"
    private val KEY_IMAGE_URLS = "image_urls"
    
    /**
     * 加载资料数据
     */
    fun loadResources(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                // 从SharedPreferences加载本地保存的图片
                val localResources = loadLocalResources(context)
                _resources.value = localResources
                
            } catch (e: Exception) {
                _errorMessage.value = "加载资料失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 发布资料
     */
    fun publishResources(
        roomId: String,
        selectedResources: List<ResourceItem>,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 模拟发布请求
                delay(2000)
                
                // 模拟发布结果
                val success = true // 这里可以根据实际API调用结果设置
                val message = if (success) "发布成功" else "发布失败"
                
                callback(success, message)
                
            } catch (e: Exception) {
                callback(false, "发布失败: ${e.message}")
            }
        }
    }
    
    /**
     * 从SharedPreferences加载本地保存的图片资源
     */
    private fun loadLocalResources(context: Context): List<ResourceItem> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val imageUrlsJson = sharedPreferences.getString(KEY_IMAGE_URLS, null)
        
        if (imageUrlsJson.isNullOrEmpty()) {
            return emptyList()
        }
        
        return try {
            val jsonArray = JSONArray(imageUrlsJson)
            val resources = mutableListOf<ResourceItem>()
            
            for (i in 0 until jsonArray.length()) {
                val imageUrl = jsonArray.getString(i)
                val resourceItem = ResourceItem(
                    id = "local_$i",
                    name = "课前资料_${i + 1}.jpg",
                    type = ResourceItem.Type.IMAGE,
                    url = imageUrl,
                    thumbnailUrl = imageUrl,
                    size = 0, // 本地图片大小未知
                    description = "课前上传的资料图片"
                )
                resources.add(resourceItem)
            }
            
            resources
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 创建模拟数据（备用）
     */
    private fun createMockResources(): List<ResourceItem> {
        return listOf(
            ResourceItem(
                id = "mock_1",
                name = "示例文档.pdf",
                type = ResourceItem.Type.DOCUMENT,
                url = "https://example.com/sample.pdf",
                size = 2048000,
                description = "示例文档"
            )
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
