package com.jxdx.resource.famousChat

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import kotlin.getValue

class FamousChatViewModel(application: Application) : BaseViewModel(application) {
    private val repository: FamousChatRepository by lazy {
        Log.d("ChatViewModel", "Initializing ChatRepository")
        FamousChatRepository()
    }

    val chatSessionLiveData: ResLiveData<ChatSession> by lazy {
        Log.d("ChatViewModel", "Initializing chatSessionLiveData")
        ResLiveData()
    }

    val sendMessageLiveData: ResLiveData<ChatResponse> by lazy {
        Log.d("ChatViewModel", "Initializing sendMessageLiveData")
        ResLiveData()
    }

    val messageList = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())

    fun initChatSession(celebrityId: Int) {
        Log.d("ChatViewModel", "initChatSession called with celebrityId=$celebrityId")

        request(
            chatSessionLiveData,
            object : LiveDataCallback<ChatSession, ChatSession> {
                override fun success(
                    emit: ResLiveData<ChatSession>,
                    msg: String?,
                    data: ChatSession?
                ) {
                    Log.d("ChatViewModel", "初始化聊天会话成功: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ChatSession>,
                    code: Int?,
                    msg: String?,
                    data: ChatSession?
                ) {
                    Log.w(
                        "ChatViewModel",
                        "初始化聊天会话其他代码: code=$code, msg=$msg, data=$data"
                    )
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<ChatSession>,
                    e: ErrorResponse
                ) {
                    Log.e("ChatViewModel", "初始化聊天会话错误: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ChatViewModel", "Executing repository call for initChatSession")
            repository.initChatSession(celebrityId)
        }
    }

    fun sendMessage(sessionId: String, message: String, celebrityId: Int) {
        Log.d("ChatViewModel", "sendMessage called with sessionId=$sessionId, message=$message")

        // 先添加用户消息到列表
        val userMessage = ChatMessage(
            messageId = "user_${System.currentTimeMillis()}",
            content = message,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        addMessage(userMessage)

        request(
            sendMessageLiveData,
            object : LiveDataCallback<ChatResponse, ChatResponse> {
                override fun success(
                    emit: ResLiveData<ChatResponse>,
                    msg: String?,
                    data: ChatResponse?
                ) {
                    Log.d("ChatViewModel", "发送消息成功: $msg, data: $data")
                    data?.let { response ->
                        // 添加AI回复到列表
                        val aiMessage = ChatMessage(
                            messageId = "ai_${System.currentTimeMillis()}",
                            content = response.data.content,
                            isUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                        addMessage(aiMessage)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ChatResponse>,
                    code: Int?,
                    msg: String?,
                    data: ChatResponse?
                ) {
                    Log.w("ChatViewModel", "发送消息其他代码: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<ChatResponse>,
                    e: ErrorResponse
                ) {
                    Log.e("ChatViewModel", "发送消息错误: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ChatViewModel", "Executing repository call for sendMessage")
            repository.sendMessage(sessionId, message, celebrityId)
        }
    }

    fun addMessage(message: ChatMessage) {
        val currentList = messageList.value ?: mutableListOf()
        currentList.add(message)
        messageList.postValue(currentList)
    }
}