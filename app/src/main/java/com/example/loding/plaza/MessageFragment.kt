package com.example.loding.plaza

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.ItemSelectListener
import com.example.loding.R
import com.example.loding.chat.ChatActivity
import com.example.loding.databinding.FragmentMessageBinding
import com.example.loding.entity.MessageItem
import com.example.loding.message.MessageAdapter

class MessageFragment : BaseFragment<FragmentMessageBinding>() {
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<MessageItem>()

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
                    startActivity(intent)

                    return true
                }
            },
        )
    }

    override fun subscribeUi() {
        // 初始化模拟数据
        initMockData()

        // 将模拟数据添加到适配器
        messageAdapter.add(messageList)
    }

    private fun initMockData() {
        // 添加3个模拟消息数据
        messageList.add(
            MessageItem(
                avatarResId = R.drawable.ic_default_avatar,
                name = "张三",
                time = "12:30",
                message = "最近在忙什么呢？好久没联系了",
            ),
        )

        messageList.add(
            MessageItem(
                avatarResId = R.drawable.ic_default_avatar,
                name = "李四",
                time = "昨天",
                message = "明天一起去看电影吧，有部新片上映了",
            ),
        )

        messageList.add(
            MessageItem(
                avatarResId = R.drawable.ic_default_avatar,
                name = "王五",
                time = "前天",
                message = "谢谢你的帮助，问题已经解决了",
            ),
        )
    }
}
