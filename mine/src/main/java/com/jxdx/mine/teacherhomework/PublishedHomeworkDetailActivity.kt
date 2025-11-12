package com.jxdx.mine.teacherhomework

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityPublishedHomeworkDetailBinding
import com.jxdx.mine.http.RetrofitClient
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.http.vo.TeachCreateHWDetailVO
import com.jxdx.mine.teacherhomework.adapter.ImagePreviewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PublishedHomeworkDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishedHomeworkDetailBinding
    private var homeworkId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishedHomeworkDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        homeworkId = intent.getLongExtra("homeworkId", -1L)

        if (homeworkId != -1L) {
            fetchHomeworkDetail(homeworkId)
        } else {
            Toast.makeText(this, "作业ID无效", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchHomeworkDetail(id: Long) {
        // 显示加载状态
        showLoadingState()
        
        RetrofitClient.apiService.getPublishedHomeworkDetail(id).enqueue(object : Callback<BaseResp<TeachCreateHWDetailVO>> {
            override fun onResponse(
                call: Call<BaseResp<TeachCreateHWDetailVO>>,
                response: Response<BaseResp<TeachCreateHWDetailVO>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    val detail = response.body()?.data
                    if (detail != null) {
                        displayHomeworkDetail(detail)
                    } else {
                        showErrorState("获取作业详情失败：数据为空")
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "获取作业详情失败"
                    showErrorState(errorMsg)
                    Log.e("PublishedHomeworkDetail", "Error fetching detail: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BaseResp<TeachCreateHWDetailVO>>, t: Throwable) {
                showErrorState("网络请求失败: ${t.message}")
                Log.e("PublishedHomeworkDetail", "Network error: ${t.message}", t)
            }
        })
    }

    private fun displayHomeworkDetail(detail: TeachCreateHWDetailVO) {
        // 设置作业标题
        binding.tvHomeworkName.text = detail.homeworkName ?: "未命名作业"

        // 处理科目信息（可能为null）
        if (!detail.subject.isNullOrEmpty()) {
            binding.tvSubject.text = detail.subject
            binding.layoutSubject.visibility = View.VISIBLE
        } else {
            binding.layoutSubject.visibility = View.GONE
        }

        // 设置时间信息
        binding.tvSendTime.text = detail.sendTime ?: "未知"
        binding.tvDeadTime.text = detail.deadTime ?: "未知"

        // 设置作业内容
        binding.tvHomeworkContent.text = detail.homeworkContent ?: "无作业内容"

        // 处理图片附件
        if (!detail.imageUrls.isNullOrEmpty()) {
            binding.tvImagesLabel.visibility = View.VISIBLE
            binding.rvImages.visibility = View.VISIBLE
            
            // 设置水平滚动的RecyclerView
            binding.rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvImages.adapter = ImagePreviewAdapter(detail.imageUrls) { imageUrl ->
                // 点击图片的处理逻辑（可以打开大图查看）
                Toast.makeText(this, "点击查看大图: $imageUrl", Toast.LENGTH_SHORT).show()
                // TODO: 实现大图查看功能
            }
        } else {
            binding.tvImagesLabel.visibility = View.GONE
            binding.rvImages.visibility = View.GONE
        }
    }

    private fun showLoadingState() {
        // 可以添加加载动画
        Toast.makeText(this, "正在加载作业详情...", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorState(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // 可以显示错误页面或重试按钮
    }
}
