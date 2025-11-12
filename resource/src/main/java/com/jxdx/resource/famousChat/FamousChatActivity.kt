package com.jxdx.resource.famousChat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
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
    override fun bindLayout(): ActivityFamousChatBinding {
        return ActivityFamousChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 ViewModel
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        celebrityName = intent.getStringExtra("CELEBRITY_NAME") ?: ""
        avatarUrl = intent.getStringExtra("CELEBRITY_AVATAR") ?: ""
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
                    // 添加欢迎消息
                    val welcomeMessage = ChatMessage(
                        messageId = "welcome_${System.currentTimeMillis()}",
                        content = "你好，我是${celebrityName}，很高兴与你交流！",
                        isUser = false,
                        timestamp = System.currentTimeMillis(),
                        avatarUrl = avatarUrl
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
            Log.d("messageList","change")
            if (messages.isNotEmpty()) {
                view.rvMessages.postDelayed({
                    view.rvMessages.scrollToPosition(messages.size - 1)
                }, 100)
            }
        }
        // 观察发送消息结果
        viewModel.sendMessageLiveData.observe(this) { resource ->
            resource.onSuccess {
                Log.d("消息发送成功，获取返回",viewModel.messageList.toString())
            }.onError { error, _ ->
                Log.d("sendMesssageLiveData","失败")
                // 显示错误
                showError(error?.message ?: "发送消息失败")
                // 隐藏加载状态1
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
            if (message.isNotEmpty())  {
                viewModel.sendMessage(message, celebrityId)
                view.etMessage.setText("")
                    view.etMessage.isEnabled = true
            }
        }

        // 设置输入框的文本变化监听，控制发送按钮的可用性
        view.etMessage.doAfterTextChanged { text ->
            val message = text?.toString()?.trim() ?: ""
            view.btnSend.isEnabled = message.isNotEmpty()
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