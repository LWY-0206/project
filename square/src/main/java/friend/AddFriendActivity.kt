package com.example.loding.friend

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.example.corekit.util.load
import com.jxdx.square.databinding.ActivityAddfriendBinding
import friend.AddDialog

class AddFriendActivity : BaseActivity<ActivityAddfriendBinding>() {
    private val searchFriendViewModel: SearchFriendViewModel by lazy {
        ViewModelProvider(this)[SearchFriendViewModel::class.java]
    }

    override fun bindLayout(): ActivityAddfriendBinding = ActivityAddfriendBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置返回按钮点击事件
        view.backButton.setOnClickListener {
            finish()
        }

        // 为搜索按钮添加点击事件
        view.searchButton.setOnClickListener {
            performSearch()
        }
        // 为添加好友添加点击事件
        view.sendRequestButton.setOnClickListener {
            val userId = view.userIdText.text.toString()
            val dialog = AddDialog()
            val bundle = Bundle()
            bundle.putString("userId", userId)
            dialog.arguments = bundle
            // 显示对话框
            dialog.show(supportFragmentManager)
        }

        // 允许按回车键执行搜索
        view.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch() {
        val searchText =
            view.searchEditText.text
                .toString()
                .trim()
        if (searchText.isNotEmpty()) {
            // 执行搜索操作
            searchFriendViewModel.getUserInfo(searchText)
            // 可以在这里添加加载指示器
        }
    }

    override fun subscribeUi() {
        // 观察搜索结果
        searchFriendViewModel.friendDetailsLiveData.observe(this) {
            it.onSuccess { userInfo ->
                userInfo?.let {
                    view.usernameText.text = userInfo.userName
                    view.userIdText.text = userInfo.id.toString()
                    view.avatarView.load(userInfo.avatarUrl, 2)
                    view.classText.text = userInfo.className
                    view.identityText.text =
                        when (userInfo.identity) {
                            0 -> "学生"
                            1 -> "老师"
                            else -> "管理员"
                        }
                    // 显示搜索结果区域
                    view.resultContainer.visibility = android.view.View.VISIBLE
                }
            }

            it.onError { error, _ ->
                // 处理搜索错误
                android.widget.Toast
                    .makeText(this, "搜索失败: ${error?.message ?: "未知错误"}", android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
