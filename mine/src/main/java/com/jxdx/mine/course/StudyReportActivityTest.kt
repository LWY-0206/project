package com.jxdx.mine.course

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityStudyReportBinding

class StudyReportActivityTest : AppCompatActivity() {
    
    private lateinit var binding: ActivityStudyReportBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initView()
        setupRecyclerView()
    }
    
    private fun initView() {
        // 设置返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // 设置课程名称
        val courseName = intent.getStringExtra("courseName") ?: "软件工程导论"
        binding.tvCourseName.text = courseName
        
        // 隐藏进度指示器，简化界面
        binding.progressIndicator.visibility = android.view.View.GONE
    }
    
    private fun setupRecyclerView() {
        // 创建一个简单的测试数据
        val testData = listOf(
            "学习概览 - 总学习时长: 48小时",
            "作业完成情况 - 已完成: 12/15",
            "出勤记录 - 出勤率: 96%",
            "考试成绩 - 平均分: 90分",
            "课堂参与 - 发言次数: 25次",
            "学习总结 - 掌握程度: 良好"
        )
        
        // 使用简单的ArrayAdapter
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = android.view.LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val textView = holder.itemView.findViewById<android.widget.TextView>(android.R.id.text1)
                textView.text = testData[position]
                textView.textSize = 16f
                textView.setPadding(32, 32, 32, 32)
            }
            
            override fun getItemCount(): Int = testData.size
        }
        
        binding.recyclerViewStudyReport.adapter = adapter
        
        // 使用简单的LinearLayoutManager
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewStudyReport.layoutManager = layoutManager
    }
}
