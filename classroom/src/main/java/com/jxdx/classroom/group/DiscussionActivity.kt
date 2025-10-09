package com.jxdx.classroom.group

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jxdx.classroom.R
import com.jxdx.classroom.databinding.ActivityDiscussionBinding
import com.jxdx.classroom.http.RetrofitClient
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.login.UserInfo
import kotlinx.coroutines.*
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Request
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// 小组讨论成员类
data class Member(
    val id: String,
    val name: String,
    var isOnline: Boolean
)

// 消息项的装饰器，用于设置不同类型消息之间的间距
class MessageItemDecoration : RecyclerView.ItemDecoration() {
    // 设置项目偏移
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // 添加消息项之间的垂直间距
        outRect.bottom = 12
    }
}

/**
     * 讨论组活动类
     * 负责显示小组讨论界面，处理消息发送和接收，以及各种用户交互操作
     */
class DiscussionActivity : AppCompatActivity() {
    // 视图绑定对象，用于访问XML布局中的UI元素
    private lateinit var binding: ActivityDiscussionBinding
    // 当前用户ID
    private var currentUserId = "1"
    private var currentUserName = "张三"
    // 当前用户头像
    private var currentUserAvatar: String? = null
    // 群组ID
    private  var groupId: Int=0
    // 群组名称
    private lateinit var groupName: String
    // 是否为教师模式
    private var isTeacherMode: Boolean = false
    // 在线人数
    private var onlineCount = 3
    // 消息列表
    private val messages = mutableListOf<Message>()
    // 消息适配器，用于RecyclerView的数据绑定
    private lateinit var messagesAdapter: MessagesAdapter
    // 学科ID，用于删除全部小组接口
    private var subjectId: Int = 0
    // 消息处理器，用于处理延迟任务
    private val messageHandler = Handler(Looper.getMainLooper())
    // 模拟消息列表
    private val simulatedMessages = mutableListOf(
        "大家好，今天我们来讨论项目的进展情况",
        "我已经完成了需求分析部分",
        "设计方面我有一些想法",
        "这个项目的时间节点是什么样的？",
        "我们应该制定一个详细的计划",
        "大家还有什么问题吗？"
    )
    
    // WebSocket相关
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()
    private val wsBaseUrl = "ws://121.41.176.238:8080/group/chat/"
    private var isConnected = false
    private var webSocket: WebSocket? = null
    private var reconnectionAttempts = 0
    private val maxReconnectionAttempts = 5
    private val reconnectionDelay = 3000L // 3秒重连间隔
    // 存储初始连接时的Token，用于重连时使用
    private var initialToken: String? = null

    /**
     * Activity生命周期方法 - 创建
     * 初始化Activity，设置布局，调用各初始化方法
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化视图绑定
        binding = ActivityDiscussionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 调用各初始化方法
        initRecyclerView() // 先初始化RecyclerView和messagesAdapter
        initData()
        initViews()
        setupClickListeners()

        // 加载历史消息
        loadHistoryMessages()
        
        // 连接WebSocket以接收广播消息
        connectWebSocket()
    }

    /**
     * 初始化数据
     * 从Intent获取groupId、groupName和isTeacher参数
     */
    private fun initData() {
        // 获取从上一个Activity传递过来的数据
        groupId = intent.getIntExtra("groupId", 0)
        groupName = intent.getStringExtra("groupName") ?: "讨论组"
        isTeacherMode = intent.getBooleanExtra("isTeacher", false)
        // 获取学科ID，用于删除全部小组接口
        subjectId = intent.getIntExtra("subjectId", 0)

        // 尝试获取并解析传递过来的students JSON字符串
            try {
                val studentsJson = intent.getStringExtra("groupStudentsJson")
                if (!studentsJson.isNullOrEmpty()) {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<Student>>() {}.type
                    val students = gson.fromJson<List<Student>>(studentsJson, type)
                    
                    // 将学生数据转换为Member对象并添加到groupMembers列表
                    groupMembers.clear()
                    students.forEachIndexed { index, student ->
                        if (student != null) {
                            groupMembers.add(Member(
                                id = student.id.toString(),
                                name = student.name,
                                isOnline = false // 暂时设置为离线，实际可以根据WebSocket状态更新
                            ))
                            
                            // 如果是当前用户，保存头像信息
                            if (student.id.toString() == currentUserId) {
                                currentUserAvatar = student.avatarUrl
                            }
                        }
                    }
                    Log.d("DiscussionActivity", "Successfully loaded ${students.size} students from TeacherViewActivity")
                }
            } catch (e: Exception) {
                Log.e("DiscussionActivity", "Failed to parse groupStudentsJson: ${e.message}")
                // 解析失败时，使用模拟数据
                setupMockMembers()
            }

        // 尝试从UserInfo获取当前用户的正确头像信息
        loadCurrentUserAvatarFromUserInfo()

        // 添加系统消息：用户加入讨论区
        if (!isTeacherMode) {
            addSystemMessage("${currentUserName}加入了讨论区")
        }
    }

    /**
     * 初始化视图
     * 设置标题、在线人数，配置输入框监听，处理教师模式UI
     */
    private fun initViews() {
        // 设置标题
        binding.tvGroupTitle.text = groupName
        // 设置在线人数
        updateOnlineCount()
        // 配置输入框文本变化监听
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSendButtonState()
            }
        })
        //输入框开始无文字，发送按钮先透明
        binding.ivSend.alpha = 0.5f
        // 处理教师模式UI
        if (isTeacherMode) {
            //输入框
            binding.etMessage.hint = "发布任务/评价/指导..."
            //教师专用操作按钮
            binding.ivTeacherJoin.visibility=View.VISIBLE
        }else{
            //输入框
            binding.etMessage.hint = "此处输入消息..."
            //教师专用操作按钮隐藏
            binding.ivTeacherJoin.visibility=View.GONE
        }
    }

    /**
     * 初始化RecyclerView
     * 初始化RecyclerView、消息适配器和分割线
     */
    private fun initRecyclerView() {
        // 创建消息适配器
        messagesAdapter = MessagesAdapter(messages, currentUserId, isTeacherMode)
        // 设置RecyclerView的布局管理器和适配器
        binding.rvMessages.apply {
            // 创建LinearLayoutManager并设置布局方向
            val layoutManager = LinearLayoutManager(this@DiscussionActivity)
            layoutManager.stackFromEnd = true // 从底部开始显示
            layoutManager.reverseLayout = false // 不反转布局方向，确保新消息在底部
            this.layoutManager = layoutManager
            
            adapter = messagesAdapter
            // 添加消息项之间的间距
            addItemDecoration(MessageItemDecoration())
        }
    }

    /**
     * 设置点击监听器
     * 设置返回、发送、附件、更多选项等按钮的点击监听
     */
    private fun setupClickListeners() {
        // 返回按钮点击事件
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 发送按钮点击事件
        binding.ivSend.setOnClickListener {
            sendMessage()
        }

        // 附件按钮点击事件
        binding.ivAddAttachment.setOnClickListener {
            showAttachmentOptions()
        }

        // 更多选项按钮点击事件
        binding.ivMore.setOnClickListener {
            showMoreOptions()
        }

        // 教师操作按钮
        binding.ivTeacherJoin.setOnClickListener {
            showTeacherActions()
        }




        // 教师模式下，长按发送按钮显示教师操作
        if (isTeacherMode) {
            binding.ivSend.setOnLongClickListener {
                showTeacherActions()
                true
            }
        }
    }

    /**
     * 发送消息
     * 发送用户输入的消息到聊天列表
     */
    private fun sendMessage() {
        // 获取输入框内容并去除前后空格
        val content = binding.etMessage.text.toString().trim()
        if (content.isNotEmpty()) {
            // 清空输入框
            binding.etMessage.text?.clear()
            // 更新发送按钮状态
            updateSendButtonState()

            // 通过API发送消息
            lifecycleScope.launch { 
                try {
                    // 根据是否为教师模式设置不同的fromUserId
                    val requestFromUserId = if (isTeacherMode) {
                        // 教师模式下使用特殊标识
                        -1 // 假设-1代表教师ID
                    } else {
                        currentUserId.toIntOrNull() ?: 1001
                    }
                    
                    val request = GroupChatApi.SendMessage.RequestBody(
                        teamId = groupId,
                        fromUserId = requestFromUserId,
                        content = content,
                        messageType = GroupChatApi.Broadcast.MessageType.TEXT
                    )

                    val response = RetrofitClient.apiService.sendGroupChatMessage(
                        request = request
                    )

                    if (response.code != 0 || response.data == null) {
                        Log.d("DiscussionActivity", "消息发送失败: ${response.message}")
                        Toast.makeText(this@DiscussionActivity, "消息发送失败", Toast.LENGTH_SHORT).show()
                    } else {
                        // 发送成功，先保存消息到服务器
                        val saveRequest = GroupChatApi.SaveMessage.RequestBody(
                            teamId = groupId,
                            fromUserId = requestFromUserId,
                            content = content,
                            messageType = GroupChatApi.Broadcast.MessageType.TEXT
                        )
                        
                        val saveResponse = RetrofitClient.apiService.saveGroupChatMessage(
                            request = saveRequest
                        )
                        
                        if (saveResponse.code == 0 && saveResponse.data != null) {
                            // 保存成功后，再添加到本地消息列表
                            val message = Message(
                                id = "msg_${System.currentTimeMillis()}",
                                senderId = if (isTeacherMode) -1 else currentUserId.toIntOrNull() ?: -1,
                                senderName = if (isTeacherMode) "教师" else currentUserName,
                                content = content,
                                timestamp = System.currentTimeMillis(),
                                isFromTeacher = isTeacherMode,
                                senderAvatar = currentUserAvatar // 添加头像信息
                            )
                            // 调用addMessage方法添加消息，确保UI正确更新
                            addMessage(message)
                        } else {
                            Log.d("DiscussionActivity", "消息保存失败: ${saveResponse.message}")
                        }
                    }
                } catch (e: Exception) {
                    // 网络异常
                    e.printStackTrace()
                    Toast.makeText(this@DiscussionActivity, "网络连接异常，消息发送失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 添加消息
     * 添加消息到列表并更新UI
     * @param message 要添加的消息对象
     */
    private fun addMessage(message: Message) {
        // 检查消息是否已存在，避免重复添加
        val isMessageExists = messages.any { it.id == message.id }
        if (!isMessageExists) {
            // 将消息添加到列表
            messages.add(message)
            // 通知适配器数据变化
            messagesAdapter.notifyItemInserted(messages.size - 1)
            // 滚动到最新消息
            binding.rvMessages.scrollToPosition(messages.size - 1)
            // 为新消息添加动画效果
            animateMessageAppearance(messages.size - 1)
        }
    }

    /**
     * 添加系统消息
     * 添加系统消息（如进入/离开提示）到聊天列表
     * @param content 系统消息内容
     */
    private fun addSystemMessage(content: String) {
        // 创建系统消息对象
        val systemMessage = Message(
            id = "sys_${System.currentTimeMillis()}",
            senderId = 0,
            senderName = "系统",
            content = content,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.SYSTEM
        )
        // 添加系统消息到列表
        addMessage(systemMessage)
    }

    /**
     * 处理接收到的广播消息
     * @param message 接收到的消息内容
     */
    private fun handleBroadcastMessage(message: String) {
        try {
            // 解析消息数据
            // 使用typeToken来安全地进行类型转换
            val messageData = gson.fromJson(message, object : TypeToken<Map<String, Any>>() {}.type)
                as Map<String, Any>
            val type = messageData["type"] as? String
            
            when (type) {
                "broadcast" -> {
                    // 处理普通广播消息
                    val content = messageData["content"] as? String
                    if (content != null) {
                        // 尝试从消息数据中获取原始消息ID
                        val originalMessageId = messageData["messageId"] as? String
                        
                        // 创建广播消息对象
                        val broadcastMessage = Message(
                            id = originalMessageId ?: "broadcast_${System.currentTimeMillis()}",
                            senderId = -1,
                            senderName = "教师",
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            isFromTeacher = true,
                            senderAvatar = null // 教师头像暂时为null
                        )
                        // 添加广播消息到讨论区
                        addMessage(broadcastMessage)
                    }
                }
                "question_type" -> {
                    // 处理题型广播消息
                    val questionType = messageData["questionType"] as? String
                    if (questionType != null) {
                        // 创建题型广播消息对象
                        val questionMessage = Message(
                            id = "question_${System.currentTimeMillis()}",
                            senderId = -1,
                            senderName = "教师",
                            content = "【题型广播】当前讨论题型: $questionType",
                            timestamp = System.currentTimeMillis(),
                            isFromTeacher = true,
                            senderAvatar = null // 教师头像暂时为null
                        )
                        // 添加题型广播消息到讨论区
                        addMessage(questionMessage)
                    }
                }
                else -> {
                    Log.w("DiscussionActivity", "未知的消息类型: $type")
                }
            }
        } catch (e: Exception) {
            Log.e("DiscussionActivity", "处理广播消息失败: ${e.message}")
        }
    }

    /**
     * 为新消息添加动画效果
     * 为新添加的消息添加进入动画
     * @param position 消息在列表中的位置
     */
    private fun animateMessageAppearance(position: Int) {
        // 延迟执行动画，确保RecyclerView已经完成了布局
        messageHandler.postDelayed({
            val viewHolder = binding.rvMessages.findViewHolderForAdapterPosition(position)
            if (viewHolder != null) {
                // 设置初始透明度和Y轴位置
                viewHolder.itemView.alpha = 0f
                viewHolder.itemView.translationY = 20f
                // 执行淡入和上移动画
                viewHolder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }
        }, 100)
    }

    /**
     * 更新发送按钮状态
     * 根据输入框内容更新发送按钮的可用状态
     */
    private fun updateSendButtonState() {
        // 根据输入框是否为空来设置发送按钮的可用状态
        binding.ivSend.isEnabled = binding.etMessage.text?.isNotEmpty() == true
        binding.ivSend.alpha = if (binding.etMessage.text?.isNotEmpty() == true) 1f else 0.5f
    }

    /**
     * 更新在线人数
     * 更新UI上显示的在线人数，实际计算groupMembers中isOnline为true的成员数量
     */
    private fun updateOnlineCount() {
        // 实际计算在线人数：统计groupMembers中isOnline为true的成员数量
        val actualOnlineCount = groupMembers.count { it.isOnline }
        binding.tvOnlineCount.text = "在线 $actualOnlineCount 人"
    }
    
    /**
     * 更新成员在线状态
     * 根据用户ID更新指定成员的在线状态
     * @param memberId 成员ID
     * @param isOnline 是否在线
     */
    private fun updateMemberOnlineStatus(memberId: String, isOnline: Boolean) {
        val member = groupMembers.find { it.id == memberId }
        if (member != null && member.isOnline != isOnline) {
            member.isOnline = isOnline
            // 在线状态改变时更新显示
            updateOnlineCount()
        }
    }

    /**
     * 显示附件选项
     * 显示附件选择底部弹窗（图片、文件、拍照）
     */
    private fun showAttachmentOptions() {
        // 创建底部弹窗
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_attachments, null)

        // 设置图片选择点击事件
        view.findViewById<View>(R.id.optionImage).setOnClickListener {
            Toast.makeText(this, "选择图片", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 设置文件选择点击事件
        view.findViewById<View>(R.id.optionFile).setOnClickListener {
            Toast.makeText(this, "选择文件", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 设置拍照点击事件
        view.findViewById<View>(R.id.optionCamera).setOnClickListener {
            Toast.makeText(this, "拍照", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 显示弹窗
        dialog.setContentView(view)
        dialog.show()
    }

    /**
     * 显示更多选项
     * 显示更多选项底部弹窗（成员、清空、导出等）
     */
    private fun showMoreOptions() {
        // 创建底部弹窗
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_more_options, null)

        // 设置成员列表点击事件
        view.findViewById<View>(R.id.optionMembers).setOnClickListener {
            showGroupMembers()
            dialog.dismiss()
        }

        // 设置清空聊天记录点击事件
        view.findViewById<View>(R.id.optionClear).setOnClickListener {
            clearChatHistory()
            dialog.dismiss()
        }

        // 设置导出讨论点击事件
        view.findViewById<View>(R.id.optionExport).setOnClickListener {
            exportDiscussion()
            dialog.dismiss()
        }

        // 显示弹窗
        dialog.setContentView(view)
        dialog.show()
    }

    /**
     * 显示教师操作
     * 显示教师操作底部弹窗（添加任务、评价、删除全部小组）
     */
    private fun showTeacherActions() {
        // 创建底部弹窗
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_teacher_actions, null)

        // 设置添加任务点击事件
        view.findViewById<View>(R.id.optionAddTask).setOnClickListener {
            addGroupTask()
            dialog.dismiss()
        }

        // 设置评价小组点击事件
        view.findViewById<View>(R.id.optionEvaluate).setOnClickListener {
            evaluateGroup()
            dialog.dismiss()
        }

        // 设置删除全部小组点击事件
        view.findViewById<View>(R.id.optionDeleteAllGroups).setOnClickListener {
            deleteAllGroups()
            dialog.dismiss()
        }

        // 显示弹窗
        dialog.setContentView(view)
        dialog.show()
    }

    /**
     * 删除全部小组
     * 弹出确认对话框并调用删除全部小组接口
     */
    private fun deleteAllGroups() {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除当前学科的全部小组吗？此操作不可撤销！")
            .setPositiveButton("确定") { dialog, which ->
                executeDeleteAllGroups()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 执行删除全部小组操作
     * 调用API删除当前学科的全部小组
     */
    private fun executeDeleteAllGroups() {
        // 获取用户token
        val token = TokenManager.getToken() ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Token获取失败，无法执行删除操作", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载提示
        Toast.makeText(this, "正在删除全部小组...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // 调用删除全部小组接口
                val response = RetrofitClient.apiService.deleteAllGroups(
                    satoken = token,
                    subjectId = subjectId,
                    createdBy = currentUserId.toIntOrNull() ?: 0
                )

                if (response.code == 0) {
                    // 删除成功，显示提示并返回上一页
                    Toast.makeText(this@DiscussionActivity, "全部小组已成功删除", Toast.LENGTH_SHORT).show()
                    // 添加系统消息到聊天记录
                    addSystemMessage("全部小组已被删除")
                    // 返回上一页
                    finish()
                } else {
                    val errorMsg = "删除失败: ${response.message}" ?: "未知错误"
                    Toast.makeText(this@DiscussionActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("DiscussionActivity", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "删除异常: ${e.message ?: "未知异常"}"
                Toast.makeText(this@DiscussionActivity, errorMsg, Toast.LENGTH_SHORT).show()
                Log.e("DiscussionActivity", errorMsg, e)
            }
        }
    }

    // 小组人员列表
    private val groupMembers = mutableListOf<Member>()

    /**
     * 从UserInfo获取当前用户的头像信息
     */
    private fun loadCurrentUserAvatarFromUserInfo() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUserInfo().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 0) {
                            // 显式转换data为UserInfo类型
                            val userInfo: UserInfo? = body.data
                            if (userInfo != null) {
                                // 更新当前用户头像
                                currentUserAvatar = userInfo.avatarUrl
                                // 更新当前用户ID和名称
                                currentUserId = userInfo.userId.toString()
                                currentUserName = userInfo.userName
                                // 如果已经有消息，需要刷新适配器以显示正确的头像
                                if (::messagesAdapter.isInitialized) {
                                    messagesAdapter.notifyDataSetChanged()
                                }
                                Log.d("DiscussionActivity", "Successfully loaded current user avatar from UserInfo: ${userInfo.avatarUrl}")
                            }
                        } else {
                            Log.w("DiscussionActivity", "Failed to load user info: body is null or code is not 0")
                        }
                    } else {
                        Log.w("DiscussionActivity", "API request failed: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DiscussionActivity", "Exception when loading user info: ${e.message}")
                }
            }
        }
    }

    /**
     * 加载小组人员信息
     */
    private fun loadGroupMembers() {
        // 检查小组成员列表是否已从前面界面传递过来（如果不为空，则不需要再次加载）
        if (groupMembers.isNotEmpty()) {
            Log.d("DiscussionActivity", "小组成员信息已从前面界面获取，无需再次加载")
            return
        }

        // 获取用户token
        val saToken = TokenManager.getToken() ?: ""
        if (saToken.isEmpty()) {
            Log.e("DiscussionActivity", "未登录，无法加载小组人员信息")
            setupMockMembers()
            return
        }

        // 在协程中调用API获取小组人员信息
        lifecycleScope.launch {
            try {
                // 由于没有直接的API获取小组人员，我们通过加载所有小组数据来获取当前小组的成员
                val groupsResponse = RetrofitClient.apiService.getGroups(
                    satoken = saToken,
                    subjectId = 1, // 可以根据实际情况传入正确的Int值
                    createdBy = 6  // 可以根据实际情况传入正确的Int值
                )

                if (groupsResponse.code == 0 && groupsResponse.data != null) {
                    // 查找当前小组
                    val currentGroup = groupsResponse.data?.find { it.id == groupId.toInt() }
                    if (currentGroup != null) {
                        // 清空现有成员列表
                        groupMembers.clear()

                        // 添加真实的小组人员信息
                        currentGroup.students.forEachIndexed { index, student ->
                            if (student != null) {
                                groupMembers.add(Member(
                                    id = student.id.toString(),
                                    name = student.name,
                                    isOnline = false // 暂时设置为离线，实际可以根据WebSocket状态更新
                                ))
                            }
                        }

                        // 如果API没有返回成员信息，使用一些模拟数据确保UI能正常显示
                        if (groupMembers.isEmpty()) {
                            // 添加一些模拟数据
                            groupMembers.add(Member("1", "张三", true))
                            groupMembers.add(Member("2", "李四", false))
                            groupMembers.add(Member("3", "王五", true))
                        }
                    } else {
                        // 未找到当前小组，使用模拟数据
                        Log.e("DiscussionActivity", "未找到当前小组，使用模拟数据")
                        setupMockMembers()
                    }
                } else {
                    Log.e("DiscussionActivity", "加载小组数据失败: ${groupsResponse.message}")
                    // 使用模拟数据
                    setupMockMembers()
                }
            } catch (e: Exception) {
                Log.e("DiscussionActivity", "网络连接异常: ${e.message}")
                // 使用模拟数据
                setupMockMembers()
            }
        }
    }

    /**
     * 设置模拟成员数据
     */
    private fun setupMockMembers() {
        groupMembers.clear()
        groupMembers.add(Member("1", "张三", true))
        groupMembers.add(Member("2", "李四", false))
        groupMembers.add(Member("3", "王五", true))
    }

    /**
     * 显示小组成员
     * 显示小组成员列表对话框
     */
    private fun showGroupMembers() {
        // 如果成员列表为空，尝试加载成员信息
        if (groupMembers.isEmpty()) {
            loadGroupMembers()
            Toast.makeText(this, "正在加载小组人员信息...", Toast.LENGTH_SHORT).show()
            return
        }

        // 创建底部弹窗
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_group_members, null)

        // 动态添加成员项
        val membersContainer = view.findViewById<ViewGroup>(R.id.membersContainer)
        membersContainer.removeAllViews() // 清空容器
        groupMembers.forEach { member ->
            val memberView = layoutInflater.inflate(R.layout.item_member, membersContainer, false)
            memberView.findViewById<TextView>(R.id.tvMemberName).text = "${member.name} (${if (member.isOnline) "在线" else "离线"})"
            membersContainer.addView(memberView)
        }

        // 显示弹窗
        dialog.setContentView(view)
        dialog.show()
    }

    /**
     * 清空聊天记录
     * 清空聊天记录并显示系统提示
     */
    private fun clearChatHistory() {
        // 清空消息列表
        messages.clear()
        // 通知适配器数据变化
        messagesAdapter.notifyDataSetChanged()
        // 添加系统消息提示
        addSystemMessage("聊天记录已清空")
    }

    /**
     * 导出讨论内容
     * 模拟导出讨论内容
     */
    private fun exportDiscussion() {
        // 显示导出成功提示
        Toast.makeText(this, "讨论内容已导出", Toast.LENGTH_SHORT).show()
    }

    /**
     * 添加小组任务
     * 添加教师布置的小组任务消息
     */
    private fun addGroupTask() {
        // 创建任务消息
        val content = "【小组任务】请在本周内完成项目原型设计，并提交设计文档"
        
        // 通过API广播任务并保存到服务器（如果是教师模式）
        if (isTeacherMode) {
            lifecycleScope.launch { 
                try {
                    // 教师模式下使用特殊标识
                    val requestFromUserId = -1 // 假设-1代表教师ID
                    
                    // 先广播消息
                    val broadcastRequest = GroupChatApi.Broadcast.RequestBody(
                        teamId = listOf(groupId), // 转换为List<Int>
                        fromUserId = requestFromUserId,
                        content = content,
                        messageType = GroupChatApi.Broadcast.MessageType.TEXT
                    )
                    RetrofitClient.apiService.broadcastGroupChatMessage(
                        request = broadcastRequest
                    )
                    
                    // 再保存消息到服务器
                    val saveRequest = GroupChatApi.SaveMessage.RequestBody(
                        teamId = groupId,
                        fromUserId = requestFromUserId,
                        content = content,
                        messageType = GroupChatApi.Broadcast.MessageType.TEXT
                    )
                    
                    val saveResponse = RetrofitClient.apiService.saveGroupChatMessage(
                        request = saveRequest
                    )
                    
                    if (saveResponse.code == 0 && saveResponse.data != null) {
                        // 保存成功后，添加到本地消息列表
                        val taskMessage = Message(
                            id = "task_${System.currentTimeMillis()}",
                            senderId = -1,
                            senderName = "教师",
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.TEXT,
                            isFromTeacher = true
                        )
                        addMessage(taskMessage)
                    } else {
                        Log.d("DiscussionActivity", "任务消息保存失败: ${saveResponse.message}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 评价小组
     * 添加教师对小组的评价消息
     */
    private fun evaluateGroup() {
        // 创建评价消息内容
        val content = "【评价】小组讨论积极，分工明确，继续保持！"
        
        // 通过API保存评价消息到服务器（如果是教师模式）
        if (isTeacherMode) {
            lifecycleScope.launch { 
                try {
                    // 教师模式下使用特殊标识
                    val requestFromUserId = -1 // 假设-1代表教师ID
                    
                    // 保存消息到服务器
                    val saveRequest = GroupChatApi.SaveMessage.RequestBody(
                        teamId = groupId,
                        fromUserId = requestFromUserId,
                        content = content,
                        messageType = GroupChatApi.Broadcast.MessageType.TEXT
                    )
                    
                    val saveResponse = RetrofitClient.apiService.saveGroupChatMessage(
                        request = saveRequest
                    )
                    
                    if (saveResponse.code == 0 && saveResponse.data != null) {
                        // 保存成功后，添加到本地消息列表
                        val evaluationMessage = Message(
                            id = "eval_${System.currentTimeMillis()}",
                            senderId = -1,
                            senderName = "教师",
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.TEXT,
                            isFromTeacher = true,
                            senderAvatar = null
                        )
                        addMessage(evaluationMessage)
                    } else {
                        Log.d("DiscussionActivity", "评价消息保存失败: ${saveResponse.message}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 干预讨论
     * 添加教师对讨论的干预指导消息
     */
    private fun interveneDiscussion() {
        // 创建干预消息内容
        val content = "【指导建议】建议你们考虑一下用户的使用场景和需求痛点"
        
        // 通过API保存干预消息到服务器（如果是教师模式）
        if (isTeacherMode) {
            lifecycleScope.launch { 
                try {
                    // 教师模式下使用特殊标识
                    val requestFromUserId = -1 // 假设-1代表教师ID
                    
                    // 保存消息到服务器
                    val saveRequest = GroupChatApi.SaveMessage.RequestBody(
                        teamId = groupId,
                        fromUserId = requestFromUserId,
                        content = content,
                        messageType = GroupChatApi.Broadcast.MessageType.TEXT
                    )
                    
                    val saveResponse = RetrofitClient.apiService.saveGroupChatMessage(
                        request = saveRequest
                    )
                    
                    if (saveResponse.code == 0 && saveResponse.data != null) {
                        // 保存成功后，添加到本地消息列表
                        val interventionMessage = Message(
                            id = "intervene_${System.currentTimeMillis()}",
                            senderId = -1,
                            senderName = "教师",
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.TEXT,
                            isFromTeacher = true,
                            senderAvatar = null
                        )
                        addMessage(interventionMessage)
                    } else {
                        Log.d("DiscussionActivity", "干预消息保存失败: ${saveResponse.message}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Activity生命周期方法 - 销毁
     * 清理资源，取消任务，断开WebSocket连接
     */
    override fun onDestroy() {
        super.onDestroy()
        // 取消所有延迟任务
        messageHandler.removeCallbacksAndMessages(null)
        // 断开WebSocket连接
        disconnectWebSocket()
    }


    /**
     * 连接WebSocket
     * 建立与服务器的WebSocket连接以接收广播消息
     */
    private fun connectWebSocket() {
        // 防止重复连接
        if (isConnected) {
            Log.d("DiscussionActivity", "WebSocket已经连接，无需重复连接")
            return
        }
        
        // 获取最新的Token，不使用缓存的Token
        val tokenToUse = TokenManager.getToken() ?: ""
        
        if (tokenToUse.isEmpty()) {
            Log.e("DiscussionActivity", "Token为空，无法连接WebSocket")
            // 尝试刷新Token并重连
            scheduleReconnection()
            return
        }
        
        Log.d("DiscussionActivity", "尝试连接WebSocket，使用Token长度: ${tokenToUse.length}")
        
        try {
            // 创建OkHttpClient，增加连接超时和Ping/Pong配置
            val client = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // 0表示不超时
                .connectTimeout(10, TimeUnit.SECONDS)
                .pingInterval(30, TimeUnit.SECONDS) // 每30秒发送一次ping保持连接
                .build()
            
            // 确保URL格式正确 - 避免URL路径拼接错误导致404
            val wsUrlBuilder = StringBuilder(wsBaseUrl)
            // 确保URL末尾有斜杠
            if (!wsBaseUrl.endsWith("/")) {
                wsUrlBuilder.append("/")
            }
            // 添加groupId
            if (groupId!=0) {
                wsUrlBuilder.append(groupId)
            }
            
            val finalWsUrl = wsUrlBuilder.toString()
            Log.d("DiscussionActivity", "WebSocket连接URL: $finalWsUrl")
            
            val request = Request.Builder()
                .url(finalWsUrl)
                .addHeader("satoken", tokenToUse)
                .build()
            
            // 建立WebSocket连接
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    mainHandler.post {
                        isConnected = true
                        reconnectionAttempts = 0 // 重置重连尝试次数
                        Log.d("DiscussionActivity", "WebSocket连接成功: ${response.code}")
                    }
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    mainHandler.post {
                        Log.d("DiscussionActivity", "收到消息: $text")
                        try {
                            // 处理收到的广播消息
                            handleBroadcastMessage(text)
                        } catch (e: Exception) {
                            Log.e("DiscussionActivity", "处理消息异常: ${e.message}", e)
                            // 消息处理异常不应影响连接
                        }
                    }
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    mainHandler.post {
                        isConnected = false
                        Log.d("DiscussionActivity", "WebSocket连接关闭中: $code, $reason")
                    }
                }
                
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    mainHandler.post {
                        isConnected = false
                        Log.d("DiscussionActivity", "WebSocket连接已关闭: $code, $reason")
                        // 移除自动重连逻辑，关闭后不再重连
                    }
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                    mainHandler.post {
                        isConnected = false
                        val errorCode = response?.code ?: -1
                        Log.e("DiscussionActivity", "WebSocket连接失败: ${t.message}, 错误码: $errorCode", t)
                        
                        // 对于401未授权错误，强制刷新Token并重连
                        if (errorCode == 401) {
                            Log.d("DiscussionActivity", "401认证失败，强制获取新Token并重连")
                            // 清除initialToken，强制下次连接获取新Token
                            initialToken = null
                        }
                        
                        // 添加重连逻辑
                        scheduleReconnection()
                    }
                }
            })
            
            // 注意：不要在连接建立后立即关闭executorService，这会导致连接断开
            // client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e("DiscussionActivity", "WebSocket连接异常: ${e.message}", e)
            // 移除自动重连逻辑，异常后不再重连
        }
    }
    
    /**
     * 关闭WebSocket连接
     */
    private fun disconnectWebSocket() {
        if (webSocket != null) {
            webSocket?.close(1000, "主动关闭连接")
            webSocket = null
            isConnected = false
            reconnectionAttempts = 0 // 重置重连尝试次数
        }
    }
    
    /**
     * 安排重连
     */
    private fun scheduleReconnection() {
        if (reconnectionAttempts < maxReconnectionAttempts) {
            reconnectionAttempts++
            Log.d("DiscussionActivity", "计划重连WebSocket (尝试 $reconnectionAttempts/$maxReconnectionAttempts)")
            
            // 指数退避策略，避免短时间内频繁重连
            val backoffDelay = reconnectionDelay * (1L shl (reconnectionAttempts - 1)).coerceAtMost(60000L) // 最大60秒，使用1L确保位移操作返回Long类型
            
            mainHandler.postDelayed({
                if (!isConnected && !isFinishing) {
                    // 重新获取Token并尝试连接
                    connectWebSocket()
                }
            }, backoffDelay)
        } else {
            Log.e("DiscussionActivity", "已达到最大重连次数，停止重连")
            mainHandler.post {
                Toast.makeText(this@DiscussionActivity, "WebSocket连接失败，请检查网络或重新登录", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 加载历史消息
     * 从服务器获取历史消息记录
     */
    // 加载历史消息
    private fun loadHistoryMessages() {
        // 创建并显示加载进度条
        val progressBar = ProgressBar(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = layoutParams
        binding.root.addView(progressBar)
        binding.rvMessages.visibility = View.GONE

        // 获取用户token
        val saToken = TokenManager.getToken() ?: ""
        if (saToken.isEmpty()) {
            Toast.makeText(this, "未登录，请先登录", Toast.LENGTH_SHORT).show()
            binding.root.removeView(progressBar)
            binding.rvMessages.visibility = View.VISIBLE
            return
        }

        // 先加载小组人员信息，以便显示正确的发送者名称
        // 如果groupMembers为空才加载，避免重复加载
        if (groupMembers.isEmpty()) {
            loadGroupMembers()
        }

        // 在协程中调用API获取历史消息
        lifecycleScope.launch { 
            try {
                val response = RetrofitClient.apiService.getGroupChatHistory(
                    satoken = saToken,
                    teamId = groupId.toInt(),
                    pageNum = 1,
                    pageSize = 20
                )
                
                if (response.code == 0) {
                    // 清空现有消息
                    messages.clear()

                    // 添加系统欢迎消息
                    addSystemMessage("欢迎加入小组讨论！")

                    // 处理历史消息
                    response.data?.messages?.forEach { apiMessage ->
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val timestamp = sdf.parse(apiMessage.sendTime)?.time ?: System.currentTimeMillis()
                                
                            // 根据senderId查找对应的成员名称和头像
                            var senderName = "用户"
                            var senderAvatar: String? = null
                            var isTeacherMsg = false
                            
                            // 判断是否为教师消息（检查是否为-1）
                            if (apiMessage.fromUserId == -1 || apiMessage.fromUserId.toString() == "teacher") {
                                senderName = "教师"
                                isTeacherMsg = true
                            } else if (groupMembers.isNotEmpty()) {
                                // 查找成员列表中是否有对应的id
                                val member = groupMembers.find { it.id == apiMessage.fromUserId.toString() }
                                if (member != null) {
                                    senderName = member.name
                                    // 当前用户头像已在initData中保存
                                    if (apiMessage.fromUserId.toString() == currentUserId) {
                                        senderAvatar = currentUserAvatar
                                    }
                                }
                            }
                                
                            val message = Message(
                                id = apiMessage.id.toString(),
                                senderId = if (isTeacherMsg) -1 else apiMessage.fromUserId,
                                senderName = senderName,
                                content = apiMessage.content,
                                timestamp = timestamp,
                                messageType = if (apiMessage.messageType == GroupChatApi.Broadcast.MessageType.TEXT) MessageType.TEXT else MessageType.SYSTEM,
                                isFromTeacher = isTeacherMsg,
                                senderAvatar = senderAvatar
                            )
                            // 检查消息是否已存在，避免重复添加
                            val isMessageExists = messages.any { it.id == message.id }
                            if (!isMessageExists) {
                                messages.add(message)
                            }
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                    }
                    
                    // 按照时间戳对消息列表进行排序，确保最新的消息在列表末尾
                    messages.sortBy { it.timestamp }

                    // 更新RecyclerView
                    messagesAdapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                } else {
                    // 加载失败，显示提示
                    Toast.makeText(this@DiscussionActivity, "加载历史消息失败: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 网络异常
                e.printStackTrace()
                Toast.makeText(this@DiscussionActivity, "网络连接异常，请检查网络设置", Toast.LENGTH_SHORT).show()
            } finally {
                // 隐藏加载状态
                binding.root.removeView(progressBar)
                binding.rvMessages.visibility = View.VISIBLE
            }
        }
    }
}

/**
 * 消息适配器
 * 用于RecyclerView中显示不同类型的消息
 * @param messages 消息列表
 * @param currentUserId 当前用户ID
 * @param isTeacherMode 是否为教师模式
 */
class MessagesAdapter(
    private val messages: List<Message>,
    private val currentUserId: String,
    private val isTeacherMode: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 消息类型常量
    companion object {
        private const val TYPE_MY_MESSAGE = 0      // 我的消息
        private const val TYPE_OTHER_MESSAGE = 1   // 他人消息
        private const val TYPE_SYSTEM_MESSAGE = 2  // 系统消息
        private const val TYPE_TEACHER_MESSAGE = 3 // 教师消息
    }

    /**
     * 获取项目视图类型
     * 根据消息类型返回不同的视图类型
     * @param position 消息在列表中的位置
     * @return 视图类型
     */
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.messageType == MessageType.SYSTEM -> TYPE_SYSTEM_MESSAGE
            message.isFromTeacher -> TYPE_TEACHER_MESSAGE
            message.senderId.toString() == currentUserId -> TYPE_MY_MESSAGE
            else -> TYPE_OTHER_MESSAGE
        }
    }

    /**
     * 创建ViewHolder
     * 根据视图类型创建不同的ViewHolder
     * @param parent 父视图
     * @param viewType 视图类型
     * @return RecyclerView.ViewHolder 对象
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MY_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message, parent, false)
                MyMessageViewHolder(view)
            }
            TYPE_OTHER_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message, parent, false)
                OtherMessageViewHolder(view)
            }
            TYPE_SYSTEM_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message, parent, false)
                SystemMessageViewHolder(view)
            }
            TYPE_TEACHER_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message, parent, false)
                TeacherMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    /**
     * 绑定ViewHolder数据
     * 将消息数据绑定到对应的ViewHolder
     * @param holder ViewHolder对象
     * @param position 消息在列表中的位置
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is MyMessageViewHolder -> holder.bind(message)
            is OtherMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
            is TeacherMessageViewHolder -> holder.bind(message)
        }
    }

    /**
     * 获取消息总数
     * @return 消息总数
     */
    override fun getItemCount() = messages.size

    /**
     * 我的消息ViewHolder
     * 用于显示当前用户发送的消息
     */
    inner class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutMyMessage = itemView.findViewById<View>(R.id.layoutMyMessage)
        private val tvMyMessageContent = itemView.findViewById<TextView>(R.id.tvMyMessageContent)
        private val tvMyMessageTime = itemView.findViewById<TextView>(R.id.tvMyMessageTime)
        private val ivMyAvatar = itemView.findViewById<ImageView>(R.id.ivMyAvatar)
        private val tvMySender = itemView.findViewById<TextView>(R.id.tvMySender)

        init {
            // 设置视图可见性
            layoutMyMessage.visibility = View.VISIBLE
            itemView.findViewById<View>(R.id.layoutOtherMessage).visibility = View.GONE
            itemView.findViewById<View>(R.id.layoutSystemMessage).visibility = View.GONE
        }

        /**
         * 绑定消息数据
         * @param message 消息对象
         */
        fun bind(message: Message) {
            tvMyMessageContent.text = message.content
            tvMyMessageTime.text = formatTime(message.timestamp)
            // 设置发送者名字
            tvMySender.text = message.senderName
            
            // 设置头像
            if (!message.senderAvatar.isNullOrEmpty()) {
                // 使用Glide加载头像
                Glide.with(itemView.context).load(message.senderAvatar).into(ivMyAvatar)
            } else {
                // 如果没有头像URL，使用默认头像
                ivMyAvatar.setImageResource(R.drawable.ic_my_avatar)
            }
        }
    }

    /**
     * 他人消息ViewHolder
     * 用于显示其他成员发送的消息
     */
    inner class OtherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutOtherMessage = itemView.findViewById<View>(R.id.layoutOtherMessage)
        private val tvOtherSender = itemView.findViewById<TextView>(R.id.tvOtherSender)
        private val tvOtherMessageContent = itemView.findViewById<TextView>(R.id.tvOtherMessageContent)
        private val tvOtherMessageTime = itemView.findViewById<TextView>(R.id.tvOtherMessageTime)
        private val ivOtherAvatar = itemView.findViewById<ImageView>(R.id.ivOtherAvatar)

        init {
            // 设置视图可见性
            layoutOtherMessage.visibility = View.VISIBLE
            itemView.findViewById<View>(R.id.layoutMyMessage).visibility = View.GONE
            itemView.findViewById<View>(R.id.layoutSystemMessage).visibility = View.GONE
        }

        /**
         * 绑定消息数据
         * @param message 消息对象
         */
        fun bind(message: Message) {
            tvOtherSender.text = message.senderName
            tvOtherMessageContent.text = message.content
            tvOtherMessageTime.text = formatTime(message.timestamp)
            
            // 设置头像
            if (!message.senderAvatar.isNullOrEmpty()) {
                // 使用Glide加载头像
                Glide.with(itemView.context).load(message.senderAvatar).into(ivOtherAvatar)
            } else {
                // 如果没有头像URL，使用默认头像
                ivOtherAvatar.setImageResource(R.drawable.ic_other_avatar)
            }
        }
    }

    /**
     * 系统消息ViewHolder
     * 用于显示系统消息
     */
    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutSystemMessage = itemView.findViewById<View>(R.id.layoutSystemMessage)
        private val tvSystemMessage = itemView.findViewById<TextView>(R.id.tvSystemMessage)

        init {
            // 设置视图可见性
            layoutSystemMessage.visibility = View.VISIBLE
            itemView.findViewById<View>(R.id.layoutMyMessage).visibility = View.GONE
            itemView.findViewById<View>(R.id.layoutOtherMessage).visibility = View.GONE
        }

        /**
         * 绑定消息数据
         * @param message 消息对象
         */
        fun bind(message: Message) {
            tvSystemMessage.text = message.content
        }
    }

    /**
     * 教师消息ViewHolder
     * 用于显示教师发送的消息
     */
    inner class TeacherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutOtherMessage = itemView.findViewById<View>(R.id.layoutOtherMessage)
        private val tvOtherSender = itemView.findViewById<TextView>(R.id.tvOtherSender)
        private val tvOtherMessageContent = itemView.findViewById<TextView>(R.id.tvOtherMessageContent)
        private val tvOtherMessageTime = itemView.findViewById<TextView>(R.id.tvOtherMessageTime)
        private val ivOtherAvatar = itemView.findViewById<ImageView>(R.id.ivOtherAvatar)

        init {
            // 设置视图可见性
            layoutOtherMessage.visibility = View.VISIBLE
            itemView.findViewById<View>(R.id.layoutMyMessage).visibility = View.GONE
            itemView.findViewById<View>(R.id.layoutSystemMessage).visibility = View.GONE
        }

        /**
         * 绑定消息数据
         * @param message 消息对象
         */
        fun bind(message: Message) {
            tvOtherSender.text = "👨‍🏫 ${message.senderName}"
            tvOtherMessageContent.text = message.content
            tvOtherMessageTime.text = formatTime(message.timestamp)

            // 教师消息特殊样式
            tvOtherSender.setTextColor(ContextCompat.getColor(itemView.context, R.color.warning))
            
            // 尝试加载教师头像
            if (!message.senderAvatar.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(message.senderAvatar)
                    .circleCrop()
                    .into(ivOtherAvatar)
            } else {
                // 如果没有头像URL，使用默认头像
                ivOtherAvatar.setImageResource(R.drawable.ic_my_avatar)
            }
        }
    }

    /**
     * 格式化时间戳
     * 将时间戳转换为可读的时间格式
     * @param timestamp 时间戳
     * @return 格式化后的时间字符串
     */
    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}