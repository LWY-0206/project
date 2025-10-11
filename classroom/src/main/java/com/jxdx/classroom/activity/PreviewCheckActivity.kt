package com.jxdx.classroom.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.databinding.ActivityPreviewCheckBinding
import com.jxdx.classroom.entity.PreviewTask
import com.jxdx.classroom.entity.StudentPreviewStatus
import com.jxdx.classroom.adapter.PreviewTaskAdapter
import com.jxdx.classroom.adapter.StudentPreviewAdapter
import com.jxdx.classroom.viewmodel.PreviewCheckViewModel

class PreviewCheckActivity : BaseActivity<ActivityPreviewCheckBinding>() {

    private lateinit var previewTaskAdapter: PreviewTaskAdapter
    private lateinit var studentPreviewAdapter: StudentPreviewAdapter
    private lateinit var viewModel: PreviewCheckViewModel
    
    private var subjectId: Int = 0
    private var teacherId: Int = 0
    private var subjectName: String = ""

    override fun bindLayout(): ActivityPreviewCheckBinding {
        return ActivityPreviewCheckBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 接收Intent传递的数据
        intent?.let {
            subjectId = it.getIntExtra("subjectId", 0)
            teacherId = it.getIntExtra("teacherId", 0)
            subjectName = it.getStringExtra("subjectName") ?: ""
            Log.d("PreviewCheckActivity", "接收到的参数: subjectId=$subjectId, teacherId=$teacherId, subjectName=$subjectName")
        }

        // 设置标题
        if (subjectName.isNotEmpty()) {
            view.tvTitle.text = "$subjectName - 预习查看"
        }

        // 返回按钮
        view.btnBack.setOnClickListener {
            finish()
        }

        // 添加预习任务按钮
        view.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // 初始化RecyclerView
        initRecyclerView()

        // 初始化ViewModel
        initViewModel()

        // 设置Tab切换
        setupTabSwitching()
    }

    private fun initRecyclerView() {
        // 预习任务列表
        previewTaskAdapter = PreviewTaskAdapter { task ->
            // 点击任务，显示任务详情对话框
            showTaskDetailDialog(task)
        }
        
        view.rvPreviewTasks.layoutManager = LinearLayoutManager(this)
        view.rvPreviewTasks.adapter = previewTaskAdapter

        // 学生预习情况列表
        studentPreviewAdapter = StudentPreviewAdapter { student ->
            // 点击学生，查看详细预习情况
            showStudentDetailDialog(student)
        }
        
        view.rvStudentPreview.layoutManager = LinearLayoutManager(this)
        view.rvStudentPreview.adapter = studentPreviewAdapter
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[PreviewCheckViewModel::class.java]
    }

    private fun setupTabSwitching() {
        view.tabPreviewTasks.setOnClickListener {
            view.tabPreviewTasks.isChecked = true
            view.tabStudentPreview.isChecked = false
            view.rvPreviewTasks.visibility = android.view.View.VISIBLE
            view.rvStudentPreview.visibility = android.view.View.GONE
            view.btnAddTask.visibility = android.view.View.VISIBLE
        }

        view.tabStudentPreview.setOnClickListener {
            view.tabPreviewTasks.isChecked = false
            view.tabStudentPreview.isChecked = true
            view.rvPreviewTasks.visibility = android.view.View.GONE
            view.rvStudentPreview.visibility = android.view.View.VISIBLE
            view.btnAddTask.visibility = android.view.View.GONE
        }
        
        // 设置默认选中状态
        view.tabPreviewTasks.isChecked = true
        view.tabStudentPreview.isChecked = false
    }

    override fun subscribeUi() {
        // 观察预习任务数据
        viewModel.previewTasks.observe(this) { tasks ->
            previewTaskAdapter.updateTasks(tasks)
            updateTaskStatistics(tasks)
        }

        // 观察学生预习状态数据
        viewModel.studentPreviewStatus.observe(this) { students ->
            studentPreviewAdapter.updateStudents(students)
            updateStudentStatistics(students)
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

        // 加载数据
        viewModel.loadPreviewTasks(subjectId, teacherId)
    }

    private fun updateTaskStatistics(tasks: List<PreviewTask>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val avgCompletionRate = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

        view.tvTaskStats.text = "总任务: $totalTasks | 已完成: $completedTasks | 完成率: $avgCompletionRate%"
    }

    private fun updateStudentStatistics(students: List<StudentPreviewStatus>) {
        val totalStudents = students.size
        val completedStudents = students.count { it.completionRate >= 80 }
        val avgCompletionRate = if (totalStudents > 0) students.map { it.completionRate }.average().toInt() else 0

        view.tvStudentStats.text = "总学生: $totalStudents | 完成率≥80%: $completedStudents | 平均完成率: $avgCompletionRate%"
    }

    private fun showAddTaskDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("添加预习任务")
            .setView(createAddTaskView())
            .setPositiveButton("添加") { _, _ ->
                // 这里可以添加保存任务的逻辑
                Toast.makeText(this, "预习任务添加功能开发中", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .create()
        
        dialog.show()
    }

    private fun createAddTaskView(): android.view.View {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val titleInput = android.widget.EditText(this).apply {
            hint = "任务标题"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val contentInput = android.widget.EditText(this).apply {
            hint = "任务内容"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(titleInput)
        layout.addView(contentInput)
        return layout
    }

    private fun showTaskDetailDialog(task: PreviewTask) {
        val message = """
            任务标题: ${task.title}
            
            任务内容: ${task.content}
            
            发布时间: ${task.publishTime}
            截止时间: ${task.deadline}
            完成率: ${task.completionRate}%
            状态: ${if (task.isCompleted) "已完成" else "进行中"}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("任务详情")
            .setMessage(message)
            .setPositiveButton("查看学生情况") { _, _ ->
                // 切换到学生情况标签并加载数据
                view.tabStudentPreview.isChecked = true
                view.tabPreviewTasks.isChecked = false
                view.rvPreviewTasks.visibility = android.view.View.GONE
                view.rvStudentPreview.visibility = android.view.View.VISIBLE
                view.btnAddTask.visibility = android.view.View.GONE
                
                // 加载该任务的学生预习情况
                viewModel.getStudentPreviewStatus(task.id)
            }
            .setNegativeButton("确定", null)
            .show()
    }

    private fun showStudentDetailDialog(student: StudentPreviewStatus) {
        val message = """
            学生姓名: ${student.studentName}
            学号: ${student.studentId}
            完成率: ${student.completionRate}%
            预习时间: ${student.previewTime}
            最后更新: ${student.lastUpdateTime}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("学生预习详情")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
}
