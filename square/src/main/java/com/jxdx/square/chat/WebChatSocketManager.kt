package com.jxdx.square.chat

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

/**
 * WebSocket管理器 - Kotlin封装版
 * 用于处理聊天界面的WebSocket通信
 */
class WebChatSocketManager {
    private lateinit var client: OkHttpClient
    private var webSocket: WebSocket? = null
    private var wsUrl: String? = null
    private var listener: WebSocketListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()
    private var isConnecting = false

    companion object {
        @Volatile
        private var instance: WebChatSocketManager? = null

        fun getInstance(): WebChatSocketManager {
            if (instance == null) {
                synchronized(WebChatSocketManager::class.java) {
                    if (instance == null) {
                        instance = WebChatSocketManager()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        initOkHttpClient()
    }

    private fun initOkHttpClient() {
        client =
            OkHttpClient
                .Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
    }

    fun setWebSocketListener(listener: WebSocketListener) {
        this.listener = listener
    }

    /**
     * 连接WebSocket
     * @param url WebSocket服务器地址
     * @param token 用户认证Token
     */
    fun connect(
        url: String,
        token: String = "",
    ) {
        if (isConnecting) return

        wsUrl = url
        isConnecting = true

        val requestBuilder =
            Request
                .Builder()
                .url(url)

        // 如果有token，添加到请求头
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("satoken", token)
        }

        val request = requestBuilder.build()

        webSocket =
            client.newWebSocket(
                request,
                object : WebSocketListener() {
                    override fun onOpen(
                        webSocket: WebSocket,
                        response: Response,
                    ) {
                        super.onOpen(webSocket, response)
                        isConnecting = false
                        mainHandler.post {
                            listener?.onOpen(webSocket, response)
                        }
                    }

                    override fun onMessage(
                        webSocket: WebSocket,
                        text: String,
                    ) {
                        super.onMessage(webSocket, text)
                        mainHandler.post {
                            listener?.onMessage(webSocket, text)
                        }
                    }

                    override fun onMessage(
                        webSocket: WebSocket,
                        bytes: ByteString,
                    ) {
                        super.onMessage(webSocket, bytes)
                        mainHandler.post {
                            listener?.onMessage(webSocket, bytes)
                        }
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?,
                    ) {
                        super.onFailure(webSocket, t, response)
                        isConnecting = false
                        mainHandler.post {
                            listener?.onFailure(webSocket, t, response)
                        }
                        // 自动重连
                        reconnect()
                    }

                    override fun onClosed(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String,
                    ) {
                        super.onClosed(webSocket, code, reason)
                        isConnecting = false
                        mainHandler.post {
                            listener?.onClosed(webSocket, code, reason)
                        }
                    }
                },
            )
    }

    /**
     * 发送消息
     * @param message 消息对象
     * @return 是否发送成功
     */
    fun sendMessage(message: Message): Boolean {
        if (webSocket == null) return false

        // 将消息对象转换为JSON字符串
        val jsonMessage = gson.toJson(message)
        return webSocket!!.send(jsonMessage)
    }

    /**
     * 发送文本消息
     * @param text 文本内容
     * @return 是否发送成功
     */
    fun sendTextMessage(text: String): Boolean {
        if (webSocket == null) return false
        return webSocket!!.send(text)
    }

    /**
     * 断开连接
     */
    fun disconnect(
        code: Int = 1000,
        reason: String = "用户主动断开连接",
    ) {
        webSocket?.close(code, reason)
        webSocket = null
    }

    /**
     * 是否连接
     */
    val isConnected: Boolean
        get() = webSocket != null

    /**
     * 重连机制
     */
    private fun reconnect() {
        // 避免频繁重连
        Thread {
            try {
                Thread.sleep(3000) // 3秒后重试
                if (wsUrl != null && !isConnecting) {
                    connect(wsUrl!!)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    }
}
