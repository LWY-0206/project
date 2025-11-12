package com.jxdx.classroom.group

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.jxdx.classroom.R
import com.jxdx.classroom.http.ApiService
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.classroom.http.DTO.JoinStuDTO
import com.jxdx.classroom.http.DTO.OutStuDTO
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.google.android.material.tabs.TabLayout
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import com.jxdx.classroom.group.Member
import com.google.gson.Gson

class GroupSeatActivity : AppCompatActivity() {
    private var subjectId: Int = 1
    private var teacherId: Int = 6
    private val groups = mutableListOf<Group>()
    private val currentStudentId = 1
    private val currentStudentName = "我自己"
    private var currentStudentAvatar: String? = null
    private var totalStudents = 19 
    private val groupSize = 5
    private var assignedCount = 0
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 正确声明findViewById需要的控件变量
    private lateinit var ivBack: ImageView
    private lateinit var btnAutoAssign: Button
    private lateinit var btnStartDiscussion: Button
    private lateinit var groupContainer: LinearLayout
    private lateinit var tvTotalStudents: TextView
    private lateinit var tvAssignedStudents: TextView
    private lateinit var tvRemainingStudents: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_seat)
        
        // 初始化控件
        initViews()
        
        // 正确获取从上一个界面传递的courseId
        subjectId = intent.getIntExtra("subjectId", 1)
        // 获取老师ID
        teacherId = intent.getIntExtra("teacherId", 6)

        // 尝试获取当前学生的头像URL
        // 注意：在实际应用中，应该从用户登录信息或API中获取真实的头像URL
        // 这里使用一个模拟的头像URL作为示例
        currentStudentAvatar =
            "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/20250905/Jr96/458X300/90.jpg"
        
        // 生成默认的小组数据
        generateGroups()
        
        // 渲染UI
        renderGroupsUI()
        
        // 尝试从API加载真实的小组数据
        loadGroupsFromApi()
    }

    private fun initViews() {
        // 初始化控件
        ivBack = findViewById(R.id.ivBack)
        btnAutoAssign = findViewById(R.id.btnAutoAssign)
        btnStartDiscussion = findViewById(R.id.btnStartDiscussion)
        groupContainer = findViewById(R.id.groupContainer)
        tvTotalStudents = findViewById(R.id.tvTotalStudents)
        tvAssignedStudents = findViewById(R.id.tvAssignedStudents)
        tvRemainingStudents = findViewById(R.id.tvRemainingStudents)
        
        // 设置点击事件
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        btnAutoAssign.setOnClickListener {
            autoAssignRemaining()
        }

        btnStartDiscussion.setOnClickListener {
            // 查找当前学生所在的小组
            val currentPosition = findCurrentStudentPosition()
            if (currentPosition != null) {
                val (group, _) = currentPosition
                if (group != null) {
                    val intent = Intent(this, DiscussionActivity::class.java)
                    // 传递小组ID（作为整数传递）
                    intent.putExtra("groupId", group.id)
                    intent.putExtra("groupName", group.name)
                    
                    // 将小组成员信息转换为JSON字符串并传递
                    val gson = Gson()
                    intent.putExtra("groupStudentsJson", gson.toJson(group.students))
                    intent.putExtra("subjectId", subjectId) // 传递subjectId
                    
                    // 跳转到讨论页面
                    startActivity(intent)
                    return@setOnClickListener
                }
            }
            
            // 如果未找到小组或未分配位置，显示提示
            Toast.makeText(this, "请先加入小组后再开始讨论", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }

    private fun loadGroupsFromApi() {
        activityScope.launch {
            try {
                // 记录API调用参数
                Log.d("GroupSeat", "调用API加载小组数据: subjectId=$subjectId, teacherId=$teacherId")
                
                val token = TokenManager.getToken() ?: ""
                // 检查API定义，确保参数名正确
                val response = RetrofitClient.apiService.getGroups(token, subjectId, teacherId)
                
                if (response.code == 0) {
                    // 清空现有的小组数据
                    groups.clear()
                    assignedCount = 0
                    
                    // 将API返回的小组数据映射到本地数据结构
                    response.data?.forEach { groupDTO ->
                        // 确保小组容量合理，如果API返回的容量小于2，则使用应用程序中定义的groupSize
                        val capacity = groupDTO.capacity
                        val group = Group(
                            id = groupDTO.id,
                            name = groupDTO.name,
                            capacity = capacity,
                            currentCount = groupDTO.currentCount,
                            students = MutableList(capacity) { null }
                        )
                        
                        // 初始化所有位置为null
                        group.students = MutableList(capacity) { null }
                        
                        // 映射学生数据，按照memberIndex来安排学生在小组中的位置
                        groupDTO.students?.forEach { studentDTO ->
                            if (studentDTO != null) {
                                // 确保学生核心信息完整
                                val studentId = studentDTO.id
                                val studentName = studentDTO.name ?: "未知学生"
                                
                                if (studentId == 0 || studentName.isEmpty()) {
                                    Log.w("GroupSeat", "忽略无效学生数据: ID=${studentDTO.id}, name=${studentDTO.name}")
                                    return@forEach
                                }
                                
                                // 获取学生的memberIndex，如果为null则使用当前迭代的索引
                                val memberIndex = studentDTO.memberIndex ?: 0
                                
                                // 记录加载学生信息，用于调试
                                Log.d("GroupSeat", "加载学生: ID=${studentId}, name=${studentName}, 原始memberIndex=${studentDTO.memberIndex}, 实际使用memberIndex=$memberIndex")
                                
                                // 确保memberIndex在有效范围内
                                if (memberIndex >= 0 && memberIndex < capacity) {
                                    // 使用API返回的memberIndex作为目标位置
                                    val targetIndex = memberIndex
                                    
                                    // 确保位置有效
                                    if (targetIndex < capacity) {
                                        // 创建学生对象，使用正确的字段名，并为可能为null的name提供默认值
                                        val student = Student(
                                            id = studentId,
                                            name = studentName,  // 处理name可能为null的情况
                                            avatarUrl = studentDTO.avatarUrl,
                                            isLeader = studentDTO.isLeader ?: false, // 为isLeader提供默认值
                                            memberIndex = memberIndex // 保持原始的memberIndex
                                        )
                                        
                                        // 如果是当前学生，确保使用当前学生的完整信息
                                        if (studentId == currentStudentId) {
                                            Log.d("GroupSeat", "检测到当前学生，使用本地完整数据")
                                            val currentUserStudent = Student(
                                                id = currentStudentId,
                                                name = currentStudentName,
                                                avatarUrl = currentStudentAvatar,
                                                isLeader = student.isLeader,
                                                memberIndex = memberIndex
                                            )
                                            group.students[targetIndex] = currentUserStudent
                                        } else {
                                            group.students[targetIndex] = student
                                        }
                                        assignedCount++
                                        Log.d("GroupSeat", "学生${studentName} 放置在位置$targetIndex")
                                    } else {
                                        Log.w("GroupSeat", "小组${group.name}已满，无法放置学生${studentName}")
                                    }
                                } else {
                                    Log.w("GroupSeat", "忽略无效的memberIndex: $memberIndex 对于学生ID: ${studentId}")
                                }
                            }
                        }
                        
                        groups.add(group)
                    }
                    
                    // 检查是否需要生成默认小组数据
                    if (groups.isEmpty()) {
                        Log.d("GroupSeat", "API返回的小组数据为空，生成默认小组数据")
                        generateGroups()
                    } else {
                        // 即使小组都没有学生，也要显示从API返回的空组
                        Log.d("GroupSeat", "API返回了${groups.size}个小组，即使它们可能都是空组")
                    }
                    
                    // 更新总学生数
                    totalStudents = calculateTotalStudentsCapacity()
                    
                    // 记录小组加载结果，包括空组信息
                    val emptyGroupsCount = groups.count { it.currentCount == 0 }
                    Log.d("GroupSeat", "成功加载小组数据: 共${groups.size}个小组, 其中空组${emptyGroupsCount}个, 已分配${assignedCount}人, 总容量${totalStudents}人")
                    
                    // 检查当前学生是否在已分配的小组中
                    val currentStudentPosition = findCurrentStudentPosition()
                    if (currentStudentPosition != null) {
                        val (group, index) = currentStudentPosition
                        Log.d("GroupSeat", "当前学生已在${group?.name}的位置${index+1}")
                    } else {
                        Log.d("GroupSeat", "当前学生尚未分配位置")
                    }
                    
                    // 渲染UI
                    renderGroupsUI()
                    updateStats()
                } else {
                    Log.e("GroupSeat", "加载小组数据失败: ${response.message}")
                    // 显示加载失败提示
                    Toast.makeText(this@GroupSeatActivity, "加载小组数据失败: ${response.message}", Toast.LENGTH_SHORT).show()
                    // 加载失败时也生成默认小组数据
                    generateGroups()
                    renderGroupsUI()
                    updateStats()
                }
            } catch (e: Exception) {
                Log.e("GroupSeat", "加载小组数据异常: ${e.message}")
                // 显示网络异常提示
                Toast.makeText(this@GroupSeatActivity, "网络异常，请检查网络连接后重试", Toast.LENGTH_SHORT).show()
                // 异常时生成默认小组数据
                generateGroups()
                renderGroupsUI()
                updateStats()
            }
        }
    }

    private fun generateGroups() {
        // 清空现有的小组数据
        groups.clear()
        assignedCount = 0
        
        // 计算需要创建的小组数量
        val groupCount = Math.ceil(totalStudents.toDouble() / groupSize).toInt()
        
        // 创建小组
        for (i in 1..groupCount) {
            // 计算小组容量
            val capacity = if (i == groupCount) {
                totalStudents - (groupCount - 1) * groupSize
            } else {
                groupSize
            }
            
            // 创建小组并添加到列表
            groups.add(Group(
                id = i,
                name = "第${i}小组",
                capacity = capacity,
                currentCount = 0,
                students = MutableList(capacity) { null }
            ))
        }
        
        // 更新总学生数
        totalStudents = calculateTotalStudentsCapacity()
    }

    // 计算所有小组的容量之和
    private fun calculateTotalStudentsCapacity(): Int {
        return groups.sumOf { it.capacity }
    }

    private fun renderGroupsUI() {
        // 确保groupContainer已初始化
        if (!this::groupContainer.isInitialized) {
            groupContainer = findViewById(R.id.groupContainer)
        }
        
        groupContainer.removeAllViews()
        
        for (group in groups) {
            val groupCard = layoutInflater.inflate(R.layout.item_group_card, null)
            setupGroupCard(groupCard, group)
            groupContainer.addView(groupCard)
        }
    }

    private fun setupGroupCard(view: View, group: Group) {
        // 修正ID名称从tvGroupName到tvGroupTitle
        val tvGroupName = view.findViewById<TextView>(R.id.tvGroupTitle)
        val tvGroupStatus = view.findViewById<TextView>(R.id.tvGroupStatus)
        val ivGroupLock = view.findViewById<ImageView>(R.id.ivGroupLock)
        val memberContainer = view.findViewById<LinearLayout>(R.id.memberContainer)
        
        // 设置小组名称
        tvGroupName.text = group.name
        
        // 设置小组状态
        val filledSeats = getFilledSeatsCount(group)
        when {
            filledSeats >= group.capacity -> {
                tvGroupStatus.text = "已锁定"
                tvGroupStatus.setBackgroundResource(R.drawable.bg_status_completed)
                ivGroupLock.visibility = View.VISIBLE
                ivGroupLock.setColorFilter(ContextCompat.getColor(this, R.color.success))
            }
            filledSeats > 0 -> {
                tvGroupStatus.text = "进行中"
                tvGroupStatus.setBackgroundResource(R.drawable.bg_status_in_progress)
                ivGroupLock.visibility = View.GONE
            }
            else -> {
                tvGroupStatus.text = "待分组"
                tvGroupStatus.setBackgroundResource(R.drawable.bg_status_pending)
                ivGroupLock.visibility = View.GONE
            }
        }
        
        // 渲染成员座位
        memberContainer.removeAllViews()
        
        // 渲染所有座位
        for ((index, student) in group.students.withIndex()) {
            // 第一个位置设为组长位
            val isLeader = index == 0 && student?.isLeader == true
            memberContainer.addView(createSeatView(student, isLeader, group, index))
        }
    }

    //当前小组人数
    private fun getFilledSeatsCount(group: Group): Int {
        return group.students.count { it != null }
    }

    private fun createSeatView(
        student: Student?,
        isLeader: Boolean,
        group: Group,
        seatIndex: Int
    ): View {
        val view = layoutInflater.inflate(R.layout.item_seat, null)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val ivLeaderBadge = view.findViewById<ImageView>(R.id.ivLeaderBadge)
        val vOnlineStatus = view.findViewById<View>(R.id.vOnlineStatus)
        val loadingAnimation = view.findViewById<LottieAnimationView>(R.id.loadingAnimation)

        if (student != null) {
            // 有学生的情况
            tvName.text = student.name
            // 加载用户真实头像
            if (!student.avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(student.avatarUrl)
                    .circleCrop()
                    .into(ivAvatar)
            } else if (isLeader) {
                // 如果是组长且没有头像URL，使用组长默认头像
                ivAvatar.setImageResource(R.drawable.ic_leader_avatar)
            } else {
                // 其他情况使用学生默认头像
                ivAvatar.setImageResource(R.drawable.ic_student_avatar)
            }

            // 如果是组长，显示组长标识
            if (isLeader) {
                ivLeaderBadge.visibility = View.VISIBLE
                tvName.setTextColor(ContextCompat.getColor(this, R.color.leader_color))
            } else {
                vOnlineStatus.visibility = View.VISIBLE
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }

            // 当前学生高亮显示
            if (student.id == currentStudentId) {
                view.background = ContextCompat.getDrawable(this, R.drawable.bg_current_user)
            }
        } else {
            // 空位情况
            tvName.text = if (seatIndex == 0) "组长空位" else "空位"
            ivAvatar.setImageResource(R.drawable.ic_add_circle)
            ivAvatar.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        // 点击事件
            view.setOnClickListener {
                // 检查小组是否已锁定（通过endGroup接口标记）
                if (group.isLocked) {
                    Toast.makeText(this, "分组已结束，不能更改小组", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val filledSeats = getFilledSeatsCount(group)
                if (filledSeats >= group.capacity) {
                    Toast.makeText(this, "该小组已满", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            if (student == null) {
                // 检查是否已经在其他位置
                val currentPosition = findCurrentStudentPosition()
                if (currentPosition != null) {
                    // 询问是否要离开当前位置
                    val groupType = if (currentPosition.second == 0) "组长" else "成员"

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("")
                    builder.setMessage(
                        "你已经是${currentPosition.first?.name ?: "某小组"}的$groupType，是否要更换位置？",
                    )
                    builder.setPositiveButton("确定") { dialog, which ->
                        // 先移除当前位置，再加入新位置
                        removeCurrentStudent()
                        joinNewPosition(view, loadingAnimation, seatIndex == 0, group, seatIndex)
                    }
                    builder.setNegativeButton("取消") { dialog, which ->

                    }
                    builder.show()

                } else {
                    // 没有位置直接加入新位置
                    joinNewPosition(view, loadingAnimation, seatIndex == 0, group, seatIndex)
                }
            } else if (student.id == currentStudentId) {
                // 点击自己的位置，提供取消选项
                showCancelPositionDialog {
                    removeCurrentStudent()
                    renderGroupsUI()
                    Toast.makeText(this, "已取消位置选择", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "${student.name} 的座位", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun joinNewPosition(
        view: View, 
        loadingAnimation: LottieAnimationView, 
        isLeader: Boolean, 
        group: Group, 
        seatIndex: Int
    ) {
        showLoadingAnimation(loadingAnimation, true)

        activityScope.launch {
            try {
                // 首先检查并移除当前学生的位置（如果有）
                val currentPosition = findCurrentStudentPosition()
                if (currentPosition != null) {
                    val (currentGroup, currentIndex) = currentPosition
                    // 如果要加入的是当前所在的组且位置相同，则不需要操作
                    if (currentGroup?.id == group.id && currentIndex == seatIndex) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@GroupSeatActivity, "您已经在该位置", Toast.LENGTH_SHORT).show()
                            showLoadingAnimation(loadingAnimation, false)
                        }
                        return@launch
                    }
                    // 否则先移除当前位置
                    removeCurrentStudent()
                }
                
                // 记录座位索引信息，用于调试
                Log.d("GroupSeat", "用户点击的位置索引: ${seatIndex}")
                
                // 验证当前学生信息
                if (currentStudentId == 0 || currentStudentName.isEmpty()) {
                    Log.e("GroupSeat", "当前学生信息不完整: ID=${currentStudentId}, 姓名=${currentStudentName}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GroupSeatActivity, "学生信息不完整，无法加入小组", Toast.LENGTH_SHORT).show()
                        showLoadingAnimation(loadingAnimation, false)
                    }
                    return@launch
                }
                
                // 确认座位索引有效
                if (seatIndex < 0 || seatIndex >= group.capacity) {
                    Log.e("GroupSeat", "无效的座位索引: ${seatIndex}, 小组容量: ${group.capacity}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GroupSeatActivity, "无效的座位位置", Toast.LENGTH_SHORT).show()
                        showLoadingAnimation(loadingAnimation, false)
                    }
                    return@launch
                }
                
                // 创建当前学生对象，明确指定所有字段
                val currentStudent = Student(
                    id = currentStudentId,
                    name = currentStudentName,
                    avatarUrl = currentStudentAvatar,
                    isLeader = isLeader,
                    memberIndex = seatIndex
                )
                
                Log.d("GroupSeat", "创建学生对象: ID=${currentStudentId}, 姓名=${currentStudentName}, 位置=${seatIndex}, isLeader=${isLeader}")
                
                // 确保学生信息完整
                if (currentStudent.id == 0 || currentStudent.name.isEmpty()) {
                    Log.e("GroupSeat", "学生对象信息不完整: $currentStudent")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GroupSeatActivity, "学生信息不完整，无法加入小组", Toast.LENGTH_SHORT).show()
                        showLoadingAnimation(loadingAnimation, false)
                    }
                    return@launch
                }
                
                // 创建JoinStuDTO请求体，确保所有字段都被正确设置
                val joinRequest = JoinStuDTO(
                    teamId = group.id,
                    student = Student(
                        id = currentStudentId,
                        name = currentStudentName,
                        avatarUrl = currentStudentAvatar,
                        isLeader = isLeader,
                        memberIndex = seatIndex
                    )
                )
                
                // 记录完整的请求体信息用于调试
                Log.d("GroupSeat", "完整的JoinStuDTO请求体: teamId=${group.id}, student=${currentStudent}")
                
                // 记录加入请求信息，详细记录学生对象的每个字段
                Log.d("GroupSeat", "发送加入小组请求: teamId=${group.id}, 请求位置=${seatIndex}")
                Log.d("GroupSeat", "学生信息明细: id=${currentStudent.id}, name=${currentStudent.name}, avatarUrl=${currentStudent.avatarUrl}, isLeader=${currentStudent.isLeader}, memberIndex=${currentStudent.memberIndex}")
                
                // 确保学生对象的所有必需字段都有值
                if (currentStudent.id == 0 || currentStudent.name.isEmpty()) {
                    Log.e("GroupSeat", "严重错误: 学生对象的关键字段为空，无法发送有效的API请求")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GroupSeatActivity, "学生信息不完整，无法加入小组", Toast.LENGTH_SHORT).show()
                        showLoadingAnimation(loadingAnimation, false)
                    }
                    return@launch
                }
                
                // 调用API加入小组
                val token = TokenManager.getToken() ?: ""
                val response = RetrofitClient.apiService.joinGroups(token, joinRequest)    //学生加入小组
                
                // 记录API响应
                Log.d("GroupSeat", "加入小组API响应: code=${response.code}, message=${response.message}")
                
                if (response.code == 0) {
                    // API调用成功，确保本地数据立即更新到用户点击的位置
                    withContext(Dispatchers.Main) {
                        // 确保位置有效并立即更新UI
                        if (seatIndex < group.capacity) {
                            group.students[seatIndex] = currentStudent
                            assignedCount++
                        }

                        updateStats()
                        showJoinAnimation(view)
                        Toast.makeText(this@GroupSeatActivity, "成功加入 ${group.name}", Toast.LENGTH_SHORT).show()
                        
                        // 立即渲染UI，让用户看到学生在正确的位置
                        renderGroupsUI()
                        
                        // 在后台重新加载数据，但不覆盖用户选择的位置
                        Log.d("GroupSeat", "加入成功，在后台重新加载小组数据以确保其他数据一致性")
                        activityScope.launch {
                            try {
                                loadGroupsFromApi()
                                delay(500) // 增加延迟时间以确保数据完全加载完成
                                withContext(Dispatchers.Main) {
                                    // 再次确认学生在用户点击的位置
                                    val targetGroup = groups.find { it.id == group.id }
                                    if (targetGroup != null && seatIndex < targetGroup.students.size) {
                                        // 重新设置学生到用户点击的位置，确保使用当前学生的完整信息
                                        val currentUserStudent = Student(
                                            id = currentStudentId,
                                            name = currentStudentName,
                                            avatarUrl = currentStudentAvatar,
                                            isLeader = isLeader,
                                            memberIndex = seatIndex
                                        )
                                        
                                        // 即使API返回的数据可能有不同的位置，也要强制设置到用户点击的位置
                                        targetGroup.students[seatIndex] = currentUserStudent
                                        Log.d("GroupSeat", "已确保学生在请求的位置${seatIndex}")
                                        renderGroupsUI()
                                    } else {
                                        Log.w("GroupSeat", "无法找到目标组或位置无效")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("GroupSeat", "重新加载数据时出错", e)
                            }
                        }
                        
                        showLoadingAnimation(loadingAnimation, false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GroupSeatActivity, "加入失败: ${response.message}", Toast.LENGTH_SHORT).show()
                        showLoadingAnimation(loadingAnimation, false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GroupSeat", "加入小组异常: ${e.message}")
                    Toast.makeText(this@GroupSeatActivity, "网络异常，请重试", Toast.LENGTH_SHORT).show()
                    showLoadingAnimation(loadingAnimation, false)
                }
            }
        }
    }

    // 查询是否选择分组，若是返回位置Pair<Group, Int> 位置索引
    private fun findStudentInGroups(studentId: Int): Pair<Group?, Int>? {
        for (group in groups) {
            for ((index, student) in group.students.withIndex()) {
                if (student?.id == studentId) {
                    return Pair(group, index)
                }
            }
        }
        return null
    }

    // 查询当前学生的位置
    private fun findCurrentStudentPosition(): Pair<Group?, Int>? {
        return findStudentInGroups(currentStudentId)
    }

    //移除分组
    private fun removeCurrentStudent() {
        val currentPosition = findCurrentStudentPosition()
        if (currentPosition != null) {
            val (group, index) = currentPosition
            val student = group?.students?.get(index)
            
            if (student != null) {
                activityScope.launch {
                    try {
                        // 调用API退出小组
                        val token = TokenManager.getToken() ?: ""
                        // 创建OutStuDTO请求体
                        val outRequest = OutStuDTO(
                            teamId = group.id,
                            userId = student.id,
                            isLeader = student.isLeader
                        )
                        RetrofitClient.apiService.outGroups(token, outRequest)
                    } catch (e: Exception) {
                        Log.e("GroupSeat", "退出小组异常: ${e.message}")
                    }
                }
                
                // 更新本地数据
                group.students?.set(index, null)
                assignedCount--
            }
        }
    }

    private fun showCancelPositionDialog(onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        builder.setMessage("确定要取消位置选择吗？")
        builder.setPositiveButton("确定") { dialog, which ->
            onConfirm()
        }
        builder.setNegativeButton("取消") { dialog, which ->
        }
        builder.show()
    }

    private fun autoAssignRemaining() {
        // 首先检查当前用户是否已经有位置
        val currentPosition = findCurrentStudentPosition()
        if (currentPosition != null) {
            // 移除当前位置，再自动分配新位置
            removeCurrentStudent()
            assignSinglePosition()
        } else {
            // 如果没有位置，分配一个位置
            assignSinglePosition()
        }
    }

    private fun assignSinglePosition() {
        // 首先检查并移除当前学生的位置（如果有）
        val currentPosition = findCurrentStudentPosition()
        if (currentPosition != null) {
            // 移除当前位置
            removeCurrentStudent()
        }
        
        // 查找所有空座位
        val emptySlots = mutableListOf<EmptySlot>()
        
        // 查找所有空位
        for (group in groups) {
            val filledSeats = getFilledSeatsCount(group)
            if (filledSeats < group.capacity) {
                group.students.forEachIndexed { index, student ->
                    if (student == null) {
                        emptySlots.add(EmptySlot(group, index == 0, index))
                    }
                }
            }
        }

        if (emptySlots.isEmpty()) {
            Toast.makeText(this, "没有可用的空位", Toast.LENGTH_SHORT).show()
            return
        }

        // 随机选择一个空位
        val randomSlot = emptySlots.random()
        
        // 分配到选择的位置，使用正确的具名参数
        val currentStudent = Student(
            id = currentStudentId,
            name = currentStudentName,
            avatarUrl = currentStudentAvatar,
            isLeader = randomSlot.isLeader,
            memberIndex = randomSlot.seatIndex
        )
        randomSlot.group.students[randomSlot.seatIndex] = currentStudent
        assignedCount++
        updateStats()
        
        // 渲染UI
        renderGroupsUI()
        Toast.makeText(this, "已自动分配位置", Toast.LENGTH_SHORT).show()
        
        // 调用API将分配操作同步到服务器
        activityScope.launch {
            try {
                // 创建Student对象
                val currentStudent = Student(
                    id = currentStudentId,
                    name = currentStudentName,
                    avatarUrl = currentStudentAvatar,
                    isLeader = randomSlot.isLeader,
                    memberIndex = randomSlot.seatIndex
                )
                
                // 创建JoinStuDTO请求体
                val joinRequest = JoinStuDTO(
                    teamId = randomSlot.group.id,
                    student = currentStudent
                )
                
                // 记录加入请求信息，详细记录学生对象的每个字段
                Log.d("GroupSeat", "发送自动分配位置请求: teamId=${randomSlot.group.id}, index=${randomSlot.seatIndex}")
                Log.d("GroupSeat", "学生信息明细: id=${currentStudent.id}, name=${currentStudent.name}, avatarUrl=${currentStudent.avatarUrl}, isLeader=${currentStudent.isLeader}, memberIndex=${currentStudent.memberIndex}")
                
                // 确保学生对象的所有必需字段都有值
                if (currentStudent.id == 0 || currentStudent.name.isEmpty()) {
                    Log.e("GroupSeat", "严重错误: 学生对象的关键字段为空，无法发送有效的API请求")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GroupSeatActivity, "学生信息不完整，自动分配失败", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                // 调用API加入小组
                val token = TokenManager.getToken() ?: ""
                val response = RetrofitClient.apiService.joinGroups(token, joinRequest)
                
                // 记录API响应
                Log.d("GroupSeat", "自动分配位置API响应: code=${response.code}, message=${response.message}")
                
                if (response.code != 0) {
                    withContext(Dispatchers.Main) {
                        // 处理失败情况
                        Toast.makeText(this@GroupSeatActivity, "自动分配位置失败: ${response.message}", Toast.LENGTH_SHORT).show()
                        Log.w("GroupSeat", "自动分配位置API调用失败: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GroupSeat", "自动分配位置异常: ${e.message}")
                }
            }
        }
    }

    private fun showLoadingAnimation(view: LottieAnimationView, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            view.playAnimation()
        } else {
            view.cancelAnimation()
        }
    }

    private fun showJoinAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.2f, 1.0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = android.view.animation.OvershootInterpolator()
        animatorSet.start()
    }

    private fun updateStats() {
        // 确保所有控件已初始化
        if (!this::tvTotalStudents.isInitialized) {
            tvTotalStudents = findViewById(R.id.tvTotalStudents)
        }
        if (!this::tvAssignedStudents.isInitialized) {
            tvAssignedStudents = findViewById(R.id.tvAssignedStudents)
        }
        if (!this::tvRemainingStudents.isInitialized) {
            tvRemainingStudents = findViewById(R.id.tvRemainingStudents)
        }
        
        try {
            tvTotalStudents.text = totalStudents.toString()
            tvAssignedStudents.text = assignedCount.toString()
            tvRemainingStudents.text = (totalStudents - assignedCount).toString()
        } catch (e: Exception) {
            Log.e("GroupSeat", "更新统计数据失败: ${e.message}")
        }
    }

    // 辅助类，用于存储空位信息
    private data class EmptySlot(
        val group: Group,
        val isLeader: Boolean,
        val seatIndex: Int
    )
}
