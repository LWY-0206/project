package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.*
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityUncorrectedHomeworkDetailBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.vo.UncorrectedHomeworkDetailVO
import com.jxdx.mine.teacherhomework.adapter.UncorrectedHomeworkDetailAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UncorrectedHomeworkDetailActivity : BaseActivity<ActivityUncorrectedHomeworkDetailBinding>() {
    
    private lateinit var adapter: UncorrectedHomeworkDetailAdapter
    private val homeworkDetailList = mutableListOf<UncorrectedHomeworkDetailVO>()
    
    private var subjectId: Int = 0
    private var homeworkId: Int = 0
    
    private val timeHandler = Handler(Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            timeHandler.postDelayed(this, 1000) // 每秒更新一次
        }
    }
    
    override fun bindLayout(): ActivityUncorrectedHomeworkDetailBinding {
        Log.d("UncorrectedHomeworkDetailActivity", "bindLayout 开始")
        try {
            val binding = ActivityUncorrectedHomeworkDetailBinding.inflate(layoutInflater)
            Log.d("UncorrectedHomeworkDetailActivity", "bindLayout 成功")
            return binding
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailActivity", "bindLayout 失败", e)
            throw e
        }
    }
    
    override fun initView() {
        Log.d("UncorrectedHomeworkDetailActivity", "========== initView 开始 ==========")
        try {
            // 获取传递的参数
            subjectId = intent.getIntExtra("subjectId", 0)
            homeworkId = intent.getIntExtra("homeworkId", 0)
            
            Log.d("UncorrectedHomeworkDetailActivity", "接收参数 - subjectId: $subjectId, homeworkId: $homeworkId")
            
            // 设置标题
            supportActionBar?.title = "作业批改详情"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            Log.d("UncorrectedHomeworkDetailActivity", "ActionBar设置完成")
            
            // 初始化RecyclerView
            initRecyclerView()
            
            // 自动加载数据
            loadHomeworkDetail()
            
            // 开始时间更新
            startTimeUpdate()
            
            Log.d("UncorrectedHomeworkDetailActivity", "========== initView 完成 ==========")
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailActivity", "initView失败", e)
            e.printStackTrace()
            finish()
        }
    }
    
    override fun subscribeUi() {
        Log.d("UncorrectedHomeworkDetailActivity", "========== subscribeUi 开始 ==========")
        try {
            // 设置返回按钮点击事件
            view.btnBack.setOnClickListener {
                Log.d("UncorrectedHomeworkDetailActivity", "返回按钮点击")
                finish()
            }
            
            // 设置刷新按钮点击事件
            view.btnRefresh.setOnClickListener {
                Log.d("UncorrectedHomeworkDetailActivity", "刷新按钮点击")
                loadHomeworkDetail()
            }
            
            Log.d("UncorrectedHomeworkDetailActivity", "========== subscribeUi 完成 ==========")
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailActivity", "subscribeUi失败", e)
            e.printStackTrace()
        }
    }
    
    private fun initRecyclerView() {
        Log.d("UncorrectedHomeworkDetailActivity", "========== initRecyclerView 开始 ==========")
        try {
            // 设置RecyclerView布局管理器
            view.recyclerViewHomework.layoutManager = LinearLayoutManager(this)
            
            // 初始化适配器
            adapter = UncorrectedHomeworkDetailAdapter(
                homeworkDetailList,
                onItemClick = { homeworkDetail ->
                    Log.d("UncorrectedHomeworkDetailActivity", "学生作业点击: ${homeworkDetail.studentName}")
                    // 显示学生作业的详细信息
                    Toast.makeText(this, "点击了学生: ${homeworkDetail.studentName}", Toast.LENGTH_SHORT).show()
                },
                onReviewClick = { homeworkDetail ->
                    Log.d("UncorrectedHomeworkDetailActivity", "批改学生作业: ${homeworkDetail.studentName}")
                    // 跳转到批改页面
                    ReviewHomeworkActivity.start(this, homeworkId.toLong(), homeworkDetail.studentId ?: 0L, subjectId, homeworkDetail)
                }
            )
            view.recyclerViewHomework.adapter = adapter
            
            Log.d("UncorrectedHomeworkDetailActivity", "========== initRecyclerView 完成 ==========")
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailActivity", "initRecyclerView失败", e)
            e.printStackTrace()
        }
    }
    
    private fun loadHomeworkDetail() {
        Log.d("UncorrectedHomeworkDetailActivity", "========== loadHomeworkDetail 开始 ==========")
        try {
            if (subjectId == 0 || homeworkId == 0) {
                Log.e("UncorrectedHomeworkDetailActivity", "参数错误: subjectId=$subjectId, homeworkId=$homeworkId")
                Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 显示加载状态
            showLoading()
            
            // 调用API获取作业详情
            RetrofitClient.apiService.getUncorrectedHomeworkDetail(
                subjectId = subjectId,
                homeworkId = homeworkId
            ).enqueue(object : Callback<BaseResp<List<UncorrectedHomeworkDetailVO>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<UncorrectedHomeworkDetailVO>>>,
                    response: Response<BaseResp<List<UncorrectedHomeworkDetailVO>>>
                ) {
                    hideLoading()
                    Log.d("UncorrectedHomeworkDetailActivity", "API响应: ${response.code()}")
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val data = response.body()?.data
                        if (data != null && data.isNotEmpty()) {
                            Log.d("UncorrectedHomeworkDetailActivity", "获取到 ${data.size} 个学生作业")
                            homeworkDetailList.clear()
                            homeworkDetailList.addAll(data)
                            adapter.updateData(homeworkDetailList)
                            updateStatistics(data.size)
                            showContent()
                        } else {
                            Log.d("UncorrectedHomeworkDetailActivity", "没有学生作业数据")
                            updateStatistics(0)
                            showEmpty()
                        }
                    } else {
                        Log.e("UncorrectedHomeworkDetailActivity", "API返回错误: ${response.body()?.message}")
                        showError("获取作业详情失败: ${response.body()?.message}")
                    }
                }
                
                override fun onFailure(
                    call: Call<BaseResp<List<UncorrectedHomeworkDetailVO>>>,
                    t: Throwable
                ) {
                    hideLoading()
                    Log.e("UncorrectedHomeworkDetailActivity", "网络请求失败", t)
                    showError("网络请求失败: ${t.message}")
                }
            })
            
        } catch (e: Exception) {
            hideLoading()
            Log.e("UncorrectedHomeworkDetailActivity", "loadHomeworkDetail失败", e)
            e.printStackTrace()
            showError("加载失败: ${e.message}")
        }
    }
    
    private fun showLoading() {
        Log.d("UncorrectedHomeworkDetailActivity", "显示加载状态")
        view.recyclerViewHomework.visibility = android.view.View.GONE
        view.layoutEmpty.visibility = android.view.View.GONE
        // TODO: 可以添加加载动画
    }
    
    private fun hideLoading() {
        Log.d("UncorrectedHomeworkDetailActivity", "隐藏加载状态")
    }
    
    private fun showContent() {
        Log.d("UncorrectedHomeworkDetailActivity", "显示内容")
        view.recyclerViewHomework.visibility = android.view.View.VISIBLE
        view.layoutEmpty.visibility = android.view.View.GONE
    }
    
    private fun showEmpty() {
        Log.d("UncorrectedHomeworkDetailActivity", "显示空状态")
        view.recyclerViewHomework.visibility = android.view.View.GONE
        view.layoutEmpty.visibility = android.view.View.VISIBLE
    }
    
    private fun showError(message: String) {
        Log.e("UncorrectedHomeworkDetailActivity", "显示错误: $message")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        updateStatistics(0)
        showEmpty()
    }
    
    private fun updateStatistics(count: Int) {
        Log.d("UncorrectedHomeworkDetailActivity", "更新统计信息: $count 个学生")
        view.tvStatistics.text = "共 $count 个学生"
    }
    
    private fun startTimeUpdate() {
        Log.d("UncorrectedHomeworkDetailActivity", "开始时间更新")
        updateTime() // 立即更新一次
        timeHandler.postDelayed(timeRunnable, 1000) // 1秒后开始定时更新
    }
    
    private fun updateTime() {
        try {
            val currentTime = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = timeFormat.format(currentTime.time)
            view.tvTime.text = timeString
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailActivity", "更新时间失败", e)
        }
    }
    
    private fun stopTimeUpdate() {
        Log.d("UncorrectedHomeworkDetailActivity", "停止时间更新")
        timeHandler.removeCallbacks(timeRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimeUpdate()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        Log.d("UncorrectedHomeworkDetailActivity", "ActionBar返回按钮点击")
        finish()
        return true
    }
    
    companion object {
        fun start(context: android.content.Context, subjectId: Int, homeworkId: Int) {
            val intent = Intent(context, UncorrectedHomeworkDetailActivity::class.java)
            intent.putExtra("subjectId", subjectId)
            intent.putExtra("homeworkId", homeworkId)
            context.startActivity(intent)
        }
    }
}
