package com.jxdx.mine.homework

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityHomeworkBinding
import com.jxdx.mine.databinding.ItemHomeworkBinding

class HomeworkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeworkBinding
    private lateinit var homeworkAdapter: HomeworkAdapter
    private val homeworkList = mutableListOf<Homework>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadHomeworkData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)                 //将自定义的 Toolbar 设置为应用的 ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)    // 显示返回按钮（左上角的箭头）
//        supportActionBar?.setDisplayShowTitleEnabled(false)  // 禁用标题显示
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }// 点击返回按钮时执行返回操作
    }

    private fun setupRecyclerView() {
        homeworkAdapter = HomeworkAdapter(homeworkList)
        binding.rVHomework.layoutManager = LinearLayoutManager(this)
        binding.rVHomework.adapter = homeworkAdapter
    }

    private fun loadHomeworkData() {
        // 模拟数据
        homeworkList.apply {
            add(Homework(
                "Android开发作业1",
                "完成登录界面设计",
                "张老师",
                "2024-01-15",
                "未提交",
                R.drawable.homework
            ))
            add(Homework(
                "数据结构实验报告",
                "实现二叉树遍历算法",
                "李老师",
                "2024-01-20",
                "已提交",
                R.drawable.homework
            ))
            add(Homework(
                "Web开发项目",
                "完成电商网站前端",
                "王老师",
                "2024-01-25",
                "待批改",
                R.drawable.homework
            ))
        }
        homeworkAdapter.notifyDataSetChanged()
    }

    inner class HomeworkAdapter(private val homeworkList: List<Homework>) :
        RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder>() {

        inner class HomeworkViewHolder(val binding: ItemHomeworkBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
            val binding = ItemHomeworkBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return HomeworkViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
            val homework = homeworkList[position]
            holder.binding.apply {
                tvHomeworkTitle.text = homework.title
                tvHomeworkDesc.text = homework.description
                tvTeacher.text = "布置老师: ${homework.teacher}"
                tvDeadline.text = "截止日期: ${homework.deadline}"
                tvStatus.text = homework.status

                // 根据状态设置颜色
                when (homework.status) {
                    "未提交" -> tvStatus.setTextColor(ContextCompat.getColor(this@HomeworkActivity, R.color.red_600))
                    "已提交" -> tvStatus.setTextColor(ContextCompat.getColor(this@HomeworkActivity, R.color.green_dark))
                    "待批改" -> tvStatus.setTextColor(ContextCompat.getColor(this@HomeworkActivity, R.color.orange_dark))
                }

                root.setOnClickListener {
                    // 跳转到作业详情页
                    val intent = Intent(this@HomeworkActivity, HomeworkDetailActivity::class.java)
                    intent.putExtra("homework", homework.description)
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount() = homeworkList.size
    }
}

data class Homework(
    val title: String,
    val description: String,
    val teacher: String,
    val deadline: String,
    val status: String,
    val iconRes: Int
)