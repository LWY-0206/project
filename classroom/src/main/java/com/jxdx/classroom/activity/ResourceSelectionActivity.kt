package com.jxdx.classroom.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.R
import com.jxdx.classroom.adapter.ResourceSelectionAdapter
import com.jxdx.classroom.databinding.ActivityResourceSelectionBinding
import com.jxdx.classroom.entity.ResourceItem
import com.jxdx.classroom.viewmodel.ResourceSelectionViewModel

/**
 * 资料选择Activity
 * 教师可以从这里选择要发布的资料
 */
class ResourceSelectionActivity : BaseActivity<ActivityResourceSelectionBinding>() {
    
    private val TAG = "ResourceSelectionActivity"
    
    // 房间ID
    private var roomId: String = ""
    
    // ViewModel
    private lateinit var viewModel: ResourceSelectionViewModel
    
    // 适配器
    private lateinit var adapter: ResourceSelectionAdapter
    
    // 选中的资料列表
    private val selectedResources = mutableListOf<ResourceItem>()
    
    override fun bindLayout(): ActivityResourceSelectionBinding {
        return ActivityResourceSelectionBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        Log.d(TAG, "ResourceSelectionActivity初始化开始")
        
        // 获取传递的参数
        roomId = intent.getStringExtra("roomId") ?: ""
        Log.d(TAG, "房间ID: $roomId")
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[ResourceSelectionViewModel::class.java]
        
        // 设置标题
        view.tvTitle.text = "发布资料"
        
        // 设置返回按钮
        view.ivBack.setOnClickListener {
            finish()
        }
        
        // 设置发布按钮
        view.btnPublish.setOnClickListener {
            publishSelectedResources()
        }
        
        // 初始化RecyclerView
        initRecyclerView()
        
        // 观察数据变化
        observeData()
        
        // 加载资料数据
        viewModel.loadResources(this)
    }
    
    override fun subscribeUi() {
        // 可以在这里添加其他UI订阅
    }
    
    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = ResourceSelectionAdapter { resourceItem, isSelected ->
            handleResourceSelection(resourceItem, isSelected)
        }
        
        view.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ResourceSelectionActivity, 2)
            adapter = this@ResourceSelectionActivity.adapter
        }
    }
    
    /**
     * 观察数据变化
     */
    private fun observeData() {
        viewModel.resources.observe(this) { resources ->
            adapter.updateData(resources)
            updatePublishButton()
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            view.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 处理资料选择
     */
    private fun handleResourceSelection(resourceItem: ResourceItem, isSelected: Boolean) {
        if (isSelected) {
            if (!selectedResources.contains(resourceItem)) {
                selectedResources.add(resourceItem)
            }
        } else {
            selectedResources.remove(resourceItem)
        }
        
        updatePublishButton()
        Log.d(TAG, "选中资料数量: ${selectedResources.size}")
    }
    
    /**
     * 更新发布按钮状态
     */
    private fun updatePublishButton() {
        val hasSelection = selectedResources.isNotEmpty()
        view.btnPublish.isEnabled = hasSelection
        view.btnPublish.text = if (hasSelection) "发布(${selectedResources.size})" else "发布"
    }
    
    /**
     * 发布选中的资料
     */
    private fun publishSelectedResources() {
        if (selectedResources.isEmpty()) {
            Toast.makeText(this, "请选择要发布的资料", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示加载状态
        view.btnPublish.isEnabled = false
        view.btnPublish.text = "发布中..."
        
        // 调用ViewModel发布资料
        viewModel.publishResources(roomId, selectedResources) { success, message ->
            runOnUiThread {
                view.btnPublish.isEnabled = true
                updatePublishButton()
                
                if (success) {
                    Toast.makeText(this, "资料发布成功", Toast.LENGTH_SHORT).show()
                    // 返回结果
                    val resultIntent = Intent().apply {
                        putExtra("publishedResources", selectedResources.size)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, message ?: "发布失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ResourceSelectionActivity销毁")
    }
}
