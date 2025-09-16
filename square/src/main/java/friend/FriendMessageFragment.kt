package com.example.loding.friend

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.jxdx.square.databinding.FragmentFriendmessageBinding
import friend.ApplicationAdapter
import friend.FriendMessageViewModel

class FriendMessageFragment : BaseFragment<FragmentFriendmessageBinding>() {
    val viewModel: FriendMessageViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application),
        )[FriendMessageViewModel::class.java]
    }

    override fun bindLayout(): FragmentFriendmessageBinding = FragmentFriendmessageBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置返回按钮点击事件
        find.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 初始化RecyclerView
        find.friendRequestRecyclerView.layoutManager = LinearLayoutManager(context)
        find.friendRequestRecyclerView.setHasFixedSize(true)
        val adapter = ApplicationAdapter(arrayListOf())
        find.friendRequestRecyclerView.adapter = adapter
    }

    override fun subscribeUi() {
        viewModel.applicationLiveData.observe(viewLifecycleOwner) { resource ->
            resource.onSuccess { applicationList ->
                val adapter = find.friendRequestRecyclerView.adapter as? ApplicationAdapter
                // 使用BaseTypeAdapter提供的clearAndAdd方法更新数据
                applicationList?.let {
                    adapter?.clearAndAdd(it)
                }
            }
        }

        viewModel.getApplications()
    }
}
