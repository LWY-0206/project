package com.jxdx.mine.course

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityStudyReportBinding
import com.jxdx.mine.adapter.StudyReportAdapter
import com.jxdx.mine.adapter.StudyReportCard
import com.jxdx.mine.adapter.ChartData
import com.jxdx.mine.adapter.StudyReportCardType

class StudyReportActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStudyReportBinding
    private lateinit var adapter: StudyReportAdapter
    private var currentPosition = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initView()
        setupRecyclerView()
        loadStudyReportData()
    }
    
    private fun initView() {
        // 设置返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // 设置课程名称
        val courseName = intent.getStringExtra("courseName") ?: "软件工程导论"
        binding.tvCourseName.text = courseName
        
        // 设置进度指示器
        setupProgressIndicator()
    }
    
    private fun setupRecyclerView() {
        adapter = StudyReportAdapter()
        binding.recyclerViewStudyReport.adapter = adapter
        
        // 使用LinearLayoutManager实现垂直滑动
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewStudyReport.layoutManager = layoutManager
        
        // 暂时移除PagerSnapHelper，避免可能的兼容性问题
        // val snapHelper = PagerSnapHelper()
        // snapHelper.attachToRecyclerView(binding.recyclerViewStudyReport)
        
        // 监听滑动事件，更新进度指示器
        binding.recyclerViewStudyReport.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateProgressIndicator()
            }
        })
        
        // 添加平滑滚动动画
        binding.recyclerViewStudyReport.itemAnimator = null
    }
    
    private fun setupProgressIndicator() {
        // 初始化进度指示器
        binding.progressIndicator.removeAllViews()
        
        val cardCount = 6 // 总共有6张卡片
        for (i in 0 until cardCount) {
            val indicator = View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.indicator_width),
                    resources.getDimensionPixelSize(R.dimen.indicator_height)
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.indicator_margin)
                }
                background = getDrawable(R.drawable.bg_progress_indicator_inactive)
            }
            binding.progressIndicator.addView(indicator)
        }
        
        // 设置第一个指示器为激活状态
        updateProgressIndicator()
    }
    
    private fun updateProgressIndicator() {
        try {
            val layoutManager = binding.recyclerViewStudyReport.layoutManager as? LinearLayoutManager ?: return
            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            val firstVisibleView = layoutManager.findViewByPosition(firstVisiblePosition)
            
            if (firstVisibleView != null && firstVisibleView.height > 0) {
                val viewHeight = firstVisibleView.height
                val scrolledHeight = -firstVisibleView.top
                val progress = scrolledHeight.toFloat() / viewHeight.toFloat()
                
                // 根据滚动进度确定当前卡片
                val newPosition = if (progress > 0.5f) firstVisiblePosition + 1 else firstVisiblePosition
                
                if (newPosition != currentPosition && newPosition >= 0 && newPosition < binding.progressIndicator.childCount) {
                    currentPosition = newPosition
                    updateIndicatorAppearance()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateIndicatorAppearance() {
        try {
            for (i in 0 until binding.progressIndicator.childCount) {
                val indicator = binding.progressIndicator.getChildAt(i)
                if (indicator != null) {
                    if (i == currentPosition) {
                        indicator.background = getDrawable(R.drawable.bg_progress_indicator_active)
                        indicator.animate()
                            .scaleX(1.2f)
                            .scaleY(1.2f)
                            .setDuration(200)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    } else {
                        indicator.background = getDrawable(R.drawable.bg_progress_indicator_inactive)
                        indicator.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadStudyReportData() {
        // 创建学习报告数据
        val studyReportCards = listOf(
            StudyReportCard(
                type = StudyReportCardType.OVERVIEW,
                title = "学习概览",
                subtitle = "软件工程导论",
                data = mapOf(
                    "总学习时长" to "48小时",
                    "学习天数" to "30天",
                    "完成度" to "85%"
                ),
                chartData = listOf(
                    ChartData("第1周", 8.5f),
                    ChartData("第2周", 12.3f),
                    ChartData("第3周", 15.7f),
                    ChartData("第4周", 11.2f)
                )
            ),
            StudyReportCard(
                type = StudyReportCardType.HOMEWORK,
                title = "作业完成情况",
                subtitle = "Assignment Progress",
                data = mapOf(
                    "已完成" to "12/15",
                    "平均分" to "87分",
                    "按时提交率" to "95%"
                ),
                chartData = listOf(
                    ChartData("作业1", 85f),
                    ChartData("作业2", 92f),
                    ChartData("作业3", 78f),
                    ChartData("作业4", 90f),
                    ChartData("作业5", 88f)
                )
            ),
            StudyReportCard(
                type = StudyReportCardType.ATTENDANCE,
                title = "出勤记录",
                subtitle = "Attendance Record",
                data = mapOf(
                    "出勤率" to "96%",
                    "迟到次数" to "2次",
                    "请假次数" to "1次"
                ),
                chartData = listOf(
                    ChartData("第1周", 100f),
                    ChartData("第2周", 100f),
                    ChartData("第3周", 90f),
                    ChartData("第4周", 95f)
                )
            ),
            StudyReportCard(
                type = StudyReportCardType.EXAM,
                title = "考试成绩",
                subtitle = "Exam Results",
                data = mapOf(
                    "期中考试" to "88分",
                    "期末考试" to "92分",
                    "平均分" to "90分"
                ),
                chartData = listOf(
                    ChartData("期中", 88f),
                    ChartData("期末", 92f),
                    ChartData("平时", 85f)
                )
            ),
            StudyReportCard(
                type = StudyReportCardType.PARTICIPATION,
                title = "课堂参与",
                subtitle = "Class Participation",
                data = mapOf(
                    "发言次数" to "25次",
                    "提问次数" to "8次",
                    "参与度" to "优秀"
                ),
                chartData = listOf(
                    ChartData("第1周", 6f),
                    ChartData("第2周", 8f),
                    ChartData("第3周", 5f),
                    ChartData("第4周", 6f)
                )
            ),
            StudyReportCard(
                type = StudyReportCardType.SUMMARY,
                title = "学习总结",
                subtitle = "Learning Summary",
                data = mapOf(
                    "掌握程度" to "良好",
                    "薄弱环节" to "算法设计",
                    "建议" to "加强实践练习"
                ),
                chartData = emptyList()
            )
        )
        
        adapter.submitList(studyReportCards)
    }
}