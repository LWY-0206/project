package com.jxdx.resource.StudySuggestions

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.databinding.ActivityStudySuggestionBinding

class StudySuggestionActivity : BaseActivity<ActivityStudySuggestionBinding>() {

    private lateinit var viewModel: StudyViewModel
    private lateinit var adapter: SubjectAnalysisAdapter
    private var isLoading = false
    private var searchQuery = ""

    override fun bindLayout(): ActivityStudySuggestionBinding {
        return ActivityStudySuggestionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[StudyViewModel::class.java]

        // 设置 RecyclerView
        setupRecyclerView()

        // 设置刷新监听
        setupRefreshListener()
        // 设置点击监听
        setupClickListeners()

        // 加载学习建议数据
        loadStudySuggestions()
    }

    override fun subscribeUi() {
        viewModel.studyLiveData.observe(this) { resource ->
            // 使用 Resource 类提供的 onSuccess 和 onError 方法
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    isLoading = false
                    view.progressBar.visibility = View.GONE
                    view.swipeRefresh.isRefreshing = false

                    data?.let { studyData ->
                        displayStudyData(studyData)
                        Log.d("StudySuggestionActivity", "学习建议数据加载成功，共${studyData.subjectAnalyses.size}个学科")
                    } ?: run {
                        Log.w("StudySuggestionActivity", "数据加载成功但data为null")
                        updateEmptyView(true, searchQuery)
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    isLoading = false
                    view.progressBar.visibility = View.GONE
                    view.swipeRefresh.isRefreshing = false

                    // 检查是否有数据在错误响应中
                    if (data != null && data.subjectAnalyses.isNotEmpty()) {
                        // 即使请求状态是错误，但如果有数据，仍然显示
                        displayStudyData(data)
                    } else {
                        // 没有数据，显示空状态视图
                        updateEmptyView(true, searchQuery)
                        // 可以显示错误提示
                        showErrorToast(error?.message ?: "加载失败")
                    }
                }
        }
    }

    private fun displayStudyData(studyData: StudyData) {
        // 显示总体建议
        view.tvOverallSuggestion.text = studyData.overallSuggestion

        // 显示学科分析
        adapter.setData(studyData.subjectAnalyses)

        // 更新空状态显示
        updateEmptyView(studyData.subjectAnalyses.isEmpty(), searchQuery)

        // 显示总体建议卡片
        view.cardOverall.visibility = View.VISIBLE
    }

    private fun updateEmptyView(isEmpty: Boolean, query: String) {
        if (isEmpty) {
            view.emptyView.visibility = View.VISIBLE
            if (query.isNotEmpty()) {
                view.emptyText.text = "没有找到与\"$query\"相关的学科"
            } else {
                view.emptyText.text = "暂无学习建议数据"
            }
            view.recyclerView.visibility = View.GONE
            view.cardOverall.visibility = View.GONE
        } else {
            view.emptyView.visibility = View.GONE
            view.recyclerView.visibility = View.VISIBLE
            view.cardOverall.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectAnalysisAdapter()
        view.recyclerView.layoutManager = LinearLayoutManager(this)
        view.recyclerView.adapter = adapter
    }

    private fun setupRefreshListener() {
        view.swipeRefresh.setOnRefreshListener {
            // 下拉刷新
            loadStudySuggestions()
        }
    }


    private fun setupClickListeners() {
        // 返回按钮
        view.ivBack.setOnClickListener {
            onBackPressed()
        }
    }
    private fun loadStudySuggestions() {
        if (isLoading) return

        isLoading = true
        view.progressBar.visibility = View.VISIBLE
        view.emptyView.visibility = View.GONE

        viewModel.getStudyPlan()
    }


    private fun showErrorToast(message: String) {
        // 使用您项目中的Toast工具类
        // ToastUtils.showShort(message)
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

}