package com.jxdx.mine.teacherhomework.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jxdx.mine.databinding.FragmentReviewedHomeworkBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.vo.UncorrectedHomeworkVO
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.teacherhomework.adapter.CorrectedHomeworkAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewedHomeworkFragment : Fragment() {
    private lateinit var binding: FragmentReviewedHomeworkBinding
    private lateinit var adapter: CorrectedHomeworkAdapter
    private val homeworkList = mutableListOf<UncorrectedHomeworkVO>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("ReviewedHomeworkFragment", "onCreateView 开始")
        try {
            binding = FragmentReviewedHomeworkBinding.inflate(inflater, container, false)
            Log.d("ReviewedHomeworkFragment", "布局初始化成功")
            return binding.root
        } catch (e: Exception) {
            Log.e("ReviewedHomeworkFragment", "onCreateView 失败", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ReviewedHomeworkFragment", "========== onViewCreated 开始 ==========")
        try {
            initViews()
            setupListeners()
            // 自动加载已批改作业
            loadReviewedHomework()
            Log.d("ReviewedHomeworkFragment", "========== onViewCreated 完成 ==========")
        } catch (e: Exception) {
            Log.e("ReviewedHomeworkFragment", "onViewCreated失败", e)
            e.printStackTrace()
        }
    }

    private fun initViews() {
        Log.d("ReviewedHomeworkFragment", "========== initViews 开始 ==========")
        try {
            // 设置标题
            binding.tvTitle.text = "已批改作业"
            binding.tvDescription.text = "这里显示已经批改完成的学生作业"
            
            // 初始化RecyclerView
            binding.recyclerViewHomework.layoutManager = LinearLayoutManager(context)
            
            // 初始化适配器
            adapter = CorrectedHomeworkAdapter(homeworkList) { homework ->
                Log.d("ReviewedHomeworkFragment", "点击已批改作业: ${homework.homeworkName}")
                // 已批改作业点击后可以跳转到详情页面查看批改结果
                Toast.makeText(context, "查看已批改作业: ${homework.homeworkName}", Toast.LENGTH_SHORT).show()
            }
            binding.recyclerViewHomework.adapter = adapter
            
            Log.d("ReviewedHomeworkFragment", "========== initViews 完成 ==========")
        } catch (e: Exception) {
            Log.e("ReviewedHomeworkFragment", "initViews失败", e)
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        Log.d("ReviewedHomeworkFragment", "========== setupListeners 开始 ==========")
        try {
            // 刷新按钮
            binding.btnRefresh.setOnClickListener {
                Log.d("ReviewedHomeworkFragment", "刷新按钮点击")
                Toast.makeText(context, "刷新已批改作业列表", Toast.LENGTH_SHORT).show()
                loadReviewedHomework()
            }
            
            Log.d("ReviewedHomeworkFragment", "========== setupListeners 完成 ==========")
        } catch (e: Exception) {
            Log.e("ReviewedHomeworkFragment", "setupListeners失败", e)
            e.printStackTrace()
        }
    }

    private fun loadReviewedHomework() {
        Log.d("ReviewedHomeworkFragment", "========== loadReviewedHomework 开始 ==========")
        try {
            // 显示加载状态
            showLoading()
            
            // 调用API获取已批改作业列表
            RetrofitClient.apiService.getCorrectedHomeworkList(
                page = 1,
                size = 20
            ).enqueue(object : Callback<BaseResp<List<UncorrectedHomeworkVO>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<UncorrectedHomeworkVO>>>,
                    response: Response<BaseResp<List<UncorrectedHomeworkVO>>>
                ) {
                    hideLoading()
                    Log.d("ReviewedHomeworkFragment", "API响应: ${response.code()}")
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val data = response.body()?.data
                        if (data != null && data.isNotEmpty()) {
                            Log.d("ReviewedHomeworkFragment", "获取到 ${data.size} 个已批改作业")
                            homeworkList.clear()
                            homeworkList.addAll(data)
                            adapter.updateData(homeworkList)
                            updateStatistics()
                            showContent()
                        } else {
                            Log.d("ReviewedHomeworkFragment", "没有已批改作业")
                            updateStatistics()
                            showEmpty()
                        }
                    } else {
                        Log.e("ReviewedHomeworkFragment", "API返回错误: ${response.body()?.message}")
                        showError("获取已批改作业失败: ${response.body()?.message}")
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<List<UncorrectedHomeworkVO>>>,
                    t: Throwable
                ) {
                    hideLoading()
                    Log.e("ReviewedHomeworkFragment", "API调用失败", t)
                    showError("网络请求失败: ${t.message}")
                }
            })
        } catch (e: Exception) {
            hideLoading()
            Log.e("ReviewedHomeworkFragment", "loadReviewedHomework异常", e)
            showError("加载已批改作业时发生异常: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateStatistics() {
        Log.d("ReviewedHomeworkFragment", "========== updateStatistics 开始 ==========")
        try {
            val totalCount = homeworkList.size
            binding.tvStatistics.text = "共${totalCount}个作业已批改"
            
            Log.d("ReviewedHomeworkFragment", "统计更新完成 - 总数: $totalCount")
        } catch (e: Exception) {
            Log.e("ReviewedHomeworkFragment", "updateStatistics失败", e)
            e.printStackTrace()
        }
    }

    private fun showLoading() {
        Log.d("ReviewedHomeworkFragment", "显示加载状态")
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewHomework.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun hideLoading() {
        Log.d("ReviewedHomeworkFragment", "隐藏加载状态")
        binding.progressBar.visibility = View.GONE
    }

    private fun showContent() {
        Log.d("ReviewedHomeworkFragment", "显示内容")
        binding.recyclerViewHomework.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showEmpty() {
        Log.d("ReviewedHomeworkFragment", "显示空状态")
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.recyclerViewHomework.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        Log.d("ReviewedHomeworkFragment", "显示错误状态: $message")
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
        binding.recyclerViewHomework.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        
        // 设置重试按钮点击事件
        binding.btnRetry.setOnClickListener {
            Log.d("ReviewedHomeworkFragment", "重试按钮点击")
            loadReviewedHomework()
        }
    }
}
