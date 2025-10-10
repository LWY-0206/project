package com.jxdx.mine.teacherhomework.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.databinding.FragmentUnreviewedHomeworkBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.vo.UncorrectedHomeworkVO
import com.jxdx.mine.teacherhomework.adapter.UncorrectedHomeworkAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UnreviewedHomeworkFragment : Fragment() {
    private lateinit var binding: FragmentUnreviewedHomeworkBinding
    private lateinit var adapter: UncorrectedHomeworkAdapter
    private val homeworkList = mutableListOf<UncorrectedHomeworkVO>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("UnreviewedHomeworkFragment", "onCreateView 开始")
        try {
            binding = FragmentUnreviewedHomeworkBinding.inflate(inflater, container, false)
            Log.d("UnreviewedHomeworkFragment", "布局初始化成功")
            return binding.root
        } catch (e: Exception) {
            Log.e("UnreviewedHomeworkFragment", "onCreateView 失败", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("UnreviewedHomeworkFragment", "========== onViewCreated 开始 ==========")
        try {
            initViews()
            setupListeners()
            loadUnreviewedHomework()
            Log.d("UnreviewedHomeworkFragment", "========== onViewCreated 完成 ==========")
        } catch (e: Exception) {
            Log.e("UnreviewedHomeworkFragment", "onViewCreated失败", e)
            e.printStackTrace()
        }
    }

    private fun initViews() {
        Log.d("UnreviewedHomeworkFragment", "========== initViews 开始 ==========")
        try {
            // 设置标题
            binding.tvTitle.text = "未批改作业"
            binding.tvDescription.text = "这里显示需要批改的学生作业"
            
            // 初始化RecyclerView
            binding.recyclerViewHomework.layoutManager = LinearLayoutManager(context)
            
            // 初始化适配器
            adapter = UncorrectedHomeworkAdapter(homeworkList) { homework ->
                Log.d("UnreviewedHomeworkFragment", "作业点击: ${homework.homeworkName}")
                // 跳转到作业批改详情页面
                if (homework.subjectId != null && homework.homeworkId != null) {
                    com.jxdx.mine.teacherhomework.UncorrectedHomeworkDetailActivity.start(
                        requireContext(),
                        homework.subjectId,
                        homework.homeworkId.toInt()
                    )
                } else {
                    Toast.makeText(context, "作业信息不完整", Toast.LENGTH_SHORT).show()
                }
            }
            binding.recyclerViewHomework.adapter = adapter
            
            Log.d("UnreviewedHomeworkFragment", "========== initViews 完成 ==========")
        } catch (e: Exception) {
            Log.e("UnreviewedHomeworkFragment", "initViews失败", e)
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        Log.d("UnreviewedHomeworkFragment", "========== setupListeners 开始 ==========")
        try {
            // 刷新按钮
            binding.btnRefresh.setOnClickListener {
                Log.d("UnreviewedHomeworkFragment", "刷新按钮点击")
                loadUnreviewedHomework()
            }
            
            Log.d("UnreviewedHomeworkFragment", "========== setupListeners 完成 ==========")
        } catch (e: Exception) {
            Log.e("UnreviewedHomeworkFragment", "setupListeners失败", e)
            e.printStackTrace()
        }
    }

    private fun loadUnreviewedHomework() {
        Log.d("UnreviewedHomeworkFragment", "========== loadUnreviewedHomework 开始 ==========")
        try {
            // 显示加载状态
            showLoading()
            
            // 调用API获取未批改作业列表
            RetrofitClient.apiService.getUncorrectedHomeworkList(
                page = 1,
                size = 20
            ).enqueue(object : Callback<BaseResp<List<UncorrectedHomeworkVO>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<UncorrectedHomeworkVO>>>,
                    response: Response<BaseResp<List<UncorrectedHomeworkVO>>>
                ) {
                    hideLoading()
                    Log.d("UnreviewedHomeworkFragment", "API响应: ${response.code()}")
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val data = response.body()?.data
                        if (data != null && data.isNotEmpty()) {
                            Log.d("UnreviewedHomeworkFragment", "获取到 ${data.size} 个未批改作业")
                            homeworkList.clear()
                            homeworkList.addAll(data)
                            adapter.updateData(homeworkList)
                            showContent()
                        } else {
                            Log.d("UnreviewedHomeworkFragment", "没有未批改作业")
                            showEmpty()
                        }
                    } else {
                        Log.e("UnreviewedHomeworkFragment", "API返回错误: ${response.body()?.message}")
                        showError("获取未批改作业失败: ${response.body()?.message}")
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<List<UncorrectedHomeworkVO>>>,
                    t: Throwable
                ) {
                    hideLoading()
                    Log.e("UnreviewedHomeworkFragment", "网络请求失败", t)
                    showError("网络请求失败: ${t.message}")
                }
            })
            
        } catch (e: Exception) {
            hideLoading()
            Log.e("UnreviewedHomeworkFragment", "loadUnreviewedHomework失败", e)
            e.printStackTrace()
            showError("加载失败: ${e.message}")
        }
    }

    private fun showLoading() {
        Log.d("UnreviewedHomeworkFragment", "显示加载状态")
        binding.recyclerViewHomework.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        // TODO: 可以添加加载动画
    }

    private fun hideLoading() {
        Log.d("UnreviewedHomeworkFragment", "隐藏加载状态")
    }

    private fun showContent() {
        Log.d("UnreviewedHomeworkFragment", "显示内容")
        binding.recyclerViewHomework.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun showEmpty() {
        Log.d("UnreviewedHomeworkFragment", "显示空状态")
        binding.recyclerViewHomework.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Log.e("UnreviewedHomeworkFragment", "显示错误: $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        showEmpty()
    }
}
