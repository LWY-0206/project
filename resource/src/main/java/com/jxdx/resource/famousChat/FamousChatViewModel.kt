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

    val chatSessionLiveData: ResLiveData<ChatContent> by lazy {
        Log.d("ChatViewModel", "Initializing chatSessionLiveData")
        ResLiveData()
    }

    val sendMessageLiveData: ResLiveData<ChatContent> by lazy {
        Log.d("ChatViewModel", "Initializing sendMessageLiveData")
        ResLiveData()
    }

    val messageList = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())

    fun initChatSession(celebrityId: Int) {
        Log.d("ChatViewModel", "initChatSession called with celebrityId=$celebrityId")

        request(
            chatSessionLiveData,
            object : LiveDataCallback<ChatContent, ChatContent> {
                override fun success(
                    emit: ResLiveData<ChatContent>,
                    msg: String?,
                    data: ChatContent?
                ) {
                    Log.d("ChatViewModel", "初始化聊天会话成功: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ChatContent>,
                    code: Int?,
                    msg: String?,
                    data: ChatContent?
                ) {
                    Log.w(
                        "ChatViewModel",
                        "初始化聊天会话其他代码: code=$code, msg=$msg, data=$data"
                    )
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<ChatContent>,
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

    fun sendMessage( message: String, celebrityId: Int) {

        Log.d("ChatViewModel", "sendMessage called with message=$message")

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
            object : LiveDataCallback<ChatContent, ChatContent> {
                override fun success(
                    emit: ResLiveData<ChatContent>,
                    msg: String?,
                    data: ChatContent?
                ) {
                    Log.d("ChatViewModel", "发送消息成功，获取到返回：$msg, data: $data")
                    data?.let { response ->
                        // 添加AI回复到列表
                        val aiMessage = ChatMessage(
                            messageId = "ai_${System.currentTimeMillis()}",
                            content = response.content,
                            isUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                        addMessage(aiMessage)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ChatContent>,
                    code: Int?,
                    msg: String?,
                    data: ChatContent?
                ) {
                    Log.w("ChatViewModel", "发送消息其他代码: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<ChatContent>,
                    e: ErrorResponse
                ) {
                    Log.e("ChatViewModel", "发送消息错误: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ChatViewModel", "Executing repository call for sendMessage")
            repository.sendMessage(message, celebrityId)
        }
    }

    fun addMessage(message: ChatMessage) {
        val currentList = messageList.value ?: mutableListOf()
        currentList.add(message)
        messageList.postValue(currentList)
    }
}