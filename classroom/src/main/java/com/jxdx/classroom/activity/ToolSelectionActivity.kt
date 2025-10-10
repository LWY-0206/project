package com.jxdx.classroom.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.databinding.ActivityToolSelectionBinding
import com.jxdx.classroom.entity.TeachingTool
import com.jxdx.classroom.adapter.ToolSelectionAdapter
import com.jxdx.classroom.viewmodel.ToolSelectionViewModel

class ToolSelectionActivity : BaseActivity<ActivityToolSelectionBinding>() {

    private lateinit var toolAdapter: ToolSelectionAdapter
    private lateinit var viewModel: ToolSelectionViewModel
    
    private var subjectId: Int = 0
    private var teacherId: Int = 0
    private var subjectName: String = ""

    override fun bindLayout(): ActivityToolSelectionBinding {
        return ActivityToolSelectionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 接收Intent传递的数据
        intent?.let {
            subjectId = it.getIntExtra("subjectId", 0)
            teacherId = it.getIntExtra("teacherId", 0)
            subjectName = it.getStringExtra("subjectName") ?: ""
            Log.d("ToolSelectionActivity", "接收到的参数: subjectId=$subjectId, teacherId=$teacherId, subjectName=$subjectName")
        }

        // 设置标题
        if (subjectName.isNotEmpty()) {
            view.tvTitle.text = "$subjectName - 工具准备"
        }

        // 返回按钮
        view.btnBack.setOnClickListener {
            finish()
        }

        // 确认选择按钮
        view.btnConfirm.setOnClickListener {
            val selectedTools = toolAdapter.getSelectedTools()
            if (selectedTools.isEmpty()) {
                Toast.makeText(this, "请至少选择一个工具", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 保存选择的工具
            viewModel.saveSelectedTools(selectedTools)
            
            // 显示选择结果
            val toolNames = selectedTools.joinToString("、") { it.name }
            Toast.makeText(this, "已选择工具: $toolNames", Toast.LENGTH_LONG).show()
            
            // 返回上一页
            finish()
        }

        // 全选/取消全选按钮
        view.btnSelectAll.setOnClickListener {
            val isAllSelected = toolAdapter.isAllSelected()
            if (isAllSelected) {
                toolAdapter.clearSelection()
                view.btnSelectAll.text = "全选"
            } else {
                toolAdapter.selectAll()
                view.btnSelectAll.text = "取消全选"
            }
            updateConfirmButton()
        }

        // 初始化RecyclerView
        initRecyclerView()

        // 初始化ViewModel
        initViewModel()
    }

    private fun initRecyclerView() {
        // 工具选择列表
        toolAdapter = ToolSelectionAdapter { tool ->
            // 工具选择状态变化
            Log.d("ToolSelectionActivity", "工具选择状态变化: ${tool.name} -> ${tool.isSelected}")
            updateConfirmButton()
            updateSelectAllButton()
        }
        
        // 使用网格布局，每行显示2个工具
        view.rvTools.layoutManager = GridLayoutManager(this, 2)
        view.rvTools.adapter = toolAdapter
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ToolSelectionViewModel::class.java]
    }

    override fun subscribeUi() {
        // 观察工具数据
        viewModel.teachingTools.observe(this) { tools ->
            toolAdapter.updateTools(tools)
            updateConfirmButton()
            updateSelectAllButton()
        }

        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            view.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        // 观察错误信息
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        // 观察保存结果
        viewModel.saveResult.observe(this) { success ->
            if (success) {
                Log.d("ToolSelectionActivity", "工具选择保存成功")
                // 不在这里退出，让用户手动点击确认按钮
            }
        }

        // 加载工具数据
        viewModel.loadTeachingTools(subjectId)
    }

    private fun updateConfirmButton() {
        val selectedCount = toolAdapter.getSelectedTools().size
        view.btnConfirm.text = if (selectedCount > 0) "确认选择 ($selectedCount)" else "确认选择"
        view.btnConfirm.isEnabled = selectedCount > 0
    }

    private fun updateSelectAllButton() {
        val isAllSelected = toolAdapter.isAllSelected()
        view.btnSelectAll.text = if (isAllSelected) "取消全选" else "全选"
    }
}
