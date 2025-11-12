package com.jxdx.resource.Famous

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.Resource
import com.jxdx.resource.databinding.ActivityFavoriteListBinding

class FavoriteListActivity : BaseActivity<ActivityFavoriteListBinding>() {
    private lateinit var viewModel: FamousViewModel
    private lateinit var adapter: FamousListAdapter
    private var currentPage = 1
    private val pageSize = 10
    private var isLoading = false

    override fun bindLayout(): ActivityFavoriteListBinding {
        return ActivityFavoriteListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[FamousViewModel::class.java]

        // 设置标题
        supportActionBar?.title = "我的收藏"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置 RecyclerView
        setupRecyclerView()

        // 加载收藏数据
        loadFavorites()
        view.btnBackFavorite.setOnClickListener {
            finish()
        }
    }

    override fun subscribeUi() {
        viewModel.favoriteLiveData.observe(this) { resource ->
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    isLoading = false
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
                        updateEmptyView(famousData.records.isEmpty())

                        Log.d("FavoriteListActivity", "收藏数据加载成功，共${famousData.records.size}位名人")
                    } ?: run {
                        Log.w("FavoriteListActivity", "收藏数据加载成功但data为null")
                        updateEmptyView(true)
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    isLoading = false
                    view.progressBar.visibility = View.GONE

                    // 检查是否有数据在错误响应中
                    if (data != null && data.records.isNotEmpty()) {
                        // 即使请求状态是错误，但如果有数据，仍然显示
                        if (currentPage == 1) {
                            adapter.setData(data.records)
                        } else {
                            adapter.addData(data.records)
                        }
                        updateEmptyView(data.records.isEmpty())
                    } else {
                        // 没有数据，显示空状态视图
                        if (currentPage == 1) {
                            adapter.setData(emptyList())
                            updateEmptyView(true)
                        } else {
                            // 显示错误视图
                            adapter.showErrorView {
                                loadFavorites()
                            }
                        }
                    }
                }
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            view.emptyView.visibility = View.VISIBLE
            view.emptyText.text = "暂无收藏的名人"
        } else {
            view.emptyView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = FamousListAdapter()
        view.rvFavorite.layoutManager = LinearLayoutManager(this)
        view.rvFavorite.adapter = adapter

        // 设置加载更多监听
        adapter.setOnLoadMoreListener {
            if (!isLoading) {
                currentPage++
                loadFavorites()
            }
        }

        // 设置item点击监听
        adapter.setOnItemClickListener { famous ->
            // 跳转到名人详情页面
            val intent = Intent(this, FamousDetailActivity::class.java)
            intent.putExtra("CELEBRITY_ID", famous.celebrityId)
            startActivity(intent)
        }

        // 设置收藏点击监听 - 在收藏页面点击收藏按钮应该是取消收藏
    }

    private fun loadFavorites() {
        isLoading = true

        // 如果是第一页，显示加载进度条
        if (currentPage == 1) {
            view.progressBar.visibility = View.VISIBLE
            view.emptyView.visibility = View.GONE
        }

        viewModel.getFavorite(currentPage, pageSize)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FavoriteListActivity", "Activity销毁")
    }
}