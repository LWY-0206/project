package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.R
import com.example.corekit.common.BaseActivity
import com.jxdx.mine.databinding.ActivityCorrectedHomeworkDetailBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.vo.CorrectedHomeworkDetailVO
import com.jxdx.mine.teacherhomework.adapter.CorrectedHomeworkDetailAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CorrectedHomeworkDetailActivity : BaseActivity<ActivityCorrectedHomeworkDetailBinding>() {
    
    private var subjectId: Int = 0
    private var homeworkId: Int = 0
    private var homeworkName: String = ""
    private var subjectName: String = ""
    
    private lateinit var adapter: CorrectedHomeworkDetailAdapter
    private val homeworkDetailList = mutableListOf<CorrectedHomeworkDetailVO>()

    override fun bindLayout(): ActivityCorrectedHomeworkDetailBinding {
        Log.d("CorrectedHomeworkDetailActivity", "========== bindLayout 开始 ==========")
        try {
            return ActivityCorrectedHomeworkDetailBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailActivity", "bindLayout失败", e)
            throw e
        }
    }

    override fun initView() {
        Log.d("CorrectedHomeworkDetailActivity", "========== initView 开始 ==========")
        try {
            // 获取传递的参数
            subjectId = intent.getIntExtra("subjectId", 0)
            homeworkId = intent.getIntExtra("homeworkId", 0)
            homeworkName = intent.getStringExtra("homeworkName") ?: "作业详情"
            subjectName = intent.getStringExtra("subjectName") ?: "科目"
            
            Log.d("CorrectedHomeworkDetailActivity", "接收参数 - subjectId: $subjectId, homeworkId: $homeworkId, homeworkName: $homeworkName, subjectName: $subjectName")
            
            // 初始化RecyclerView
            view.recyclerViewHomework.layoutManager = LinearLayoutManager(this)
            
            // 初始化适配器
            adapter = CorrectedHomeworkDetailAdapter(homeworkDetailList) { homeworkDetail ->
                Log.d("CorrectedHomeworkDetailActivity", "点击已批改作业详情: ${homeworkDetail.studentName}")
                Toast.makeText(this, "查看学生 ${homeworkDetail.studentName} 的批改详情", Toast.LENGTH_SHORT).show()
            }
            view.recyclerViewHomework.adapter = adapter
            
            Log.d("CorrectedHomeworkDetailActivity", "========== initView 完成 ==========")
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailActivity", "initView失败", e)
            e.printStackTrace()
        }
    }

    override fun subscribeUi() {
        Log.d("CorrectedHomeworkDetailActivity", "========== subscribeUi 开始 ==========")
        try {
            // 返回按钮
            view.btnBack.setOnClickListener {
                Log.d("CorrectedHomeworkDetailActivity", "返回按钮点击")
                finish()
            }
            
            // 刷新按钮
            view.btnRefresh.setOnClickListener {
                Log.d("CorrectedHomeworkDetailActivity", "刷新按钮点击")
                loadHomeworkDetail()
            }
            
            // 自动加载数据
            loadHomeworkDetail()
            
            Log.d("CorrectedHomeworkDetailActivity", "========== subscribeUi 完成 ==========")
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailActivity", "subscribeUi失败", e)
            e.printStackTrace()
        }
    }

    private fun loadHomeworkDetail() {
        Log.d("CorrectedHomeworkDetailActivity", "========== loadHomeworkDetail 开始 ==========")
        try {
            if (subjectId == 0 || homeworkId == 0) {
                Log.e("CorrectedHomeworkDetailActivity", "参数错误: subjectId=$subjectId, homeworkId=$homeworkId")
                showError("参数错误，无法加载作业详情")
                return
            }
            
            // 显示加载状态
            showLoading()
            
            // 调用API获取已批改作业详情
            RetrofitClient.apiService.getCorrectedHomeworkDetail(
                subjectId = subjectId,
                homeworkId = homeworkId
            ).enqueue(object : Callback<BaseResp<List<CorrectedHomeworkDetailVO>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<CorrectedHomeworkDetailVO>>>,
                    response: Response<BaseResp<List<CorrectedHomeworkDetailVO>>>
                ) {
                    hideLoading()
                    Log.d("CorrectedHomeworkDetailActivity", "API响应: ${response.code()}")
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val data = response.body()?.data
                        if (data != null && data.isNotEmpty()) {
                            Log.d("CorrectedHomeworkDetailActivity", "获取到 ${data.size} 个已批改作业详情")
                            homeworkDetailList.clear()
                            homeworkDetailList.addAll(data)
                            adapter.updateData(homeworkDetailList)
                            showContent()
                            updateStatistics()
                        } else {
                            Log.d("CorrectedHomeworkDetailActivity", "没有已批改作业详情")
                            showEmpty()
                        }
                    } else {
                        Log.e("CorrectedHomeworkDetailActivity", "API返回错误: ${response.body()?.message}")
                        showError("获取已批改作业详情失败: ${response.body()?.message}")
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<List<CorrectedHomeworkDetailVO>>>,
                    t: Throwable
                ) {
                    hideLoading()
                    Log.e("CorrectedHomeworkDetailActivity", "API调用失败", t)
                    showError("网络请求失败: ${t.message}")
                }
            })
        } catch (e: Exception) {
            hideLoading()
            Log.e("CorrectedHomeworkDetailActivity", "loadHomeworkDetail异常", e)
            showError("加载已批改作业详情时发生异常: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateStatistics() {
        Log.d("CorrectedHomeworkDetailActivity", "========== updateStatistics 开始 ==========")
        try {
            val totalCount = homeworkDetailList.size
            val averageScore = if (totalCount > 0) {
                homeworkDetailList.mapNotNull { it.score }.average()
            } else {
                0.0
            }
            
            view.tvStatistics.text = "共${totalCount}个学生已批改，平均分${String.format("%.1f", averageScore)}分"
            
            Log.d("CorrectedHomeworkDetailActivity", "统计更新完成 - 总数: $totalCount, 平均分: $averageScore")
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailActivity", "updateStatistics失败", e)
            e.printStackTrace()
        }
    }

    private fun showLoading() {
        Log.d("CorrectedHomeworkDetailActivity", "显示加载状态")
        view.progressBar.visibility = View.VISIBLE
        view.recyclerViewHomework.visibility = View.GONE
        view.layoutEmpty.visibility = View.GONE
        view.layoutError.visibility = View.GONE
    }

    private fun hideLoading() {
        Log.d("CorrectedHomeworkDetailActivity", "隐藏加载状态")
        view.progressBar.visibility = View.GONE
    }

    private fun showContent() {
        Log.d("CorrectedHomeworkDetailActivity", "显示内容")
        view.recyclerViewHomework.visibility = View.VISIBLE
        view.layoutEmpty.visibility = View.GONE
        view.layoutError.visibility = View.GONE
    }

    private fun showEmpty() {
        Log.d("CorrectedHomeworkDetailActivity", "显示空状态")
        view.layoutEmpty.visibility = View.VISIBLE
        view.recyclerViewHomework.visibility = View.GONE
        view.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        Log.d("CorrectedHomeworkDetailActivity", "显示错误状态: $message")
        view.layoutError.visibility = View.VISIBLE
        view.tvErrorMessage.text = message
        view.recyclerViewHomework.visibility = View.GONE
        view.layoutEmpty.visibility = View.GONE
        
        // 设置重试按钮点击事件
        view.btnRetry.setOnClickListener {
            Log.d("CorrectedHomeworkDetailActivity", "重试按钮点击")
            loadHomeworkDetail()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CorrectedHomeworkDetailActivity", "onDestroy")
        try {
            // 清理资源
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailActivity", "onDestroy失败", e)
            e.printStackTrace()
        }
    }

    companion object {
        fun start(context: android.content.Context, subjectId: Int, homeworkId: Int, homeworkName: String, subjectName: String) {
            Log.d("CorrectedHomeworkDetailActivity", "启动已批改作业详情页面")
            try {
                val intent = Intent(context, CorrectedHomeworkDetailActivity::class.java)
                intent.putExtra("subjectId", subjectId)
                intent.putExtra("homeworkId", homeworkId)
                intent.putExtra("homeworkName", homeworkName)
                intent.putExtra("subjectName", subjectName)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("CorrectedHomeworkDetailActivity", "启动页面失败", e)
                e.printStackTrace()
            }
        }
    }
}
