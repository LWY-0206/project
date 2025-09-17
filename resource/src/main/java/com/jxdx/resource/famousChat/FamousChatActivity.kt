package com.jxdx.resource.famousChat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.adpter.FamousChatAdapter
import com.jxdx.resource.databinding.ActivityFamousChatBinding

class FamousChatActivity : BaseActivity<ActivityFamousChatBinding>() {

    private lateinit var viewModel: FamousChatViewModel
    private lateinit var adapter: FamousChatAdapter
    private var celebrityId: Int = -1
    private var celebrityName: String = ""
    private var avatarUrl: String = ""
    private var sessionId: String = ""

    override fun bindLayout(): ActivityFamousChatBinding {
        return ActivityFamousChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 ViewModel

    }
    override fun initView() {
        viewModel = ViewModelProvider(this)[FamousChatViewModel::class.java]
        // 设置顶部名称和返回按钮
        setupViews()
        celebrityId = intent.getIntExtra("CELEBRITY_ID", -1)
        if (celebrityId == -1) {
            finish()
            return
        }
        // 设置 RecyclerView
        setupRecyclerView()

        // 初始化聊天会话
        viewModel.initChatSession(celebrityId)
    }

    override fun subscribeUi() {
        // 观察聊天会话初始化
        viewModel.chatSessionLiveData.observe(this) { resource ->
            resource.onSuccess { data ->
                data?.let {
                    sessionId = it.sessionId
                    // 添加欢迎消息
                    val welcomeMessage = ChatMessage(
                        messageId = "welcome_${System.currentTimeMillis()}",
                        content = "你好，我是${it.celebrityName}，很高兴与你交流！",
                        isUser = false,
                        timestamp = System.currentTimeMillis(),
                        avatarUrl = it.avatarUrl
                    )
                    viewModel.addMessage(welcomeMessage)
                }
            }.onError { error, _ ->
                // 显示错误
                showError(error?.message ?: "初始化聊天失败")
            }
        }

        // 观察消息列表
        viewModel.messageList.observe(this) { messages ->
            adapter.submitList(messages.toList())
            // 滚动到底部
            if (messages.isNotEmpty()) {
                view.rvMessages.postDelayed({
                    view.rvMessages.scrollToPosition(messages.size - 1)
                }, 100)
            }
        }
        // 观察发送消息结果
        viewModel.sendMessageLiveData.observe(this) { resource ->
            resource.onSuccess {
                view.progressBar.visibility = View.GONE
            }.onError { error, _ ->
                // 显示错误
                showError(error?.message ?: "发送消息失败")
                // 隐藏加载状态
                view.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupViews() {
        // 设置顶部标题
        view.tvChatTitle.text = celebrityName

        // 返回按钮
        view.ivBack.setOnClickListener {
            finish()
        }

        // 发送按钮
        view.btnSend.setOnClickListener {
            val message = view.etMessage.text.toString().trim()
            if (message.isNotEmpty() && sessionId.isNotEmpty()) {
                // 显示加载状态
                view.progressBar.visibility = View.VISIBLE
                viewModel.sendMessage(sessionId, message, celebrityId)
                view.etMessage.setText("")
            } else if (sessionId.isEmpty()) {
                showError("聊天会话未初始化，请稍后再试")
            }
        }

        // 设置输入框的文本变化监听，控制发送按钮的可用性
        view.etMessage.doAfterTextChanged { text ->
            val message = text?.toString()?.trim() ?: ""
            view.btnSend.isEnabled = message.isNotEmpty() && sessionId.isNotEmpty()
        }
    }

    private fun setupRecyclerView() {
        adapter = FamousChatAdapter(celebrityName, avatarUrl)
        view.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        view.rvMessages.adapter = adapter

        // 添加滚动监听，当用户手动滚动时暂停自动滚动
        view.rvMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 可以在这里实现更复杂的滚动行为控制
            }
        })
    }

    private fun showError(message: String) {
        if (message.isEmpty()) {
            return
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        viewModel.messageList.value?.clear()
    }
}