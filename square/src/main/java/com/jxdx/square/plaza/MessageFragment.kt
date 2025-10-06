package com.jxdx.square.plaza

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.ItemSelectListener
import com.jxdx.square.R
import com.jxdx.square.chat.ChatActivity
import com.jxdx.square.databinding.FragmentMessageBinding
import com.jxdx.square.entity.MessageItem
import com.jxdx.square.message.MessageAdapter
import com.jxdx.square.message.MessageViewModel

class MessageFragment : BaseFragment<FragmentMessageBinding>() {
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<MessageItem>()
    
    private val viewModel: MessageViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[MessageViewModel::class.java]
    }

    override fun bindLayout(): FragmentMessageBinding = FragmentMessageBinding.inflate(layoutInflater)

    override fun initView() {
        // 初始化RecyclerView
        find.rvFriendMessages.layoutManager = LinearLayoutManager(context)
        find.rvFriendMessages.setHasFixedSize(true)

        // 初始化适配器
        messageAdapter = MessageAdapter()
        find.rvFriendMessages.adapter = messageAdapter

        // 设置点击事件监听器
        messageAdapter.setOnItemClickListener(
            object : ItemSelectListener {
                override fun onItemClick(position: Int): Boolean {
                    // 获取点击的消息项
                    val messageItem = messageList[position]

                    // 创建Intent并启动ChatActivity
                    val intent = Intent(context, ChatActivity::class.java)
                    // 传递用户名参数
                    intent.putExtra("USER_NAME", messageItem.name)
                    // 传递好友ID参数
                    messageItem.friendId?.let {
                        intent.putExtra("FRIEND_ID", it)
                    }
                    // 传递好友头像URL参数
                    messageItem.avatarUrl?.let {
                        intent.putExtra("FRIEND_AVATAR", it)
                    }
                    startActivity(intent)

                    return true
                }
            },
        )
    }

    override fun subscribeUi() {
        // 加载聊天好友数据
        loadChatFriends()
        
        // 监听数据变化
        viewModel.messageListLiveData.observe(this) {
            it.onSuccess { messages ->
                messageList.clear()
                messages?.let { messageList.addAll(it) }
                messageAdapter.add(messageList)
            }
            it.onError { error, data ->
                // 处理错误，可以显示错误提示
                android.util.Log.e("MessageFragment", "加载好友消息失败: ${error?.message}")
                // 如果加载失败，显示空列表
                messageList.clear()
                messageAdapter.add(messageList)
            }
        }
    }

    private fun loadChatFriends() {
        viewModel.loadChatFriends()
    }
}
