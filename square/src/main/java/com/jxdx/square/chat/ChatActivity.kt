package com.jxdx.square.chat

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.google.gson.Gson
import com.jxdx.square.databinding.ActivityChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID

// 继承BaseActivity并指定ViewBinding类型
class ChatActivity : BaseActivity<ActivityChatBinding>() {
    private lateinit var wsManager: WebChatSocketManager

    // 配置参数
    private val saToken = TokenManager.getToken().toString() // 用户的Sa-Token
    private val wsBaseUrl = "ws://121.41.176.238:8080/single/chat/" // 基础WebSocket地址

    // WebSocket相关
    private val mainHandler = Handler(Looper.getMainLooper()) // 主线程更新UI
    private val gson = Gson()
    
    // 协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // API接口
    private val chatHistoryApi by lazy { HttpManager.instance.service(ChatHistoryApi::class.java) }

    // 聊天对象信息
    private var targetUserId: Int = 0 // 聊天对象的用户ID
    private var friendName: String = "" // 好友姓名
    private var friendId: String = "" // 好友ID（用于显示）

    // 初始化适配器
    private val chatAdapter by lazy { ChatAdapter() }

    // 消息列表
    private val messageList = mutableListOf<Message>()

    // 当前用户ID（从Token或其他地方获取）
    private val currentUserId = 6 // 根据API数据，当前用户是李四老师（ID=6）

    // 当前用户头像URL
    private val currentUserAvatar =
        "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/" +
            "20250905/Jr96/458X300/90.jpg"
    // 好友头像URL，将从Intent中获取
    private var friendAvatar = ""    // 默认头像URL，当没有传递头像URL时使用
    private val defaultAvatarUrl = "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/" +
            "20250905/Jr96/458X300/90.jpg"

    // 绑定布局文件
    override fun bindLayout(): ActivityChatBinding = ActivityChatBinding.inflate(layoutInflater)

    // 初始化视图
    override fun initView() {
        // 获取传递的参数
        intent?.let {
            friendName = it.getStringExtra("USER_NAME") ?: "未知用户"
            // 尝试获取Int类型的FRIEND_ID
            targetUserId = it.getIntExtra("FRIEND_ID", 0)
            friendId = targetUserId.toString()
            // 获取好友头像URL，如果为空则使用默认头像
            friendAvatar = it.getStringExtra("FRIEND_AVATAR") ?: defaultAvatarUrl
        }
        
        // 设置标题
        view.tvFriendName.text = friendName
        
        // 检查参数有效性
        if (targetUserId == 0) {
            Toast.makeText(this, "好友ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        android.util.Log.d("ChatActivity", "开始聊天 - 好友: $friendName, ID: $targetUserId")

        // 初始化标题栏
        view.tvFriendName.text = friendName
        view.ivBack.setOnClickListener { finish() }

        // 初始化消息列表
        initMessageList()
        // 加载历史记录
        loadChatHistory()
        // 初始禁用发送按钮
        view.ivSend.isEnabled = false
        
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
        
        // 自动连接WebSocket
        connect()
    }

    private fun disconnect() {
        wsManager.disconnect()
        view.ivSend.isEnabled = false
    }

    private fun connect() {
        // 检查Token是否有效
        if (TextUtils.isEmpty(saToken)) {
            Toast.makeText(this, "Sa-Token为空，请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        // 更新UI状态
        view.ivSend.isEnabled = false
        // 构建完整连接地址（基础地址 + 目标用户ID）
        val fullWsUrl = "$wsBaseUrl$targetUserId"
        android.util.Log.d("ChatActivity", "WebSocket连接地址: $fullWsUrl")

        // 设置WebSocket监听器
        wsManager.setWebSocketListener(
            object : WebSocketListener() {
                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response,
                ) {
                    super.onOpen(webSocket, response)
                    mainHandler.post {
                        view.ivSend.isEnabled = true
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
                            android.util.Log.d("ChatActivity", "收到消息: $text")
                            
                            // 尝试解析JSON格式的消息
                            try {
                                // 解析JSON消息
                                val jsonObject = gson.fromJson(text, com.google.gson.JsonObject::class.java)
                                val content = jsonObject.get("content")?.asString ?: ""
                                val senderId = jsonObject.get("senderId")?.asInt ?: 0
                                val messageId = jsonObject.get("messageId")?.asInt ?: 0
                                val timestamp = jsonObject.get("timestamp")?.asLong ?: System.currentTimeMillis()
                                
                                android.util.Log.d("ChatActivity", "解析JSON消息成功 - 发送者: $senderId, 内容: $content")
                                
                                // 创建接收消息对象
                                val receivedMsg = Message(
                                    id = messageId.toString(),
                                    content = content,
                                    type = Message.TYPE_RECEIVE,
                                    time = timestamp,
                                    senderId = senderId,
                                    receiverId = currentUserId,
                                    avatarUrl = friendAvatar,
                                )
                                
                                // 添加到消息列表并显示
                                messageList.add(receivedMsg)
                                updateMessageDisplay()
                                
                            } catch (jsonException: Exception) {
                                // 如果JSON解析失败，尝试解析简单文本格式
                                android.util.Log.d("ChatActivity", "JSON解析失败，尝试文本格式解析")
                                val parts = text.split(" ", limit = 2)
                                if (parts.size >= 2) {
                                    val senderId = parts[0].toIntOrNull() ?: 0
                                    val content = parts[1]
                                    
                                    // 创建接收消息对象
                                    val receivedMsg = Message(
                                        id = UUID.randomUUID().toString(),
                                        content = content,
                                        type = Message.TYPE_RECEIVE,
                                        time = System.currentTimeMillis(),
                                        senderId = senderId,
                                        receiverId = currentUserId,
                                        avatarUrl = friendAvatar,
                                    )
                                    
                                    // 添加到消息列表并显示
                                    messageList.add(receivedMsg)
                                    updateMessageDisplay()
                                } else {
                                    android.util.Log.w("ChatActivity", "消息格式不正确: $text")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ChatActivity", "解析消息失败", e)
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
                        view.ivSend.isEnabled = false
                    }
                }

                override fun onClosed(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    super.onClosed(webSocket, code, reason)
                    mainHandler.post {
                        view.ivSend.isEnabled = false
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

        // 根据WebSocket接口格式发送消息
        // 格式："{currentUserId} {content}"
        val messageText = "$currentUserId $content"


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
        val success = wsManager.sendTextMessage(messageText)
        if (!success) {
            Toast.makeText(this, "消息发送失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
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

        // 历史记录将通过loadChatHistory()方法加载
    }

    // 加载聊天历史记录
    private fun loadChatHistory() {
        coroutineScope.launch {
            try {
                android.util.Log.d("ChatActivity", "开始加载聊天历史记录，目标用户ID: $targetUserId")
                
                val response = withContext(Dispatchers.IO) {
                    chatHistoryApi.getChatHistory(otherUserId = targetUserId, pageNum = 1, pageSize = 10)
                }
                
                android.util.Log.d("ChatActivity", "API响应: code=${response.code}, message=${response.message}, data=${response.data}")
                
                if (response.code == 0 && response.data != null) {
                    val historyResponse = response.data!!
                    val historyMessages = historyResponse.messages
                    android.util.Log.d("ChatActivity", "成功加载历史记录，共 ${historyMessages.size} 条消息")
                    
                    // 转换为Message对象并添加到列表
                    val convertedMessages = historyMessages.map { historyMessage ->
                        val message = Message.fromHistoryMessage(historyMessage)
                        android.util.Log.d("ChatActivity", "转换消息: content=${message.content}, type=${message.type}, senderId=${message.senderId}")
                        message
                    }
                    
                    // 按时间排序（从旧到新）
                    val sortedMessages = convertedMessages.sortedBy { it.time }
                    
                    // 清空现有消息并添加历史记录
                    messageList.clear()
                    messageList.addAll(sortedMessages)
                    
                    android.util.Log.d("ChatActivity", "历史记录加载完成，共 ${messageList.size} 条消息")
                    for (i in messageList.indices) {
                        val msg = messageList[i]
                        android.util.Log.d("ChatActivity", "历史消息[$i]: content=${msg.content}, type=${msg.type}, senderId=${msg.senderId}")
                    }
                    
                    // 更新显示
                    updateMessageDisplay()
                    
                    android.util.Log.d("ChatActivity", "历史记录加载完成，共显示 ${messageList.size} 条消息")
                } else {
                    android.util.Log.w("ChatActivity", "加载历史记录失败: ${response.message}")
                    Toast.makeText(this@ChatActivity, "加载历史记录失败: ${response.message}", Toast.LENGTH_SHORT).show()
                    // 不添加模拟消息，保持消息列表为空
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatActivity", "加载历史记录异常", e)
                Toast.makeText(this@ChatActivity, "加载历史记录失败: ${e.message}", Toast.LENGTH_SHORT).show()
                // 不添加模拟消息，保持消息列表为空
            }
        }
    }
    
    // 移除模拟消息方法，现在只使用真实的历史记录

    // 更新消息显示
    private fun updateMessageDisplay() {
        android.util.Log.d("ChatActivity", "updateMessageDisplay: 消息列表大小=${messageList.size}")
        chatAdapter.clearAndAdd(messageList)
        android.util.Log.d("ChatActivity", "适配器更新完成，适配器数据大小=${chatAdapter.itemCount}")
        // 滚动到底部
        scrollToBottom()
    }

    // 滚动到最新消息
    private fun scrollToBottom() {
        if (messageList.isNotEmpty()) {
            view.rvChatMessages.scrollToPosition(messageList.size - 1)
        }
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
