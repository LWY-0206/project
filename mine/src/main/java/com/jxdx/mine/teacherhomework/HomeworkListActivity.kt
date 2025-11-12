package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.PageData
import com.jxdx.mine.R
import com.jxdx.mine.StudentSubmission
import com.jxdx.mine.databinding.ActivityHomeworkListBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.request.CreateHomeworkRequest
import com.jxdx.mine.http.vo.TeachCreateHWSimpleVO
import com.jxdx.mine.teacherhomework.adapter.HomeworkAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeworkListActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkListBinding
    private lateinit var homeworkAdapter: HomeworkAdapter
    private val homeworkList = mutableListOf<HomeworkDetail>() // 已发布作业列表
    private val homeworkLibraryList = mutableListOf<HomeworkDetail>() // 作业库列表
    private val publishedHomeworkList = mutableListOf<HomeworkDetail>() // 已发布作业缓存
    private var courseId: String? = null
    private var courseName: String? = null
    private var currentPage = 1
    private val pageSize = 10
    private var isLoading = false
    private var hasMoreData = true
    private var isDeleteMode = false // 是否处于删除模式
    private val selectedHomeworkIds = mutableSetOf<String>() // 选中的作业ID
    private var currentTab = 0 // 0: 已发放, 1: 作业库
    private var isCreateMenuExpanded = false // 创建菜单是否展开
    
    companion object {
        private const val REQUEST_CREATE_HOMEWORK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取从CourseListActivity传递过来的课程信息
        courseId = intent.getStringExtra("courseId")
        courseName = intent.getStringExtra("courseName")
        
        initViews()
        setupListeners()
        // 初始化时加载作业库数据（所有作业）
        loadHomeworkLibrary()
    }

    private fun initViews() {
        initRecyclerView()
        initCreateHomeworkButton()
        // 初始化默认选中"已发放"标签
        switchTab(0)
    }

    private fun setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 分段控制器
        binding.tabIssued.setOnClickListener {
            switchTab(0)
        }
        
        binding.tabLibrary.setOnClickListener {
            switchTab(1)
        }

        // 删除按钮
        binding.btnDelete.setOnClickListener {
            if (isDeleteMode) {
                confirmDelete()
            } else {
                enterDeleteMode()
            }
        }

        // 批改作业按钮
        binding.fabReviewHomework.setOnClickListener {
            goToReviewHomework()
        }
    }

    private fun switchTab(tabIndex: Int) {
        currentTab = tabIndex
        
        // 切换标签时退出删除模式和隐藏创建菜单
        if (isDeleteMode) {
            exitDeleteMode()
        }
        if (isCreateMenuExpanded) {
            hideCreateMenu()
        }
        
        // 重置分页状态
        currentPage = 1
        hasMoreData = true
        
        if (tabIndex == 0) {
            // 已发放 - 显示已发布的作业
            binding.tabIssued.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabIssued.setTextColor(getColor(android.R.color.white))
            binding.tabLibrary.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabLibrary.setTextColor(getColor(R.color.primary_color))
            binding.fabCreateHomework.visibility = android.view.View.GONE
            binding.fabReviewHomework.visibility = android.view.View.VISIBLE
            
            // 显示已发放的作业（已发布的作业）
            showPublishedHomework()
        } else {
            // 作业库 - 显示您之前创建的所有作业
            binding.tabLibrary.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabLibrary.setTextColor(getColor(android.R.color.white))
            binding.tabIssued.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabIssued.setTextColor(getColor(R.color.primary_color))
            binding.fabCreateHomework.visibility = android.view.View.VISIBLE
            binding.fabReviewHomework.visibility = android.view.View.GONE
            // 显示作业库的作业（您之前创建的所有作业）
            homeworkAdapter.updateData(homeworkLibraryList)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.jxdx.mine.R.menu.menu_homework_list, menu)
        
        // 根据删除模式动态显示菜单项
        menu?.findItem(com.jxdx.mine.R.id.action_delete_homework)?.isVisible = !isDeleteMode
        menu?.findItem(com.jxdx.mine.R.id.action_confirm_delete)?.isVisible = isDeleteMode
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.jxdx.mine.R.id.action_delete_homework -> {
                // 进入删除模式
                enterDeleteMode()
                true
            }
            com.jxdx.mine.R.id.action_confirm_delete -> {
                // 确认删除
                confirmDelete()
                true
            }
            com.jxdx.mine.R.id.action_homework_library -> {
                // 跳转到作业库页面
                val intent = Intent(this, HomeworkLibraryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun enterDeleteMode() {
        isDeleteMode = true
        selectedHomeworkIds.clear()
        // 更新菜单显示
        invalidateOptionsMenu()
        // 通知适配器进入删除模式
        homeworkAdapter.setDeleteMode(true)
        Toast.makeText(this, "请选择要删除的作业", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete() {
        if (selectedHomeworkIds.isNotEmpty()) {
            // 显示确认对话框
            AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除选中的 ${selectedHomeworkIds.size} 个作业吗？删除后无法恢复。")
                .setPositiveButton("删除") { _, _ ->
                    performDelete()
                }
                .setNegativeButton("取消") { _, _ ->
                    exitDeleteMode()
                }
                .show()
        } else {
            Toast.makeText(this, "请先选择要删除的作业", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performDelete() {
        if (selectedHomeworkIds.isNotEmpty()) {
            // 显示加载状态
            Toast.makeText(this, "正在删除作业...", Toast.LENGTH_SHORT).show()
            
            // 将选中的作业ID用逗号连接
            val homeworkIdString = selectedHomeworkIds.joinToString(",")
            
            // 调用删除API
            RetrofitClient.apiService.deleteHomework(homeworkIdString).enqueue(object : Callback<BaseResp<String>> {
                override fun onResponse(
                    call: Call<BaseResp<String>>,
                    response: Response<BaseResp<String>>
                ) {
                    if (response.isSuccessful && response.body()?.code == 0) {
                        Toast.makeText(this@HomeworkListActivity, "成功删除 ${selectedHomeworkIds.size} 个作业", Toast.LENGTH_SHORT).show()
                        // 重新加载数据
                        loadHomework(true)
                    } else {
                        val errorMsg = response.body()?.message ?: "删除失败"
                        Toast.makeText(this@HomeworkListActivity, "删除失败: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                    exitDeleteMode()
                }

                override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                    Toast.makeText(this@HomeworkListActivity, "网络异常，删除失败", Toast.LENGTH_SHORT).show()
                    exitDeleteMode()
                }
            })
        } else {
            Toast.makeText(this, "请先选择要删除的作业", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exitDeleteMode() {
        isDeleteMode = false
        selectedHomeworkIds.clear()
        // 更新菜单显示
        invalidateOptionsMenu()
        // 通知适配器退出删除模式
        homeworkAdapter.setDeleteMode(false)
    }

    fun onHomeworkSelected(homeworkId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedHomeworkIds.add(homeworkId)
        } else {
            selectedHomeworkIds.remove(homeworkId)
        }
        // 更新确认删除按钮的可用状态
        invalidateOptionsMenu()
    }

    private fun goToReviewHomework() {
        // 直接跳转到批改作业页面
        val intent = Intent(this, TeacherReviewActivity::class.java)
        startActivity(intent)
    }
    
    private fun initCreateHomeworkButton() {
        binding.fabCreateHomework.setOnClickListener {
            if (currentTab == 1) { // 只在作业库模式下工作
                toggleCreateMenu()
            }
        }
        
        // AI创建选项点击
        binding.optionAiCreate.setOnClickListener {
            hideCreateMenu()
            // 跳转到AI创建作业界面
            val intent = Intent(this, AiCreateHomeworkActivity::class.java)
            intent.putExtra("subjectId", courseId?.toIntOrNull() ?: 1)
            startActivityForResult(intent, REQUEST_CREATE_HOMEWORK)
        }
        
        // 手动创建选项点击
        binding.optionManualCreate.setOnClickListener {
            hideCreateMenu()
            // 跳转到手动创建作业界面
            val intent = Intent(this, CreateHomeworkActivity::class.java)
            intent.putExtra("courseId", courseId)
            intent.putExtra("courseName", courseName)
            startActivityForResult(intent, REQUEST_CREATE_HOMEWORK)
        }
    }

    private fun toggleCreateMenu() {
        if (isCreateMenuExpanded) {
            hideCreateMenu()
        } else {
            showCreateMenu()
        }
    }

    private fun showCreateMenu() {
        isCreateMenuExpanded = true
        binding.createMenuContainer.visibility = android.view.View.VISIBLE
        binding.createMenuContainer.alpha = 0f
        binding.createMenuContainer.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    private fun hideCreateMenu() {
        isCreateMenuExpanded = false
        binding.createMenuContainer.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.createMenuContainer.visibility = android.view.View.GONE
            }
            .start()
    }

    private fun initRecyclerView() {
        homeworkAdapter = HomeworkAdapter(homeworkList) { homework ->
            // 根据当前标签页跳转到不同的详情页面
            if (currentTab == 0) {
                // 已发布标签页 - 跳转到已发布作业详情
                val intent = Intent(this, PublishedHomeworkDetailActivity::class.java)
                intent.putExtra("homeworkId", homework.id.toLongOrNull() ?: -1L)
                startActivity(intent)
            } else {
                // 作业库标签页 - 跳转到普通作业详情
                val intent = Intent(this, HomeworkDetailActivity::class.java)
                intent.putExtra("homeworkId", homework.id)
                intent.putExtra("homeworkTitle", homework.title)
                startActivity(intent)
            }
        }
        
        // 设置选择变化监听器
        homeworkAdapter.setOnSelectionChangeListener { homeworkId, isSelected ->
            onHomeworkSelected(homeworkId, isSelected)
        }
        
        binding.recyclerViewHomework.apply {
            layoutManager = LinearLayoutManager(this@HomeworkListActivity)
            adapter = homeworkAdapter
            
            // 添加滚动监听，实现自动加载更多
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    
                    // 当滚动到倒数第3个item时，开始加载下一页
                    if (!isLoading && hasMoreData && lastVisibleItem >= totalItemCount - 3) {
                        loadMoreData()
                    }
                }
            })
        }
        
        // 初始化作业库数据
        initHomeworkLibrary()
    }
    
    private fun loadMoreData() {
        if (isLoading || !hasMoreData) return
        
        // 显示加载更多提示
        Toast.makeText(this, "正在加载更多...", Toast.LENGTH_SHORT).show()
        
        // 根据当前标签页加载不同的数据
        if (currentTab == 0) {
            // 已发布标签页
            loadPublishedHomework()
        } else {
            // 作业库标签页
            loadHomework(false)
        }
    }

    private fun loadHomeworkLibrary() {
        // 加载作业库数据（所有作业）
        loadHomework(true)
    }
    
    private fun initHomeworkLibrary() {
        // 作业库显示您之前创建的所有作业（包括已发布和未发布的）
        // 这里应该从API获取您创建的所有作业
        // 暂时使用现有的homeworkList作为作业库数据
        homeworkLibraryList.clear()
        homeworkLibraryList.addAll(homeworkList)
        
        // 可以添加更多您之前创建的作业
        // 这里可以根据实际需求从数据库或API获取
    }
    
    private fun showPublishedHomework() {
        // 如果有缓存数据，直接显示；否则加载数据
        if (publishedHomeworkList.isNotEmpty()) {
            homeworkAdapter.updateData(publishedHomeworkList)
        } else {
            loadPublishedHomework()
        }
    }
    
    private fun loadPublishedHomework() {
        if (isLoading) return
        
        isLoading = true
        
        // 调用已发布作业接口
        RetrofitClient.apiService.getPublishedHomeworkList(
            page = currentPage,
            size = pageSize
        ).enqueue(object : Callback<BaseResp<PageData<TeachCreateHWSimpleVO>>> {
            override fun onResponse(
                call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>,
                response: Response<BaseResp<PageData<TeachCreateHWSimpleVO>>>
            ) {
                isLoading = false
                
                if (response.isSuccessful && response.body()?.code == 0) {
                    val data = response.body()?.data
                    data?.records?.let { records ->
                        if (records.isEmpty()) {
                            // 没有已发布的作业，显示空状态
                            showEmptyPublishedState()
                        } else {
                            // 有已发布的作业，显示列表
                            val newPublishedHomework = records.map { hw ->
                                HomeworkDetail(
                                    id = hw.homeworkId?.toString() ?: "",
                                    title = hw.homeworkName ?: "未命名作业",
                                    description = "",
                                    dueDate = hw.deadTime ?: "",
                                    submissions = mutableListOf(),
                                    isPublished = true, // 这个接口返回的都是已发布的
                                    publishTime = hw.publishTime,
                                    createTime = hw.createTime
                                )
                            }
                            
                            // 保存到缓存中
                            if (currentPage == 1) {
                                publishedHomeworkList.clear()
                            }
                            publishedHomeworkList.addAll(newPublishedHomework)
                            
                            homeworkAdapter.updateData(publishedHomeworkList)
                            
                            // 显示统计信息
                            Log.d("HomeworkList", "已发布作业数量: ${publishedHomeworkList.size}")
                        }
                        
                        // 检查是否还有更多数据
                        hasMoreData = currentPage < (data?.pages ?: 0)
                        currentPage++
                    }
                } else {
                    // API调用失败，显示空状态
                    showEmptyPublishedState()
                    val errorMsg = response.body()?.message ?: "获取已发布作业失败"
                    Toast.makeText(this@HomeworkListActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>,
                t: Throwable
            ) {
                isLoading = false
                // 网络失败，显示空状态
                showEmptyPublishedState()
                Toast.makeText(this@HomeworkListActivity, "网络异常，获取已发布作业失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showEmptyPublishedState() {
        // 显示暂无已发布作业的状态
        publishedHomeworkList.clear()
        homeworkAdapter.updateData(emptyList())
        Toast.makeText(this, "暂无已发布的作业", Toast.LENGTH_SHORT).show()
    }
    
    private fun publishHomework(homeworkId: String) {
        // 调用发布作业API
        RetrofitClient.apiService.publishHomework(homeworkId).enqueue(object : Callback<BaseResp<String>> {
            override fun onResponse(
                call: Call<BaseResp<String>>,
                response: Response<BaseResp<String>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@HomeworkListActivity, "作业发布成功", Toast.LENGTH_SHORT).show()
                    // 重新加载数据
                    loadHomework(true)
                } else {
                    val errorMsg = response.body()?.message ?: "发布失败"
                    Toast.makeText(this@HomeworkListActivity, "发布失败: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                Toast.makeText(this@HomeworkListActivity, "网络异常，发布失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadHomework(isRefresh: Boolean = false) {
        if (isLoading) return
        
        if (isRefresh) {
            currentPage = 1
            hasMoreData = true
            homeworkLibraryList.clear()
        }
        
        if (!hasMoreData) return
        
        isLoading = true
        
        // 调用API获取作业库列表 (@GET /api/teach/homework/create/list)
        RetrofitClient.apiService.getTeacherHomeworkList(
            page = currentPage,
            size = pageSize
        ).enqueue(object : Callback<BaseResp<PageData<TeachCreateHWSimpleVO>>> {
            override fun onResponse(call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>, response: Response<BaseResp<PageData<TeachCreateHWSimpleVO>>>) {
                isLoading = false
                
                if (response.isSuccessful && response.body()?.code == 0) {
                    val data = response.body()?.data
                    data?.records?.let { records ->
                        // 将API返回的数据转换为HomeworkDetail对象
                        val newHomeworkList = records.map { hw ->
                            HomeworkDetail(
                                id = hw.homeworkId?.toString() ?: "",
                                title = hw.homeworkName ?: "未命名作业",
                                description = "", // 详情API获取
                                dueDate = hw.deadTime ?: "",
                                submissions = mutableListOf(),
                                isPublished = hw.isPublished ?: false,
                                publishTime = hw.publishTime,
                                createTime = hw.createTime
                            )
                        }
                        
                        // 添加到作业库列表
                        homeworkLibraryList.addAll(newHomeworkList)
                        
                        // 检查是否还有更多数据
                        hasMoreData = currentPage < (data?.pages ?: 0)
                        currentPage++
                        
                        // 只在作业库标签页时更新显示
                        if (currentTab == 1) {
                            homeworkAdapter.updateData(homeworkLibraryList)
                        }
                    }
                } else {
                    hasMoreData = false
                    // API调用失败，显示模拟数据
                    if (currentPage == 1) {
                        showMockData()
                    }
                    // 显示错误信息
                    Toast.makeText(this@HomeworkListActivity, "获取作业库失败: ${response.body()?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>, t: Throwable) {
                isLoading = false
                hasMoreData = false
                Log.e("HomeworkList", "Failed to load homework: ${t.message}")
                // 网络失败，显示模拟数据
                if (currentPage == 1) {
                    showMockData()
                }
                // 显示错误信息
                Toast.makeText(this@HomeworkListActivity, "网络异常，获取作业库失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showMockData() {
        // 模拟数据 - 根据课程ID显示不同的大学作业
        homeworkList.clear()
        homeworkLibraryList.clear()
        
        when (courseId) {
            "1" -> {
                // 高等数学（微积分）的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h1",
                        title = "函数极限与连续性作业",
                        description = "完成教材第25页练习题1-10题，要求写出详细的解题步骤和计算过程，重点关注极限的ε-δ定义证明。",
                        dueDate = "2025-10-18",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：根据函数极限的定义，对于任意ε>0，存在δ=ε/2，当0<|x-2|<δ时...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/calculus_answer1.jpg", 92, "解题步骤清晰，极限证明方法正确，计算准确", true),
                            StudentSubmission("s3", "李娜", "答案内容：我采用了夹逼定理来求解第5题的极限，过程如下...", null, null, false),
                            StudentSubmission("s4", "王芳", "https://example.com/calculus_answer2.jpg", 85, "整体表现良好，但第7题的连续性证明可以更严谨", true)
                        ),
                        isPublished = true,
                        publishTime = "2025-10-15 09:00:00",
                        createTime = "2025-10-15 08:30:00"
                    )
                )
                homeworkList.add(
                    HomeworkDetail(
                        id = "h2",
                        title = "导数与微分作业",
                        description = "完成教材第48页练习题1-12题，包括导数的定义计算、基本初等函数求导法则、隐函数求导和高阶导数计算。",
                        dueDate = "2025-10-25",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/derivative_answer1.jpg", null, null, false),
                            StudentSubmission("s2", "张伟", "", null, null, false),
                            StudentSubmission("s3", "李娜", "https://example.com/derivative_answer2.jpg", 88, "隐函数求导掌握较好，但高阶导数计算有小错误", true)
                        ),
                        isPublished = true,
                        publishTime = "2025-10-20 10:00:00",
                        createTime = "2025-10-20 09:30:00"
                    )
                )
                
                // 添加一些未发布的作业到作业库
                homeworkLibraryList.add(
                    HomeworkDetail(
                        id = "h2_draft",
                        title = "积分计算作业（草稿）",
                        description = "完成教材第60页练习题1-8题，包括不定积分和定积分的计算。",
                        dueDate = "2025-11-01",
                        submissions = mutableListOf(),
                        isPublished = false,
                        publishTime = null,
                        createTime = "2025-10-22 14:30:00"
                    )
                )
            }
            "2" -> {
                // 线性代数的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h3",
                        title = "矩阵运算与行列式计算作业",
                        description = "完成教材第36页练习题1-8题，包括矩阵的加减乘运算、转置、行列式计算和伴随矩阵求解。",
                        dueDate = "2025-10-20",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：矩阵乘法需要注意行列对应，第1题的计算结果为...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/matrix_answer1.jpg", 90, "行列式计算正确，矩阵运算熟练", true)
                        )
                    )
                )
            }
            "3" -> {
                // 大学物理（力学）的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h4",
                        title = "牛顿运动定律应用作业",
                        description = "完成教材第42页练习题1-6题，分析物体受力情况，应用牛顿三大定律解决力学问题，要求画出受力分析图。",
                        dueDate = "2025-10-22",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/physics_answer1.jpg", null, null, false),
                            StudentSubmission("s2", "张伟", "答案内容：根据牛顿第二定律F=ma，对于斜面问题，物体受到重力、支持力和摩擦力...", 95, "受力分析图清晰，解题过程完整正确", true)
                        )
                    )
                )
            }
            "4" -> {
                // 程序设计基础（Java）的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h5",
                        title = "Java类与对象编程作业",
                        description = "设计一个学生类(Student)，包含姓名、学号、成绩等属性，以及构造方法、getter/setter方法和显示信息的方法。编写主程序测试该类的功能。",
                        dueDate = "2025-10-28",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/java_code1.zip", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/java_code2.zip", 94, "类设计合理，代码规范，功能完整", true),
                            StudentSubmission("s3", "李娜", "https://example.com/java_code3.zip", 87, "实现了基本功能，但可以增加异常处理机制", true)
                        )
                    )
                )
            }
            "5" -> {
                // 宏观经济学的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h6",
                        title = "国民收入决定模型作业",
                        description = "结合IS-LM模型分析财政政策和货币政策对国民收入的影响，并探讨当前经济形势下的政策选择。",
                        dueDate = "2025-10-30",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：IS曲线表示产品市场均衡，LM曲线表示货币市场均衡，两者交点决定均衡国民收入...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/econ_answer1.pdf", 91, "分析深入，理论结合实际，论证充分", true)
                        )
                    )
                )
            }
            else -> {
                // 默认作业（适用于未指定课程）
                homeworkList.add(
                    HomeworkDetail(
                        id = "default1",
                        title = "课程作业",
                        description = "完成相关习题，掌握本章节的核心知识点",
                        dueDate = "2025-10-20",
                        submissions = mutableListOf()
                    )
                )
            }
        }
        
        // 将所有作业也加入作业库
        homeworkLibraryList.addAll(homeworkList)
        
        // 根据当前标签显示对应数据
        if (currentTab == 0) {
            homeworkAdapter.updateData(homeworkList)
        } else {
            homeworkAdapter.updateData(homeworkLibraryList)
        }
    }
    
    private fun createHomework() {
        // 创建一个示例作业请求 - 按照新的API格式
        val request = CreateHomeworkRequest(
            subjectId = courseId?.toInt(), // 使用课程ID
            homeworkName = "新创建的作业",
            homeworkContent = "这是作业内容",
            deadTime = "2025-12-31",
            imageUrls = listOf()
        )
        
        // 调用API创建作业 (@POST /api/teach/homework/create)
        RetrofitClient.apiService.createHomework(request).enqueue(object : Callback<BaseResp<String>> {
            override fun onResponse(call: Call<BaseResp<String>>, response: Response<BaseResp<String>>) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@HomeworkListActivity, "作业创建成功", Toast.LENGTH_SHORT).show()
                    // 重新加载作业列表
                    loadHomework(true)
                } else {
                    Toast.makeText(this@HomeworkListActivity, "作业创建失败: ${response.body()?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                Log.e("HomeworkList", "Failed to create homework: ${t.message}")
                Toast.makeText(this@HomeworkListActivity, "网络异常，作业创建失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_HOMEWORK && resultCode == RESULT_OK) {
            // 创建作业成功，重新加载作业列表
            loadHomework(true)
        }
    }
}