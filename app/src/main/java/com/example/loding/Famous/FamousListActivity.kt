package com.example.loding.Famous

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.Resource
import com.example.loding.R
import com.example.loding.databinding.ActivityFamousListBinding

class FamousListActivity : BaseActivity<ActivityFamousListBinding>() {

    private lateinit var viewModel: FamousViewModel
    private lateinit var adapter: FamousListAdapter
    private var currentPage = 1
    private val pageSize = 10
    private var isLoading = false
    private var searchQuery = ""
    private var currentProfession = "" // 记录当前搜索的职业

    override fun bindLayout(): ActivityFamousListBinding {
        return ActivityFamousListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[FamousViewModel::class.java]

        // 设置 RecyclerView
        setupRecyclerView()

        // 设置刷新监听
        setupRefreshListener()

        // 设置搜索功能
        setupSearchFunction()

        // 加载名人数据
        loadFamous()
    }

    override fun subscribeUi() {
        viewModel.famousLiveData.observe(this) { resource ->
            // 使用 Resource 类提供的 onSuccess 和 onError 方法
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    isLoading = false
                    view.swipeRefresh.isRefreshing = false
                    view.progressBar.visibility = View.GONE

                    data?.let { famousData ->
                        if (currentPage == 1) {
                            adapter.setData(famousData.records)
                        } else {
                            adapter.addData(famousData.records)
                        }

                        // 检查是否还有更多数据
                        val hasMore = currentPage * pageSize < famousData.total
                        adapter.setHasMore(hasMore)

                        // 更新空状态显示
                        updateEmptyView(famousData.records.isEmpty(), searchQuery)

                        Log.d("FamousListActivity", "数据加载成功，共${famousData.records.size}位名人")
                    } ?: run {
                        Log.w("FamousListActivity", "数据加载成功但data为null")
                        updateEmptyView(true, searchQuery)
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    isLoading = false
                    view.swipeRefresh.isRefreshing = false
                    view.progressBar.visibility = View.GONE

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
                        if (currentPage == 1) {
                            adapter.setData(emptyList())
                            updateEmptyView(true, searchQuery)
                        } else {
                            // 显示错误视图
                            adapter.showErrorView {
                                loadFamous()
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
                view.emptyText.text = "没有找到与\"$query\"相关的名人"
            } else {
                view.emptyText.text = "暂无名人数据"
            }
        } else {
            view.emptyView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = FamousListAdapter()
        view.recyclerView.layoutManager = LinearLayoutManager(this)
        view.recyclerView.adapter = adapter

        // 设置加载更多监听
        adapter.setOnLoadMoreListener {
            if (!isLoading) {
                currentPage++
                loadFamous()
            }
        }

        // 设置item点击监听
        // 设置item点击监听
        adapter.setOnItemClickListener { famous ->
            // 跳转到名人详情页面
            val intent = Intent(this, FamousDetailActivity::class.java)
            intent.putExtra("CELEBRITY_ID", famous.celebrityId)
            startActivity(intent)
        }
    }

    private fun setupRefreshListener() {
        view.swipeRefresh.setOnRefreshListener {
            // 下拉刷新
            currentPage = 1
            loadFamous()
        }
    }

    private fun setupSearchFunction() {
        // 设置搜索框文本变化监听
        view.etSearch.addTextChangedListener { editable ->
            searchQuery = editable?.toString()?.trim() ?: ""

            // 更新清除按钮可见性
            view.ivClear.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE

            // 添加延迟搜索，避免频繁请求
            view.etSearch.removeCallbacks(searchRunnable)
            view.etSearch.postDelayed(searchRunnable, 500) // 500毫秒延迟
        }

        // 设置清除按钮点击监听
        view.ivClear.setOnClickListener {
            view.etSearch.setText("")
            searchQuery = ""
            view.ivClear.visibility = View.GONE
            performSearch()
        }

        // 设置搜索按钮点击监听
        view.ivSearch.setOnClickListener {
            performSearch()
        }
    }

    // 搜索执行Runnable
    private val searchRunnable = Runnable {
        performSearch()
    }

    private fun performSearch() {
        currentPage = 1
        currentProfession = searchQuery // 使用搜索内容作为职业筛选条件
        loadFamous()
    }

    private fun loadFamous() {
        isLoading = true

        // 如果是第一页，显示加载进度条
        if (currentPage == 1) {
            view.progressBar.visibility = View.VISIBLE
            view.emptyView.visibility = View.GONE
        }

        // 使用搜索内容作为职业筛选条件，如果为空则返回全部名人
        viewModel.getFamous(currentProfession, currentPage, pageSize)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除搜索Runnable，避免内存泄漏
        view.etSearch.removeCallbacks(searchRunnable)
        Log.d("FamousListActivity", "Activity销毁")
    }
}