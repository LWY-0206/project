package com.example.loding.chat

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.corekit.common.BaseActivity
import com.example.loding.databinding.ActivityChatBinding
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID

// 继承BaseActivity并指定ViewBinding类型
class ChatActivity : BaseActivity<ActivityChatBinding>() {
    private lateinit var wsManager: WebChatSocketManager

    // 配置参数
    private val saToken = "1bde9e31-402b-4bb9-8179-3fdaf9479acb" // 用户的Sa-Token
    private val targetUserId = 1 // 聊天对象的用户ID
    private val wsBaseUrl = "ws://121.41.176.238:8080/single/chat/" // 基础WebSocket地址

    // WebSocket相关
    private val mainHandler = Handler(Looper.getMainLooper()) // 主线程更新UI
    private val gson = Gson()

    // 初始化ViewModel
    private var friendId: String = ""
    private var friendName: String = ""

    // 初始化适配器
    private val chatAdapter by lazy { ChatAdapter() }

    // 消息列表
    private val messageList = mutableListOf<Message>()

    // 模拟用户ID
    private val currentUserId = 2

    // 模拟头像URL
    private val currentUserAvatar =
        "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/" +
            "20250905/Jr96/458X300/90.jpg"
    private val friendAvatar = "https://example.com/avatar2.png"

    // 绑定布局文件
    override fun bindLayout(): ActivityChatBinding = ActivityChatBinding.inflate(layoutInflater)

    // 初始化视图
    override fun initView() {
        // 获取传递的好友信息
        val intent = intent
        friendId = intent.getStringExtra("friend_id") ?: ""
        friendName = intent.getStringExtra("friend_name") ?: "好友"

        // 初始化标题栏
        view.tvFriendName.text = friendName
        view.tvOnlineStatus.text = "连接状态：未连接"
        view.tvOnlineStatus.setTextColor(Color.GRAY)
        view.ivBack.setOnClickListener { finish() }

        // 初始化消息列表
        initMessageList()
        // 初始禁用发送按钮
        view.ivSend.isEnabled = false
        // 连接按钮点击事件
        view.btnTest.setOnClickListener {
            if (wsManager.isConnected) {
                disconnect()
            } else {
                connect()
            }
        }
        // 发送按钮点击事件
        view.ivSend.setOnClickListener {
            val msg =
                view.etMessage.text
                    .toString()
                    .trim()
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendMessage(msg)
        }
    }

    private fun disconnect() {
        wsManager.disconnect()
        updateConnectionStatus("连接状态：已断开", Color.GRAY)
        view.ivSend.isEnabled = false
        view.btnTest.text = "连接"
    }

    private fun connect() {
        // 检查Token是否有效
        if (TextUtils.isEmpty(saToken)) {
            Toast.makeText(this, "Sa-Token为空，请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        // 更新UI状态
        updateConnectionStatus("连接状态：正在连接...", Color.BLUE)
        view.ivSend.isEnabled = false
        // 构建完整连接地址（基础地址 + 目标用户ID）
        val fullWsUrl = "$wsBaseUrl$targetUserId"

        // 设置WebSocket监听器
        wsManager.setWebSocketListener(
            object : WebSocketListener() {
                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response,
                ) {
                    super.onOpen(webSocket, response)
                    mainHandler.post {
                        updateConnectionStatus("连接状态：已连接（与用户 $targetUserId 聊天中）", Color.GREEN)
                        view.ivSend.isEnabled = true
                        view.btnTest.text = "断开连接"
                    }
                }

                // 接收消息（JSON格式）
                override fun onMessage(
                    webSocket: WebSocket,
                    text: String,
                ) {
                    super.onMessage(webSocket, text)
                    mainHandler.post {
                        try {
                            // 解析JSON为Message对象
                            val receivedMsg = gson.fromJson(text, Message::class.java)
                            // 标记为接收类型（本地显示用）
                            val displayMsg =
                                receivedMsg.copy(
                                    type = Message.TYPE_RECEIVE,
                                    avatarUrl = friendAvatar,
                                )
                            // 添加到消息列表并显示
                            messageList.add(displayMsg)
                            updateMessageDisplay()
                        } catch (e: Exception) {
                            Toast.makeText(this@ChatActivity, "解析消息失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?,
                ) {
                    super.onFailure(webSocket, t, response)
                    mainHandler.post {
                        var errorMsg = "连接失败："
                        errorMsg += t.message ?: "未知错误"
                        response?.let {
                            errorMsg += "，响应码：${it.code}"
                            if (it.code == 401) {
                                errorMsg += "（Token无效或已过期）"
                            }
                        }
                        updateConnectionStatus(errorMsg, Color.RED)
                        view.ivSend.isEnabled = false
                        view.btnTest.text = "连接"
                    }
                }

                override fun onClosed(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    super.onClosed(webSocket, code, reason)
                    mainHandler.post {
                        updateConnectionStatus("连接状态：已关闭（原因：$reason）", Color.GRAY)
                        view.ivSend.isEnabled = false
                        view.btnTest.text = "连接"
                    }
                }
            },
        )

        // 建立连接
        wsManager.connect(fullWsUrl, saToken)
    }

    private fun sendMessage(content: String) {
        if (!wsManager.isConnected) {
            Toast.makeText(this, "未连接，请先建立连接", Toast.LENGTH_SHORT).show()
            return
        }

        // 创建消息ID（使用UUID确保唯一性）
        val messageId = UUID.randomUUID().toString()

        // 创建本地消息对象（发送类型）
        val sendMsg =
            Message(
                id = messageId,
                time = System.currentTimeMillis(),
                avatarUrl = currentUserAvatar,
                content = content,
                type = Message.TYPE_SEND,
                senderId = currentUserId,
                receiverId = targetUserId,
            )

        // 先添加到本地列表（优化体验）
        messageList.add(sendMsg)
        updateMessageDisplay()
        view.etMessage.setText("")

        // 通过WebSocket发送消息
        val success = wsManager.sendMessage(sendMsg)
        if (!success) {
            Toast.makeText(this, "消息发送失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
        // 清空输入框
        view.etMessage.setText("")
    }

    // 初始化消息列表
    private fun initMessageList() {
        // 初始化WebSocket管理器
        wsManager = WebChatSocketManager.getInstance()
        // 设置RecyclerView
        view.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = chatAdapter
            // 禁用嵌套滚动
            isNestedScrollingEnabled = false
            // 添加滚动监听器，处理自动滚动逻辑
            addOnScrollListener(
                object : OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                    ) {
                        super.onScrollStateChanged(recyclerView, newState)
                        // 自动滚动到底部逻辑可以在这里实现
                    }
                },
            )
        }

        // 添加一些模拟消息
        addMockMessages()
    }

    // 添加模拟消息
    private fun addMockMessages() {
        val mockMessages =
            listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    content = "你好！",
                    type = Message.TYPE_RECEIVE,
                    time = System.currentTimeMillis() - 3600000,
                    senderId = targetUserId,
                    receiverId = currentUserId,
                    avatarUrl = friendAvatar,
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    content = "你好，最近过得怎么样？",
                    type = Message.TYPE_SEND,
                    time = System.currentTimeMillis() - 3500000,
                    senderId = currentUserId,
                    receiverId = targetUserId,
                    avatarUrl = currentUserAvatar,
                ),
            )
        messageList.addAll(mockMessages)
        updateMessageDisplay()
    }

    // 更新消息显示
    private fun updateMessageDisplay() {
        chatAdapter.clearAndAdd(messageList)
        // 滚动到底部
        scrollToBottom()
    }

    // 滚动到最新消息
    private fun scrollToBottom() {
        if (messageList.isNotEmpty()) {
            view.rvChatMessages.scrollToPosition(messageList.size - 1)
        }
    }

    // 更新连接状态
    private fun updateConnectionStatus(
        status: String,
        color: Int,
    ) {
        view.tvOnlineStatus.text = status
        view.tvOnlineStatus.setTextColor(color)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect() // 页面销毁时断开连接
        mainHandler.removeCallbacksAndMessages(null)
    }

    // 订阅UI数据
    override fun subscribeUi() {
        // 这里可以添加数据观察逻辑
    }
}
