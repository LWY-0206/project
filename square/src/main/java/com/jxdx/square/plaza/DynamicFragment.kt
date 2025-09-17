package com.jxdx.square.plaza

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.CommonItemDecoration
import com.jxdx.square.adapter.DynamicAdapter
import com.jxdx.square.adapter.DynamicViewModel

import com.jxdx.square.databinding.FragmentDynamicBinding

class DynamicFragment(
    private val callback: () -> Unit,
) : BaseFragment<FragmentDynamicBinding>() {
    val viewModel: DynamicViewModel by lazy {
        ViewModelProvider(this)[DynamicViewModel::class.java]
    }
    lateinit var dynamicAdapter: DynamicAdapter

    // 分页相关变量
    private var currentPage = 1 // 当前页码，从1开始
    private val pageSize = 10 // 每页加载的数量
    private var isLoadingMore = false // 是否正在加载更多数据
    private var hasMoreData = true // 是否还有更多数据

    override fun bindLayout(): FragmentDynamicBinding = FragmentDynamicBinding.inflate(layoutInflater)

    override fun initView() {
        dynamicAdapter = DynamicAdapter()

        // 配置RecyclerView（布局管理器 + 绑定适配器）
        val layoutManager = LinearLayoutManager(requireContext()) // 纵向列表
        find.recyclerViewDynamics.layoutManager = layoutManager
        find.recyclerViewDynamics.adapter = dynamicAdapter // 将适配器绑定到布局中的RecyclerView
        // 添加间距
        find.recyclerViewDynamics.addItemDecoration(CommonItemDecoration(10f))

        // 添加滚动监听器，用于实现下拉加载更多
        setupRecyclerViewScrollListener(layoutManager)

        // 配置SwipeRefreshLayout
        setupSwipeRefreshLayout()

        // 添加发布动态按钮的点击事件
        find.tvPublishDynamic.setOnClickListener {
            // 跳转到发布动态页面
            callback.invoke()
        }

        initData()
    }

    /**
     * 为RecyclerView添加滚动监听器，实现下拉加载更多
     */
    private fun setupRecyclerViewScrollListener(layoutManager: LinearLayoutManager) {
        find.recyclerViewDynamics.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    // 只有向下滚动且还有更多数据且当前没有在加载时，才触发加载更多
                    if (dy > 0 && hasMoreData && !isLoadingMore) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        // 当滚动到倒数第2个item时，开始加载更多数据
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 2) {
                            loadMoreData()
                        }
                    }
                }
            },
        )
    }

    /**
     * 设置SwipeRefreshLayout的属性和刷新监听器
     */
    private fun setupSwipeRefreshLayout() {
        // 设置刷新时的颜色
        find.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_light,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light,
        )

        // 设置刷新监听器
        find.swipeRefreshLayout.setOnRefreshListener {
            // 执行刷新操作
            refreshData()
        }
    }

    /**
     * 刷新数据
     */
    private fun refreshData() {
        // 重置分页状态
        currentPage = 1
        hasMoreData = true

        // 重新加载数据
        viewModel.getDynamics(null, pageSize)
    }

    /**
     * 加载更多数据
     */
    private fun loadMoreData() {
        if (isLoadingMore || !hasMoreData) return

        isLoadingMore = true

        // 加载下一页数据
        viewModel.getDynamics(currentPage, pageSize)
    }

    fun initData() {
        viewModel.getDynamics(null, pageSize)
    }

    override fun subscribeUi() {
        viewModel.dynamicLiveData.observe(this) { resource ->
            // 无论成功与否，只要有响应就停止刷新动画和加载状态
            if (find.swipeRefreshLayout.isRefreshing) {
                find.swipeRefreshLayout.isRefreshing = false
            }
            isLoadingMore = false

            resource.onSuccess { dynamicList ->
                if (dynamicList != null) {
                    if (currentPage == 1) {
                        // 第一页数据，清空旧数据，添加新数据
                        dynamicAdapter.clearAndAdd(dynamicList)
                    } else {
                        // 加载更多数据，直接添加到现有列表
                        dynamicAdapter.add(dynamicList)
                    }

                    // 判断是否还有更多数据
                    hasMoreData = dynamicList.size == pageSize

                    // 更新页码
                    if (hasMoreData) {
                        currentPage++
                    }
                }
            }
        }
    }
}
