package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityHomeworkLibraryBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.teacherhomework.adapter.HomeworkLibraryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeworkLibraryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkLibraryBinding
    private lateinit var homeworkAdapter: HomeworkLibraryAdapter
    private val homeworkList = mutableListOf<HomeworkDetail>()
    private var currentTab = 0 // 0: 已发放, 1: 作业库
    private var isDeleteMode = false
    private val selectedHomeworkIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // 初始化RecyclerView
        homeworkAdapter = HomeworkLibraryAdapter(homeworkList) { homework ->
            if (!isDeleteMode) {
                // 非删除模式下点击作业项
                val intent = Intent(this, HomeworkDetailActivity::class.java)
                intent.putExtra("homeworkId", homework.id)
                intent.putExtra("homeworkTitle", homework.title)
                startActivity(intent)
            }
        }
        
        // 设置选择变化监听器
        homeworkAdapter.setOnSelectionChangeListener { homeworkId, isSelected ->
            onHomeworkSelected(homeworkId, isSelected)
        }
        
        binding.recyclerViewHomework.apply {
            layoutManager = LinearLayoutManager(this@HomeworkLibraryActivity)
            adapter = homeworkAdapter
        }
        
        // 初始化默认选中"已发放"标签
        switchTab(0)
    }

    private fun setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 分段控制器
        binding.tabIssued.setOnClickListener {
            switchTab(0)
        }
        
        binding.tabLibrary.setOnClickListener {
            switchTab(1)
        }

        // 删除按钮
        binding.btnDelete.setOnClickListener {
            if (isDeleteMode) {
                confirmDelete()
            } else {
                enterDeleteMode()
            }
        }

        // 创建作业按钮
        binding.fabCreateHomework.setOnClickListener {
            val intent = Intent(this, CreateHomeworkActivity::class.java)
            startActivity(intent)
        }
    }

    private fun switchTab(tabIndex: Int) {
        currentTab = tabIndex
        
        if (tabIndex == 0) {
            // 已发放
            binding.tabIssued.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabIssued.setTextColor(resources.getColor(android.R.color.white))
            binding.tabLibrary.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabLibrary.setTextColor(resources.getColor(R.color.primary_color))
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            // 作业库
            binding.tabLibrary.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabLibrary.setTextColor(resources.getColor(android.R.color.white))
            binding.tabIssued.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabIssued.setTextColor(resources.getColor(R.color.primary_color))
            binding.btnDelete.visibility = View.GONE
        }
        
        loadHomeworkData()
    }

    private fun enterDeleteMode() {
        isDeleteMode = true
        selectedHomeworkIds.clear()
        binding.btnDelete.setImageResource(R.drawable.ic_check)
        homeworkAdapter.setDeleteMode(true)
        Toast.makeText(this, "请选择要删除的作业", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete() {
        if (selectedHomeworkIds.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的作业", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示确认删除的对话框
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除选中的 ${selectedHomeworkIds.size} 个作业吗？删除后无法恢复。")
            .setPositiveButton("删除") { _, _ ->
                performDelete()
            }
            .setNegativeButton("取消") { _, _ ->
                exitDeleteMode()
            }
            .show()
    }

    private fun performDelete() {
        if (selectedHomeworkIds.isNotEmpty()) {
            // 显示加载状态
            Toast.makeText(this, "正在删除作业...", Toast.LENGTH_SHORT).show()
            
            // 将选中的作业ID用逗号连接
            val homeworkIdsString = selectedHomeworkIds.joinToString(",")
            
            // 调用作业库删除API
            RetrofitClient.apiService.deleteCreatedHomework(homeworkIdsString).enqueue(object : Callback<BaseResp<String>> {
                override fun onResponse(
                    call: Call<BaseResp<String>>,
                    response: Response<BaseResp<String>>
                ) {
                    if (response.isSuccessful && response.body()?.code == 0) {
                        Toast.makeText(this@HomeworkLibraryActivity, "成功删除 ${selectedHomeworkIds.size} 个作业", Toast.LENGTH_SHORT).show()
                        // 重新加载数据
                        loadHomeworkData()
                    } else {
                        val errorMsg = response.body()?.message ?: "删除失败"
                        Toast.makeText(this@HomeworkLibraryActivity, "删除失败: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                    exitDeleteMode()
                }

                override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                    Toast.makeText(this@HomeworkLibraryActivity, "网络异常，删除失败", Toast.LENGTH_SHORT).show()
                    exitDeleteMode()
                }
            })
        } else {
            Toast.makeText(this, "请先选择要删除的作业", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exitDeleteMode() {
        isDeleteMode = false
        selectedHomeworkIds.clear()
        binding.btnDelete.setImageResource(R.drawable.ic_delete)
        homeworkAdapter.setDeleteMode(false)
    }

    private fun loadHomeworkData() {
        // 根据当前tab加载不同的数据
        homeworkList.clear()
        
        if (currentTab == 0) {
            // 已发放的作业
            loadIssuedHomework()
        } else {
            // 作业库
            loadHomeworkLibrary()
        }
        
        homeworkAdapter.notifyDataSetChanged()
    }

    private fun loadIssuedHomework() {
        // 模拟已发放的作业数据
        homeworkList.add(
            HomeworkDetail(
                id = "1",
                title = "英语下",
                description = "完成英语下册第3单元练习",
                dueDate = "2025-09-21 08:37:47",
                submissions = mutableListOf()
            )
        )
        
        homeworkList.add(
            HomeworkDetail(
                id = "2", 
                title = "英语上",
                description = "完成英语上册第2单元练习",
                dueDate = "2025-09-24 16:36:42",
                submissions = mutableListOf()
            )
        )
    }

    private fun loadHomeworkLibrary() {
        // 模拟作业库数据
        homeworkList.add(
            HomeworkDetail(
                id = "lib1",
                title = "数学练习册",
                description = "数学基础练习",
                dueDate = "",
                submissions = mutableListOf()
            )
        )
        
        homeworkList.add(
            HomeworkDetail(
                id = "lib2",
                title = "语文阅读理解",
                description = "语文阅读理解练习",
                dueDate = "",
                submissions = mutableListOf()
            )
        )
    }

    fun onHomeworkSelected(homeworkId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedHomeworkIds.add(homeworkId)
        } else {
            selectedHomeworkIds.remove(homeworkId)
        }
    }
}
