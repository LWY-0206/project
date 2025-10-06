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
import com.jxdx.classroom.R
import com.jxdx.classroom.http.ApiService
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.classroom.http.DTO.JoinStuDTO
import com.jxdx.classroom.com.jxdx.classroom.http.DTO.OutStuDTO
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.google.android.material.tabs.TabLayout
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class GroupSeatActivity : AppCompatActivity() {
    private var subjectId: Int = 1
    private var teacherId: Int = 6
    private val groups = mutableListOf<Group>()
    private val currentStudentId = 1
    private val currentStudentName = "我自己"
    private val totalStudents = 19
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
        
        // 记录参数值，便于调试
        Log.d("GroupSeatActivity", "onCreate - subjectId: $subjectId, teacherId: $teacherId")
        Log.d("GroupSeatActivity", "onCreate - Token exists: ${TokenManager.getToken() != null}")

        // 先生成本地默认小组数据作为备用
        generateGroups()
        // 添加UI渲染调用，确保初始时显示分组内容
        renderGroupsUI()
        // 尝试从API获取小组列表数据
        loadGroupsFromApi()
    }

    private fun initViews() {
        // 使用findViewById初始化所有控件
        ivBack = findViewById(R.id.ivBack)
        btnAutoAssign = findViewById(R.id.btnAutoAssign)
        btnStartDiscussion = findViewById(R.id.btnStartDiscussion)
        groupContainer = findViewById(R.id.groupContainer)
        tvTotalStudents = findViewById(R.id.tvTotalStudents)
        tvAssignedStudents = findViewById(R.id.tvAssignedStudents)
        tvRemainingStudents = findViewById(R.id.tvRemainingStudents)

        // 返回按钮
        ivBack.setOnClickListener {
            finish()
            // 使用存在的slide_in_right动画替代不存在的slide_in_left
            try {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } catch (e: Exception) {
                // 忽略动画资源不存在的错误
            }
        }

        // 自动分配按钮
        btnAutoAssign.setOnClickListener {
            autoAssignRemaining()
        }

        // 开始讨论按钮
        btnStartDiscussion.setOnClickListener {
            startDiscussionForAllGroups()
        }

        // 更新统计数据
        updateStats()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }

    // 从API获取小组列表
    private fun loadGroupsFromApi() {
        activityScope.launch {
            try {
                val token = TokenManager.getToken() ?: ""
                
                // 检查Token是否存在
                if (token.isEmpty()) {
                    Log.e("GroupSeatActivity", "Token为空，可能需要重新登录")
                    // 即使Token为空，也继续尝试API调用，让服务器返回具体的错误信息
                }
                
                // 记录API调用参数
                Log.d("GroupSeatActivity", "loadGroupsFromApi - 调用API参数: subjectId=$subjectId, createdBy=$teacherId")
                
                // 使用从intent获取的subjectId和teacherId
                val response = RetrofitClient.apiService.getGroups(token, subjectId, teacherId)
                
                // 记录API响应
                Log.d("GroupSeatActivity", "loadGroupsFromApi - API响应: code=${response.code}, message=${response.message}, dataSize=${response.data?.size ?: 0}")
                
                if (response.code == 0 && response.data != null) {
                    // 清空现有小组数据
                    groups.clear()
                    assignedCount = 0
                    
                    // 处理API返回的小组数据
                    val data = response.data
                    // 使用安全调用处理可空类型
                    if (data?.isNotEmpty() == true) {
                        data.forEach { group ->
                            groups.add(group)
                            // 已分配的学生数
                            assignedCount += group.currentCount
                        }
                        
                        // 重新渲染UI
                        renderGroupsUI()
                        updateStats()
                        Log.d("GroupSeatActivity", "成功从API获取小组数据")
                    } else {
                        // 如果API返回空数据，确保有默认数据显示
                        Log.d("GroupSeatActivity", "API返回空数据，使用本地生成数据")
                        generateGroups()
                        renderGroupsUI()
                        updateStats()
                    }
                } else {
                    // 如果API调用失败，继续使用本地生成的小组数据
                    Log.e("GroupSeatActivity", "从API获取小组数据失败: ${response.message ?: "未知错误"}")
                    // 如果API返回的数据为空，则确保有默认数据显示
                    if (groups.isEmpty()) {
                        generateGroups()
                        renderGroupsUI()
                        updateStats()
                    }
                    
                    // 特殊处理登录相关错误
                    if (response.code == 401 || response.code == 403 || 
                        response.message?.contains("未登录") == true || 
                        response.message?.contains("token") == true) {
                        Log.e("GroupSeatActivity", "检测到登录状态异常，建议重新登录")
                        // 在UI线程显示提示
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@GroupSeatActivity, "登录状态已失效，请重新登录", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupSeatActivity", "API调用异常: ${e.message}")
                e.printStackTrace() // 打印完整异常堆栈，便于调试
                
                // 异常情况下，确保有默认数据显示
                if (groups.isEmpty()) {
                    generateGroups()
                    renderGroupsUI()
                    updateStats()
                }
                
                // 显示网络异常提示
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GroupSeatActivity, "网络连接异常，请检查网络设置", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //初始化空的学生列表
    private fun generateGroups() {
        groups.clear() // 清空现有小组数据
        assignedCount = 0 // 重置已分配计数
        
        // 计算小组数量：对于19人分5人一组，应该是4个小组(5+5+5+4=19)
        val groupCount = if (totalStudents % groupSize == 0) {
            totalStudents / groupSize
        } else {
            totalStudents / groupSize + 1
        }
        
        // 实际需要检查的是：有多少个小组可以达到满员状态
        val fullGroupsCount = totalStudents / groupSize
        val remainingStudents = totalStudents % groupSize
        
        // 创建小组
        for (i in 1..groupCount) {
            // 计算当前小组的成员数
            val actualSize = if (i <= fullGroupsCount) {
                // 前fullGroupsCount个小组是满员的
                groupSize
            } else {
                // 最后一个小组处理剩下的学生
                remainingStudents
            }
            
            // 初始化空的学生列表
            val students = MutableList<Student?>(actualSize) { null }
            groups.add(Group(i, "小组 $i", actualSize, 0, students))
        }
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
        // 移除不存在的tvMemberCount控件引用
        
        // 设置小组名称
        tvGroupName.text = group.name
        // 移除对不存在的tvMemberCount的设置
        
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
            }
            else -> {
                tvGroupStatus.text = "待分组"
                tvGroupStatus.setBackgroundResource(R.drawable.bg_status_pending)
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
            if (isLeader) {
                ivAvatar.setImageResource(R.drawable.ic_leader_avatar)
                ivLeaderBadge.visibility = View.VISIBLE
                tvName.setTextColor(ContextCompat.getColor(this, R.color.leader_color))
            } else {
                ivAvatar.setImageResource(R.drawable.ic_student_avatar)
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
            val filledSeats = getFilledSeatsCount(group)
            if (filledSeats >= group.capacity) {
                Toast.makeText(this, "该小组已锁定", Toast.LENGTH_SHORT).show()
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
                // 创建当前学生对象，使用正确的字段名avatarUrl
                val currentStudent = Student(currentStudentId, currentStudentName, null, isLeader)
                
                // 创建JoinStuDTO请求体
                val joinRequest = JoinStuDTO(
                    teamId = group.id,
                    index = seatIndex,
                    student = currentStudent
                )
                
                // 调用API加入小组
                val token = TokenManager.getToken() ?: ""
                val response = RetrofitClient.apiService.joinGroups(token, joinRequest)    //学生加入小组
                
                if (response.code == 0) {
                    // API调用成功，更新本地数据
                    withContext(Dispatchers.Main) {
                        // 将学生保存到Group.students的相应位置
                        group.students[seatIndex] = currentStudent
                        assignedCount++

                        updateStats()
                        showJoinAnimation(view)
                        Toast.makeText(this@GroupSeatActivity, "成功加入 ${group.name}", Toast.LENGTH_SHORT).show()
                        renderGroupsUI()
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
                    Log.e("GroupSeatActivity", "加入小组异常: ${e.message}")
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
                        Log.e("GroupSeatActivity", "退出小组异常: ${e.message}")
                    }
                }
                
                // 更新本地数据
                group.students?.set(index, null)
                assignedCount--
            }
        }
    }

    private fun showCancelPositionDialog(onConfirm: () -> Unit) {
        Toast.makeText(this, "确定要取消当前位置选择吗？", Toast.LENGTH_LONG).show()
        // 为了简化，这里直接在短暂延迟后执行确认操作
        Handler(Looper.getMainLooper()).postDelayed({
            onConfirm()
        }, 1500)
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
        
        // 分配到选择的位置，使用正确的字段名avatarUrl
        val currentStudent = Student(currentStudentId, currentStudentName, null, randomSlot.isLeader)
        randomSlot.group.students[randomSlot.seatIndex] = currentStudent
        assignedCount++
        updateStats()
        renderGroupsUI()
        Toast.makeText(this, "已为你自动分配到${randomSlot.group.name}", Toast.LENGTH_SHORT).show()
    }

    // 辅助类，用于存储空位信息
    private data class EmptySlot(
        val group: Group,
        val isLeader: Boolean,
        val seatIndex: Int
    )

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
            Log.e("GroupSeatActivity", "更新统计数据失败: ${e.message}")
        }
    }

    private fun isAllGroupsFull(): Boolean {
        return assignedCount >= totalStudents
    }

    private fun startDiscussionForAllGroups() {
        groups.forEach { group ->
            if (group.students.any { it?.id == currentStudentId }) {
                enterDiscussion(group)
                return
            }
        }
        // 如果当前学生不在任何组，自动分配到第一个组
        enterDiscussion(groups.first())
    }

    private fun enterDiscussion(group: Group) {
        val intent = Intent(this, DiscussionActivity::class.java)
        intent.putExtra("groupId", group.id)
        intent.putExtra("groupName", group.name)
        intent.putExtra("isTeacher", false)  //学生进入讨论区
        startActivity(intent)
        try {
            // 使用存在的slide_in_right动画
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            // 忽略动画资源不存在的错误
        }
    }
}
