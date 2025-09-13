package com.example.loding.Schools

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.Resource
import com.example.loding.R
import com.example.loding.databinding.ActivitySchoolListBinding

class SchoolListActivity : BaseActivity<ActivitySchoolListBinding>() {

    private lateinit var viewModel: SchoolListViewModel
    private lateinit var adapter: SchoolListAdapter
    private var currentPage = 1
    private val pageSize = 10
    private var isLoading = false
    private var searchQuery = ""
    private var currentSearchType = "名称" // 记录当前搜索类型

    override fun bindLayout(): ActivitySchoolListBinding {
        return ActivitySchoolListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[SchoolListViewModel::class.java]

        // 设置 RecyclerView
        setupRecyclerView()

        // 设置刷新监听
        setupRefreshListener()

        // 设置搜索功能
        setupSearchFunction()

        // 加载学校数据
        loadSchools()
    }

    override fun subscribeUi() {
        viewModel.schoolLiveData.observe(this) { resource ->
            // 使用 Resource 类提供的 onSuccess 和 onError 方法
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    isLoading = false
                    view.swipeRefresh.isRefreshing = false
                    view.progressBar.visibility = View.GONE

                    data?.let { schoolData ->
                        if (currentPage == 1) {
                            adapter.setData(schoolData.records)
                        } else {
                            adapter.addData(schoolData.records)
                        }

                        // 检查是否还有更多数据
                        val hasMore = currentPage * pageSize < schoolData.total
                        adapter.setHasMore(hasMore)

                        // 更新空状态显示
                        updateEmptyView(schoolData.records.isEmpty(), searchQuery)

                        Log.d("SchoolListActivity", "数据加载成功，共${schoolData.records.size}所学校")
                    } ?: run {
                        Log.w("SchoolListActivity", "数据加载成功但data为null")
                        updateEmptyView(true, searchQuery)
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    isLoading = false
                    view.swipeRefresh.isRefreshing = false
                    view.progressBar.visibility = View.GONE

                    // 检查是否是SQL语法错误（后端空IN条件导致的错误）
                    val isSqlSyntaxError = error?.message?.contains("SQL syntax", ignoreCase = true) == true

                    // 检查是否有数据在错误响应中
                    if (data != null && data.records.isNotEmpty()) {
                        // 即使请求状态是错误，但如果有数据，仍然显示
                        if (currentPage == 1) {
                            adapter.setData(data.records)
                        } else {
                            adapter.addData(data.records)
                        }
                        updateEmptyView(data.records.isEmpty(), searchQuery)
                    } else {
                        // 没有数据，显示空状态视图
                        if (currentPage == 1 || isSqlSyntaxError) {
                            adapter.setData(emptyList())
                            updateEmptyView(true, searchQuery)
                            // 如果是SQL语法错误，可以记录日志，但不显示错误提示
                            if (isSqlSyntaxError) {
                                Log.w("SchoolListActivity", "后端SQL语法错误，视为空结果: ${error?.message}")
                            } else {
                                Toast.makeText(this, "加载失败: ${error?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 显示错误视图
                            adapter.showErrorView {
                                loadSchools()
                            }
                        }
                    }
                }
        }
    }

    private fun updateEmptyView(isEmpty: Boolean, query: String) {
        if (isEmpty) {
            view.emptyView.visibility = View.VISIBLE
            if (query.isNotEmpty()) {
                view.emptyText.text = "没有找到与\"$query\"相关的学校($currentSearchType)"
            } else {
                view.emptyText.text = "暂无学校数据"
            }
        } else {
            view.emptyView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = SchoolListAdapter()
        view.recyclerView.layoutManager = LinearLayoutManager(this)
        view.recyclerView.adapter = adapter

        // 设置加载更多监听
        adapter.setOnLoadMoreListener {
            if (!isLoading) {
                currentPage++
                loadSchools()
            }
        }

        // 设置item点击监听
        adapter.setOnItemClickListener { school ->
            val intent = Intent(this, SchoolDetailActivity::class.java)
            intent.putExtra("SCHOOL_ID", school.schoolId)
            startActivity(intent)
        }

    }

    private fun setupRefreshListener() {
        view.swipeRefresh.setOnRefreshListener {
            // 下拉刷新
            currentPage = 1
            loadSchools()
        }
    }

    private fun setupSearchFunction() {
        // 设置搜索框文本变化监听
        view.etSearch.addTextChangedListener { editable ->
            searchQuery = editable?.toString()?.trim() ?: ""

            // 更新清除按钮可见性
            view.ivClear.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE

            // 更新搜索类型提示
            updateSearchTypeHint()

            // 添加延迟搜索，避免频繁请求
            view.etSearch.removeCallbacks(searchRunnable)
            view.etSearch.postDelayed(searchRunnable, 500) // 500毫秒延迟
        }

        // 设置清除按钮点击监听
        view.ivClear.setOnClickListener {
            view.etSearch.setText("")
            searchQuery = ""
            view.ivClear.visibility = View.GONE
            updateSearchTypeHint()
            performSearch()
        }

        // 设置搜索按钮点击监听
        view.ivSearch.setOnClickListener {
            performSearch()
        }

        // 初始化搜索类型提示
        updateSearchTypeHint()
    }

    // 更新搜索类型提示
    private fun updateSearchTypeHint() {
        val isScoreSearch = isScoreQuery(searchQuery)
        currentSearchType = if (isScoreSearch) "成绩" else "名称"

        // 更新搜索框提示
        view.etSearch.hint = if (isScoreSearch) "请输入成绩" else "请输入学校名称"

        // 更新搜索类型提示文本
        view.tvSearchTypeHint.text = "当前搜索类型: $currentSearchType"
    }

    // 判断查询是否为成绩搜索
    private fun isScoreQuery(query: String): Boolean {
        // 空查询按名称处理
        if (query.isEmpty()) return false

        // 尝试转换为数字
        return try {
            query.toInt()
            // 如果是纯数字，则认为是成绩搜索
            true
        } catch (e: NumberFormatException) {
            // 如果不是纯数字，则按名称搜索
            false
        }
    }

    // 搜索执行Runnable
    private val searchRunnable = Runnable {
        performSearch()
    }

    private fun performSearch() {
        currentPage = 1
        loadSchools()
    }

    private fun loadSchools() {
        isLoading = true

        // 如果是第一页，显示加载进度条
        if (currentPage == 1) {
            view.progressBar.visibility = View.VISIBLE
            view.emptyView.visibility = View.GONE
        }

        // 使用统一的搜索方法，ViewModel会自动判断搜索类型
        viewModel.getSchools(searchQuery, currentPage, pageSize)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除搜索Runnable，避免内存泄漏
        view.etSearch.removeCallbacks(searchRunnable)
        Log.d("SchoolListActivity", "Activity销毁")
    }
}