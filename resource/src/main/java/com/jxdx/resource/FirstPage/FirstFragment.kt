package com.jxdx.resource.FirstPage
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.corekit.common.BaseFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.jxdx.resource.News.NewsAdapter
import com.jxdx.resource.News.NewsItem
import com.jxdx.resource.R
import com.jxdx.resource.Recommendation.WaterfallAdapter
import com.jxdx.resource.StudySuggestions.StudySuggestionActivity
import com.jxdx.resource.databinding.FragmentFirstBinding
import com.jxdx.resource.databinding.ItemScheduleBinding
import com.jxdx.resource.databinding.LayoutNewsSectionBinding
import com.jxdx.resource.resource.GlideImageLoader
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class FirstFragment : BaseFragment<FragmentFirstBinding>() {
    private lateinit var fab: FloatingActionButton
    private var dX = 0f
    private var dY = 0f
    private var isDragging = false
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var newsAdapter: NewsAdapter
    private var topBanner: com.youth.banner.Banner? = null
    private lateinit var waterfallRecyclerView: RecyclerView
    private lateinit var waterfallAdapter: WaterfallAdapter
    private val recommendationList = mutableListOf<RecommendationItem>()
    override fun bindLayout(): FragmentFirstBinding {
        return FragmentFirstBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化Banner
        topBanner = find.topBanner
        val imageUrls: MutableList<String?> = ArrayList<String?>()
        imageUrls.add(
            "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/d7e459cf-e03c-4cac-b6e1-0aa5783f1466.png"
        )

        topBanner?.setImages(imageUrls)
            ?.setImageLoader(GlideImageLoader())
            ?.isAutoPlay(true)
            ?.setDelayTime(3000)
            ?.start()
        // 初始化课表数据
        initSchedule()
        // 设置瀑布流RecyclerView
        setupWaterfallRecyclerView()
        // 添加数据
        val newsBinding = find.newsSection

        // 然后使用newsBinding来设置新闻部分的视图
        // 例如，设置查看更多点击事件
        newsBinding.tvViewMore.setOnClickListener {
            openNewsList()
        }

        // 设置新闻RecyclerView
        setupNewsRecyclerView(newsBinding)

        // 加载新闻数据
        loadNewsData(newsBinding)
        setUpFloatingActionButton()
    }
    override fun subscribeUi() {
        // 可以在这里添加数据观察或UI更新逻辑
    }


private fun setUpFloatingActionButton() {
    fab= find.fabDraggable
    fab.setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录触摸点相对于 FAB 左上角的偏移
                dX = view.x - event.rawX
                dY = view.y - event.rawY
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                // 计算新的位置
                var newX = event.rawX + dX
                var newY = event.rawY + dY

                // 限制在屏幕范围内
                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels

                // 确保不超出屏幕边界
                newX = newX.coerceIn(0f, (screenWidth - view.width).toFloat())
                newY = newY.coerceIn(0f, (screenHeight - view.height).toFloat())

                // 更新位置
                view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()

                isDragging = true
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    // 手指抬起时的逻辑 - 吸附到边缘
                    snapToEdge(view)
                } else {
                    // 点击事件 - 执行 FAB 的原有功能
                    performFabClick()
                }
            }
        }
        true
    }

    // 原有的点击监听器（如果需要）
    fab.setOnClickListener {
        // 只有没有拖动时才执行点击
        if (!isDragging) {
            performFabClick()
        }
    }
}
    private fun snapToEdge(view: View) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val centerX = screenWidth / 2

        val newX = if (view.x + view.width / 2 < centerX) {
            0f // 吸附到左边
        } else {
            (screenWidth - view.width).toFloat() // 吸附到右边
        }

        // 确保 Y 坐标在屏幕范围内
        var newY = view.y
        newY = newY.coerceIn(0f, (screenHeight - view.height).toFloat())

        view.animate()
            .x(newX)
            .y(newY)
            .setDuration(200)
            .start()
    }

    private fun performFabClick() {
        val context = getContext()
        if (context != null) {
            // 创建 Intent，参数1：当前上下文；参数2：目标 Activity 类
            val intent: Intent = Intent(context, StudySuggestionActivity::class.java)
            // 启动目标 Activity
            startActivity(intent)
        }
    }
    private fun setupWaterfallRecyclerView() {
        waterfallRecyclerView = find.waterfallRecyclerView
        updataRecommendationUI(recommendationList)
        Log.d("更新数据推荐数据列表", "recommendationList: $recommendationList")

        // 设置瀑布流布局管理器，2列
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        waterfallRecyclerView.layoutManager = layoutManager

        // 创建适配器
        waterfallAdapter = WaterfallAdapter(recommendationList)
        waterfallRecyclerView.adapter = waterfallAdapter

        // 设置点击事件
        waterfallAdapter.onItemClickListener = { item ->
            handleItemClick(item)
        }

        // 设置收藏点击事件
        waterfallAdapter.onFavoriteClickListener = { item ->
            handleFavoriteClick(item)
        }

        // 添加滚动监听实现加载更多
        waterfallRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    // 到达底部，加载更多数据
                    loadMoreData()
                }
            }
        })
    }

    private fun handleItemClick(item: RecommendationItem) {
        // 根据类型处理点击事件
        if (item.isVideo()) {
            showMessage("播放视频: ${item.title}")
            // 实际开发中这里可以跳转到视频播放页面
            // val intent = Intent(requireContext(), VideoPlayActivity::class.java)
            // intent.putExtra("video_id", item.id)
            // startActivity(intent)
        } else {
            showMessage("查看文章: ${item.title}")
            // 实际开发中这里可以跳转到文章详情页面
            // val intent = Intent(requireContext(), ArticleDetailActivity::class.java)
            // intent.putExtra("article_id", item.id)
            // startActivity(intent)
        }
    }

    private fun handleFavoriteClick(item: RecommendationItem) {
        // 切换收藏状态
        item.isFavorited = !item.isFavorited

        // 更新UI
        val position = recommendationList.indexOf(item)
        if (position != -1) {
            waterfallAdapter.notifyItemChanged(position)
        }

        // 显示提示信息
        if (item.isFavorited) {
            showMessage("已收藏: ${item.title}")
        } else {
            showMessage("取消收藏: ${item.title}")
        }

        // 这里可以添加实际的收藏/取消收藏API调用
        // callFavoriteApi(item.id, item.isFavorited)
    }

    private fun loadMoreData() {
        // 模拟加载更多数据
        val newItems = listOf(
            RecommendationItem(
                id = "7",
                title = "新添加的内容1",
                description = "这是加载更多的测试内容",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/58765522-815b-4191-aeb5-9b8a552ba891.png",
                type = "video",
                duration = "10:15",
                viewCount = 500,
                likeCount = 23
            ),
            RecommendationItem(
                id = "8",
                title = "新添加的内容2",
                description = "这是另一个加载更多的测试内容",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/58765522-815b-4191-aeb5-9b8a552ba891.png",
                type = "article",
                viewCount = 300,
                likeCount = 15
            )
        )

        waterfallAdapter.addData(newItems)
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
    private fun initSchedule() {
        // 设置刷新按钮点击事件
        find.ivRefresh.setOnClickListener {
            refreshSchedule()
        }

        // 设置课表点击事件
        find.scheduleContainer.setOnClickListener {
            // 跳转到课表详情页面
            showMessage("查看完整课表")
        }

        // 加载课表数据
        loadScheduleData()
    }

    private fun loadScheduleData() {
        // 模拟课表数据
        val scheduleItems = listOf(
            ScheduleItem(
                id = "1",
                week = "第2周",
                weekday = "周二",
                currentTime = "08:10",
                courseName = "线性代数B◆",
                courseTime = "15:15-17:25",
                location = "梁林校区 兴礼楼 (14号楼) 512",
                courseType = "门课",
                isCurrent = true
            )
            // 可以添加更多课程...
        )

        updateScheduleUI(scheduleItems)
    }

    private fun updateScheduleUI(scheduleItems: List<ScheduleItem>) {
        if (scheduleItems.isEmpty()) {
            // 显示空状态
            find.tvEmptySchedule.visibility = View.VISIBLE
            find.scheduleContainer.visibility = View.GONE
        } else {
            // 显示课表
            find.tvEmptySchedule.visibility = View.GONE
            find.scheduleContainer.visibility = View.VISIBLE

            // 更新课表数据（这里简化处理，实际可以使用RecyclerView）
            val scheduleItem = scheduleItems.first() // 取第一个课程
            val Sch_binding= ItemScheduleBinding.inflate(layoutInflater)
            // 更新UI
            Sch_binding.tvWeek.text = scheduleItem.week
            Sch_binding.tvWeekday.text = scheduleItem.weekday
            Sch_binding.tvCurrentTime.text = scheduleItem.currentTime
            Sch_binding.tvCourseName.text = scheduleItem.courseName
            Sch_binding.tvCourseTime.text = scheduleItem.courseTime
            Sch_binding.tvLocation.text = scheduleItem.location
            Sch_binding.tvCourseType.text = scheduleItem.courseType

            // 如果是当前课程，可以添加特殊样式
            if (scheduleItem.isCurrent) {
                Sch_binding.tvCurrentTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            }
        }
    }
    private fun setupNewsRecyclerView(newsBinding: LayoutNewsSectionBinding) {
        // 创建适配器
        newsAdapter = NewsAdapter(newsList)

        // 设置适配器
        newsBinding.rvNews.adapter = newsAdapter
        // 设置布局管理器 - 使用LinearLayoutManager垂直排列
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        newsBinding.rvNews.layoutManager = layoutManager
        Log.d("设置新闻适配器","success")

//        // 禁用嵌套滚动（因为已经在NestedScrollView中）
//        newsBinding.rvNews.isNestedScrollingEnabled = false
        //允许rvNews单独滚动
        newsBinding.rvNews.isNestedScrollingEnabled = true

        // 设置点击事件
        newsAdapter.onItemClickListener = { newsItem ->
            openNewsDetail(newsItem)
        }
    }

    private fun loadNewsData(newsBinding: LayoutNewsSectionBinding) {
        // 显示加载状态
        newsBinding.pbNewsLoading.visibility = View.VISIBLE
        newsBinding.rvNews.visibility = View.GONE
        newsBinding.tvEmptyNews.visibility = View.GONE
        // 模拟新闻数据（实际开发中应该从API获取）
        Log.d("创建模拟数据","success")
        val mockNews = listOf(
            NewsItem(
                id = "1",
                title = "学校举办春季运动会，各学院积极备战",
                summary = "为丰富校园文化生活，学校将于下月举办春季运动会，各学院已经开始积极备战...",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/d9a280aa-15c5-42b1-a0d9-c5a962bcd8b2.jpg",
                source = "校园新闻",
                publishTime = "2小时",
                viewCount = 1250
            ),
            NewsItem(
                id = "2",
                title = "计算机学院学生在编程大赛中荣获一等奖",
                summary = "在刚刚结束的全国大学生程序设计大赛中，我校计算机学院代表队表现出色...",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/451278d3-c571-406d-8a57-276a71efe710.jpg",
                source = "学术动态",
                publishTime = "5小时",
                viewCount = 890
            ),
            NewsItem(
                id = "3",
                title = "图书馆新增电子资源，助力学术研究",
                summary = "为满足师生学术研究需求，图书馆近期引进了多个知名数据库和电子期刊...",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/c9338d44-4517-45dd-bc86-763f6c1eac03.jpg",
                source = "资源更新",
                publishTime = "1天",
                viewCount = 567
            ),
            NewsItem(
                id = "4",
                title = "学校开展心理健康教育周活动",
                summary = "为关注学生心理健康，学校将于本周举办系列心理健康教育活动...",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/4588f1e9-9b03-4033-a6c6-b73355a51b3d.jpg",
                source = "学生工作",
                publishTime = "3小时",
                viewCount = 432
            )
        )
        updateNewsUI(mockNews,newsBinding)
        Log.d("设置模拟数据","${mockNews}")
        // 模拟网络延迟
//        Handler(Looper.getMainLooper()).postDelayed({
//            updateNewsUI(mockNews)
//        }, 1000)
    }

    private fun updateNewsUI(newsItems: List<NewsItem>,newsBinding: LayoutNewsSectionBinding) {
        newsBinding.pbNewsLoading.visibility = View.GONE

        if (newsItems.isEmpty()) {
            newsBinding.tvEmptyNews.visibility = View.VISIBLE
            newsBinding.rvNews.visibility = View.GONE
            return
        }

        newsBinding.tvEmptyNews.visibility = View.GONE
        newsBinding.rvNews.visibility = View.VISIBLE

        // 更新适配器数据
        newsAdapter.updateData(newsItems)
        Log.d("更新适配器数据","success, ${newsItems}")
        newsAdapter.notifyDataSetChanged()
    }

    // 刷新新闻

    // 加载更多新闻（如果需要分页）
    private fun loadMoreNews() {
        val moreNews = listOf(
            NewsItem(
                id = "5",
                title = "新增的新闻条目",
                summary = "这是加载更多的新闻内容...",
                coverUrl = "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/news/more.jpg",
                source = "最新动态",
                publishTime = "刚刚",
                viewCount = 100
            )
        )

        newsAdapter.addData(moreNews)
    }

    private fun openNewsList() {
        // 跳转到新闻列表页面
        showMessage("查看所有新闻")
        // val intent = Intent(requireContext(), NewsListActivity::class.java)
        // startActivity(intent)
    }

    private fun openNewsDetail(newsItem: NewsItem) {
        // 跳转到新闻详情页面
        showMessage("打开新闻: ${newsItem.title}")
        // val intent = Intent(requireContext(), NewsDetailActivity::class.java)
        // intent.putExtra("news_id", newsItem.id)
        // startActivity(intent)
    }
    private fun refreshSchedule() {
        showMessage("刷新课表")
        // 这里可以调用API刷新课表数据
        loadScheduleData()
    }
    // 清理资源
    override fun onDestroyView() {
        super.onDestroyView()
        waterfallRecyclerView.clearOnScrollListeners()
        topBanner?.stopAutoPlay()
    }
    private fun updataRecommendationUI(recommendationItems: List<RecommendationItem>) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://121.41.176.238/api/resource/list")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val recommendationResponse = Gson().fromJson(responseData, RecommendationResponse::class.java)
                val recommendationItems = recommendationResponse.data.RecommendationList
                Log.d("RecommendationItems", recommendationItems.toString())
                // 更新适配器数据
                waterfallAdapter.updateData(recommendationItems)
            }
        })
    }
}