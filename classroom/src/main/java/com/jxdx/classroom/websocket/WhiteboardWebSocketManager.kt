package com.jxdx.classroom.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jxdx.classroom.model.Message
import com.jxdx.classroom.model.MessageType
import com.jxdx.classroom.model.SenderType
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * 后端返回的WebSocket消息格式
 */
data class WebSocketResponse(
    val message: String,  // 内层JSON字符串
    val timestamp: Long
)

/**
 * 白板WebSocket连接管理器
 * 负责与后端WebSocket服务进行实时通信
 */
class WhiteboardWebSocketManager {
    
    private val TAG = "WhiteboardWebSocketManager"
    
    // WebSocket相关
    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null
    private var isConnected = false
    
    // 回调接口
    private var messageListener: MessageListener? = null
    private var connectionListener: ConnectionListener? = null
    
    // 房间信息
    private var roomId: String = ""
    private var identity: Int = 0 // 0=学生，1=老师
    
    // Gson用于JSON解析
    private val gson = Gson()
    
    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 消息监听器
     */
    interface MessageListener {
        fun onMessageReceived(message: Message)
        fun onWhiteboardSnapshotReceived(imageUrl: String, senderId: String, senderName: String)
        fun onError(error: String)
    }
    
    /**
     * 连接状态监听器
     */
    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionError(error: String)
    }
    
    /**
     * 初始化WebSocket客户端
     */
    private fun initClient() {
        client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * 连接到WebSocket服务器
     * @param roomId 房间ID
     * @param identity 身份标识（0=学生，1=老师）
     */
    fun connect(roomId: String, identity: Int) {
        this.roomId = roomId
        this.identity = identity
        
        Log.d(TAG, "=== WebSocket连接开始 ===")
        Log.d(TAG, "房间ID: $roomId")
        Log.d(TAG, "身份标识: $identity (${if(identity == 0) "学生" else "老师"})")
        
        if (isConnected) {
            Log.w(TAG, "WebSocket已连接，无需重复连接")
            return
        }
        
        initClient()
        
        // 构建WebSocket URL
        val url = "ws://121.41.176.238:8080/live/room?roomId=$roomId&identity=$identity"
        Log.d(TAG, "正在连接WebSocket: $url")
        
        // 获取Token
        val token = com.example.corekit.http.TokenManager.getToken()
        Log.d(TAG, "Token: ${if (token.isNullOrEmpty()) "空" else "已设置"}")
        
        val request = Request.Builder()
            .url(url)
            .addHeader("satoken", token.toString())
            .build()
        
        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket连接成功")
                isConnected = true
                connectionListener?.onConnected()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "收到WebSocket消息: $text")
                handleMessage(text)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket正在关闭: $code - $reason")
                isConnected = false
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket连接已关闭: $code - $reason")
                isConnected = false
                connectionListener?.onDisconnected()
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "=== WebSocket连接失败 ===")
                Log.e(TAG, "错误信息: ${t.message}")
                Log.e(TAG, "错误类型: ${t.javaClass.simpleName}")
                Log.e(TAG, "响应状态: ${response?.code}")
                Log.e(TAG, "响应消息: ${response?.message}")
                Log.e(TAG, "连接URL: ws://121.41.176.238:8080/live/room?roomId=$roomId&identity=$identity")
                isConnected = false
                connectionListener?.onConnectionError("连接失败: ${t.message}")
            }
        })
    }
    
    /**
     * 处理接收到的消息
     */
    private fun handleMessage(text: String) {
        Log.d(TAG, "🟢🟢🟢 === 收到WebSocket消息 ===")
        Log.d(TAG, "🟢🟢🟢 原始消息: $text")
        Log.d(TAG, "🟢🟢🟢 消息长度: ${text.length}")
        
        try {
            // 第一步：解析外层JSON结构
            Log.d(TAG, "🟡🟡🟡 开始解析外层JSON...")
            val response = gson.fromJson(text, WebSocketResponse::class.java)
            Log.d(TAG, "🟡🟡🟡 外层JSON解析成功")
            Log.d(TAG, "外层时间戳: ${response.timestamp}")
            Log.d(TAG, "内层消息字符串: ${response.message}")
            
            // 第二步：解析内层JSON消息
            Log.d(TAG, "🟡🟡🟡 开始解析内层JSON...")
            val message = gson.fromJson(response.message, Message::class.java)
            Log.d(TAG, "🟡🟡🟡 === 内层JSON解析成功 ===")
            Log.d(TAG, "解析后的Message对象: $message")
            Log.d(TAG, "消息ID: ${message.id}")
            Log.d(TAG, "消息内容: ${message.content}")
            Log.d(TAG, "发送者ID: ${message.senderId}")
            Log.d(TAG, "发送者姓名: ${message.senderName}")
            Log.d(TAG, "发送者类型: ${message.senderType}")
            Log.d(TAG, "消息类型: ${message.messageType}")
            Log.d(TAG, "图片URL: ${message.imageUrl}")
            Log.d(TAG, "时间戳: ${message.timestamp}")
            Log.d(TAG, "是否已读: ${message.isRead}")
            
            messageListener?.onMessageReceived(message)
            
            // 如果是白板快照消息，特殊处理
            if (message.messageType == MessageType.WHITEBOARD && message.imageUrl != null) {
                Log.d(TAG, "检测到白板快照消息 - 图片URL: ${message.imageUrl}")
                messageListener?.onWhiteboardSnapshotReceived(message.imageUrl, message.senderId, message.senderName)
            } else {
                Log.d(TAG, "非白板快照消息或缺少图片URL")
                Log.d(TAG, "消息类型: ${message.messageType}")
                Log.d(TAG, "图片URL: ${message.imageUrl}")
            }
            
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "🔴🔴🔴 === JSON解析失败 ===")
            Log.e(TAG, "🔴🔴🔴 错误信息: ${e.message}")
            Log.e(TAG, "🔴🔴🔴 原始消息内容: $text")
            messageListener?.onError("消息格式错误: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "=== 处理消息失败 ===")
            Log.e(TAG, "错误信息: ${e.message}")
            Log.e(TAG, "错误类型: ${e.javaClass.simpleName}")
            messageListener?.onError("处理消息失败: ${e.message}")
        }
    }
    
    /**
     * 发送消息
     */
    fun sendMessage(message: Message) {
        if (!isConnected) {
            Log.w(TAG, "WebSocket未连接，无法发送消息")
            messageListener?.onError("连接已断开")
            return
        }
        
        try {
            val jsonMessage = gson.toJson(message)
            Log.d(TAG, "=== 发送JSON消息 ===")
            Log.d(TAG, "JSON内容: $jsonMessage")
            Log.d(TAG, "JSON长度: ${jsonMessage.length}")
            webSocket?.send(jsonMessage)
            Log.d(TAG, "消息已发送到WebSocket服务器")
        } catch (e: Exception) {
            Log.e(TAG, "发送消息失败: ${e.message}")
            messageListener?.onError("发送消息失败")
        }
    }
    
    /**
     * 发送白板快照
     */
    fun sendWhiteboardSnapshot(imageUrl: String, senderId: String, senderName: String) {
        val message = Message(
            id = System.currentTimeMillis().toString(),
            content = "白板快照",
            senderId = senderId,
            senderName = senderName,
            senderType = if (identity == 0) SenderType.STUDENT else SenderType.TEACHER,
            messageType = MessageType.WHITEBOARD,
            timestamp = System.currentTimeMillis(),
            imageUrl = imageUrl
        )
        
        Log.d(TAG, "=== 发送白板快照消息 ===")
        Log.d(TAG, "消息ID: ${message.id}")
        Log.d(TAG, "发送者ID: ${message.senderId}")
        Log.d(TAG, "发送者姓名: ${message.senderName}")
        Log.d(TAG, "发送者类型: ${message.senderType}")
        Log.d(TAG, "消息类型: ${message.messageType}")
        Log.d(TAG, "图片URL: ${message.imageUrl}")
        
        sendMessage(message)
    }
    
    /**
     * 发送文本消息
     */
    fun sendTextMessage(content: String, senderId: String, senderName: String) {
        val message = Message(
            id = System.currentTimeMillis().toString(),
            content = content,
            senderId = senderId,
            senderName = senderName,
            senderType = if (identity == 0) SenderType.STUDENT else SenderType.TEACHER,
            messageType = MessageType.TEXT,
            timestamp = System.currentTimeMillis()
        )
        sendMessage(message)
    }
    
    /**
     * 设置消息监听器
     */
    fun setMessageListener(listener: MessageListener) {
        this.messageListener = listener
    }
    
    /**
     * 设置连接监听器
     */
    fun setConnectionListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        Log.d(TAG, "主动断开WebSocket连接")
        webSocket?.close(1000, "主动断开")
        webSocket = null
        isConnected = false
    }
    
    /**
     * 检查连接状态
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * 获取当前房间ID
     */
    fun getRoomId(): String = roomId
    
    /**
     * 获取当前身份
     */
    fun getIdentity(): Int = identity
    
    /**
     * 释放资源
     */
    fun release() {
        disconnect()
        scope.cancel()
        client = null
    }
}
