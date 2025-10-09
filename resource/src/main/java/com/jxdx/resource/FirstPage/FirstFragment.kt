package com.jxdx.resource.FirstPage

import Schedule.FirstPage.ScheduleAdapter
import Schedule.FirstPage.ScheduleItem
import com.jxdx.resource.Schedule.ScheduleViewModel
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.corekit.common.BaseFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.jxdx.resource.News.NewsAdapter
import com.jxdx.resource.News.NewsItem
import com.jxdx.resource.News.NewVIewModel
import com.jxdx.resource.Recommendation.RecommendationItem
import com.jxdx.resource.Recommendation.RecommendationResponse
import com.jxdx.resource.Recommendation.RecommendationViewModel
import com.jxdx.resource.Recommendation.WaterfallAdapter
import com.jxdx.resource.RescourseDetail.PdfViewerActivity
import com.jxdx.resource.StudySuggestions.StudySuggestionActivity
import com.jxdx.resource.databinding.FragmentFirstBinding
import com.jxdx.resource.databinding.LayoutNewsSectionBinding
import com.jxdx.resource.resource.GlideImageLoader
import com.youth.banner.Banner
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jxxy.debug.h5.activity.WebViewActivity
import java.io.File
import java.io.IOException
import java.util.Calendar

class FirstFragment : BaseFragment<FragmentFirstBinding>() {
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var newsViewModel: NewVIewModel
    private lateinit var fab: FloatingActionButton
    private var dX = 0f
    private var dY = 0f
    private var isDragging = false
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var newsAdapter: NewsAdapter
    private var topBanner: Banner? = null
    private lateinit var waterfallRecyclerView: RecyclerView
    private lateinit var waterfallAdapter: WaterfallAdapter
    private val recommendationList = mutableListOf<RecommendationItem>()
    private lateinit var recommendationViewModel: RecommendationViewModel
    // 新增课表相关的变量
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val scheduleList = mutableListOf<ScheduleItem>()

    // 当前选择的周次和星期
    private var currentWeek = "3" // 默认为第三周
    private var currentWeekday = "2" // 默认为星期二


    override fun bindLayout(): FragmentFirstBinding {
        return FragmentFirstBinding.inflate(layoutInflater)
    }

    override fun initView() {
        scheduleViewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]
        newsViewModel = ViewModelProvider(this)[NewVIewModel::class.java]
        recommendationViewModel = ViewModelProvider(this)[RecommendationViewModel::class.java]

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

        // 初始化课表数据 - 现在使用RecyclerView
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
        recommendationViewModel.recommendationLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    recommendationList.clear()
                    recommendationList.addAll(data)
                    waterfallAdapter.updateData(recommendationList)
                    Log.d("RecommendationViewModel", "Data loaded successfully, size: ${data}")
                } else {
                }
            }
        }
        recommendationViewModel.recommendationDetailLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    WebViewActivity.actionStart(requireActivity(), data.fileUrl, data.title)
                }
            }
        }
        scheduleViewModel.scheduleLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    scheduleList.clear()
                    scheduleList.addAll(data)
                    scheduleAdapter.updateData(scheduleList)
                    updateScheduleUI(scheduleList)
                    Log.d("ScheduleViewModel", "Data loaded successfully, size: ${data.size}")
                } else {
                    updateScheduleUI(emptyList())
                    Log.d("ScheduleViewModel", "Data is null")
                }
            }
            result.onError { error, _ ->
                Log.d("ScheduleViewModel", "Error loading data: $error")
                updateScheduleUI(emptyList())
            }
        }

        // 观察新闻数据
        newsViewModel.newsLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    updateNewsUI(data, find.newsSection)
                    Log.d("NewsViewModel", "News data loaded successfully, size: ${data.size}")
                } else {
                    updateNewsUI(emptyList(), find.newsSection)
                    Log.d("NewsViewModel", "News data is null")
                }
            }
            result.onError { error, _ ->
                Log.d("NewsViewModel", "Error loading news data: $error")
                updateNewsUI(emptyList(), find.newsSection)
                showMessage("加载新闻失败")
            }
        }
    }

    private fun initSchedule() {
        // 设置刷新按钮点击事件
        find.ivRefresh.setOnClickListener {
            refreshSchedule()
        }

        // 设置课表RecyclerView
        setupScheduleRecyclerView()

        // 加载课表数据
        loadScheduleData()
    }

    private fun setupScheduleRecyclerView() {
        scheduleRecyclerView = find.scheduleRecyclerView
        scheduleAdapter = ScheduleAdapter(scheduleList)

        // 使用线性布局管理器
        val layoutManager = LinearLayoutManager(requireContext())
        scheduleRecyclerView.layoutManager = layoutManager
        scheduleRecyclerView.adapter = scheduleAdapter

        // 设置点击事件
        scheduleAdapter.onItemClickListener = { scheduleItem ->
            // 跳转到课表详情页面
            showMessage("查看课程: ${scheduleItem.courseName}")
            // val intent = Intent(requireContext(), ScheduleDetailActivity::class.java)
            // intent.putExtra("schedule_item", scheduleItem)
            // startActivity(intent)
        }
    }

    private fun loadScheduleData() {
        // 方法1: 使用固定值（第三周星期二）
        loadScheduleWithFixedValue()

        // 方法2: 也可以使用系统时间（注释掉上面那行，取消下面这行的注释）
        // loadScheduleWithSystemTime()
    }

    /**
     * 使用固定值加载课表数据（第三周星期二）
     */
    private fun loadScheduleWithFixedValue() {
        currentWeek = "第三周"
        currentWeekday = "星期三"

        Log.d("com/jxdx/resource/Schedule", "使用固定值加载课表: 第${currentWeek}周 星期${currentWeekday}")
        scheduleViewModel.getScheduleList(currentWeek, currentWeekday)
    }

    /**
     * 使用系统时间加载课表数据
     */
    private fun loadScheduleWithSystemTime() {
        val (week, weekday) = getCurrentWeekAndWeekday()
        currentWeek = week
        currentWeekday = weekday

        Log.d("com/jxdx/resource/Schedule", "使用系统时间加载课表: 第${currentWeek}周 星期${getChineseWeekday(currentWeekday)}")
        scheduleViewModel.getScheduleList(currentWeek, currentWeekday)
    }

    /**
     * 获取当前周次和星期几
     * @return Pair<周次, 星期几> 星期几: 1=星期一, 2=星期二, ..., 7=星期日
     */
    private fun getCurrentWeekAndWeekday(): Pair<String, String> {
        val calendar = Calendar.getInstance()

        // 计算当前是第几周（这里需要根据学期开始日期计算，暂时使用简单逻辑）
        // 实际项目中应该根据学期开始日期计算准确的周次
        val currentWeek = calculateCurrentWeek(calendar)

        // 获取星期几 (Calendar中: 1=星期日, 2=星期一, ..., 7=星期六)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // 转换为我们的格式: 1=星期一, 2=星期二, ..., 7=星期日
        val weekday = when (dayOfWeek) {
            Calendar.MONDAY -> "1"
            Calendar.TUESDAY -> "2"
            Calendar.WEDNESDAY -> "3"
            Calendar.THURSDAY -> "4"
            Calendar.FRIDAY -> "5"
            Calendar.SATURDAY -> "6"
            Calendar.SUNDAY -> "7"
            else -> "1"
        }

        return Pair(currentWeek.toString(), weekday)
    }

    /**
     * 计算当前周次
     * 这里需要根据学期开始日期计算，暂时使用简单逻辑
     * 实际项目中应该从服务器获取学期开始日期或使用固定值
     */
    private fun calculateCurrentWeek(calendar: Calendar): Int {
        // 假设学期从2024年2月26日开始（第三周）
        val semesterStart = Calendar.getInstance().apply {
            set(2024, Calendar.FEBRUARY, 26) // 月份从0开始，所以2月是Calendar.FEBRUARY
        }

        val diffInMillis = calendar.timeInMillis - semesterStart.timeInMillis
        val diffInWeeks = (diffInMillis / (1000 * 60 * 60 * 24 * 7)).toInt()

        // 学期开始是第三周
        return 3 + diffInWeeks
    }

    /**
     * 将数字星期转换为中文
     */
    private fun getChineseWeekday(weekday: String): String {
        return when (weekday) {
            "1" -> "一"
            "2" -> "二"
            "3" -> "三"
            "4" -> "四"
            "5" -> "五"
            "6" -> "六"
            "7" -> "日"
            else -> "未知"
        }
    }

    /**
     * 手动选择周次和星期
     */
    private fun loadScheduleWithCustomSelection(week: String, weekday: String) {
        currentWeek = week
        currentWeekday = weekday
        Log.d("com/jxdx/resource/Schedule", "手动选择加载课表: 第${currentWeek}周 星期${getChineseWeekday(currentWeekday)}")
        scheduleViewModel.getScheduleList(currentWeek, currentWeekday)
    }

    private fun updateScheduleUI(scheduleItems: List<ScheduleItem>) {
        if (scheduleItems.isEmpty()) {
            // 显示空状态
            find.tvEmptySchedule.visibility = View.VISIBLE
            scheduleRecyclerView.visibility = View.GONE
        } else {
            // 显示课表
            find.tvEmptySchedule.visibility = View.GONE
            scheduleRecyclerView.visibility = View.VISIBLE

            // 更新适配器数据
            scheduleAdapter.updateData(scheduleItems)
        }
    }

    private fun refreshSchedule() {
        showMessage("刷新课表")
        // 重新加载当前选择的周次和星期的数据
        scheduleViewModel.getScheduleList(currentWeek, currentWeekday)
    }

    // 以下方法保持不变...
    private fun setUpFloatingActionButton() {
        fab = find.fabDraggable
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

        // 设置瀑布流布局管理器，2列
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        waterfallRecyclerView.layoutManager = layoutManager

        // 创建适配器
        waterfallAdapter = WaterfallAdapter(recommendationList)
        waterfallRecyclerView.adapter = waterfallAdapter

        // 设置点击事件
        waterfallAdapter.onItemClickListener = { item ->
          handleItemClick( item)
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

        // 加载推荐数据
        loadRecommendationData()
    }
    private fun loadRecommendationData() {
        Log.d("Recommendation", "开始加载推荐数据")
        recommendationViewModel.getRecommendationList(2)
    }
    private fun handleItemClick(item: RecommendationItem) {
        // 根据类型处理点击事件
        if (item.isVideo()) {
            recommendationViewModel.getRecommendationDetail(item.id.toInt())
        } else {
            recommendationViewModel.getRecommendationDetail(item.id.toInt())
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupNewsRecyclerView(newsBinding: LayoutNewsSectionBinding) {
        // 创建适配器
        newsAdapter = NewsAdapter(newsList)

        // 设置适配器
        newsBinding.rvNews.adapter = newsAdapter
        // 设置布局管理器 - 使用LinearLayoutManager垂直排列
        val layoutManager = LinearLayoutManager(requireContext())
        newsBinding.rvNews.layoutManager = layoutManager
        Log.d("News", "设置新闻适配器成功")

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

        Log.d("News", "开始从API加载新闻数据")

        // 从API加载新闻数据
        newsViewModel.getNewsList()
    }

    private fun updateNewsUI(newsItems: List<NewsItem>, newsBinding: LayoutNewsSectionBinding) {
        newsBinding.pbNewsLoading.visibility = View.GONE

        if (newsItems.isEmpty()) {
            newsBinding.tvEmptyNews.visibility = View.VISIBLE
            newsBinding.rvNews.visibility = View.GONE
            Log.d("News", "新闻数据为空")
            return
        }

        newsBinding.tvEmptyNews.visibility = View.GONE
        newsBinding.rvNews.visibility = View.VISIBLE

        // 更新适配器数据
        newsAdapter.updateData(newsItems)
        Log.d("News", "更新新闻适配器数据，数量: ${newsItems.size}")

        // 显示第一条新闻的标题用于调试
        if (newsItems.isNotEmpty()) {
            Log.d("News", "第一条新闻标题: ${newsItems[0].title}")
            Log.d("News", "第一条新闻来源: ${newsItems[0].source}")
            Log.d("News", "第一条新闻图片: ${newsItems[0].coverUrl}")
        }
    }

    // 加载更多新闻（如果需要分页）
    private fun loadMoreNews() {
        // 如果需要分页加载更多新闻，可以在这里实现
        // 目前API没有分页参数，暂时不实现
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
                val recommendationResponse =
                    Gson().fromJson(responseData, RecommendationResponse::class.java)
                val recommendationItems = recommendationResponse.data.RecommendationList
                Log.d("RecommendationItems", recommendationItems.toString())
                // 更新适配器数据
                waterfallAdapter.updateData(recommendationItems)
            }
        })
    }
}