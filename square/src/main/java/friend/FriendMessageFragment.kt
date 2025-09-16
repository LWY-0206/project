package com.example.loding.friend

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.jxdx.square.databinding.FragmentFriendmessageBinding

class FriendMessageFragment : BaseFragment<FragmentFriendmessageBinding>() {
    override fun bindLayout(): FragmentFriendmessageBinding = FragmentFriendmessageBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置返回按钮点击事件
        find.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 初始化RecyclerView
        find.friendRequestRecyclerView.layoutManager = LinearLayoutManager(context)
        find.friendRequestRecyclerView.setHasFixedSize(true)
    }

    override fun subscribeUi() {
        // 这里可以添加数据订阅逻辑，但用户要求不需要写适配器，所以保持简洁
    }
}
