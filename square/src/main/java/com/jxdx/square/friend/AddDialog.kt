package com.jxdx.square.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseDialog
import com.example.corekit.http.bean.Resource
import com.jxdx.square.databinding.AddDialogBinding

class AddDialog : BaseDialog<AddDialogBinding>() {
    private lateinit var sendApplicationViewmodel: SendApplicationViewmodel

    init {
        alpha = 0.5f
        ifCancelOnTouch = true
        enableBack = true
    }

    override fun initView(view: AddDialogBinding) {
        // 初始化ViewModel
        sendApplicationViewmodel = ViewModelProvider(requireActivity())[SendApplicationViewmodel::class.java]

        // 获取从AddFriendActivity传递过来的userId参数
        val userId = arguments?.getString("userId") ?: ""

        view.cancelButton.setOnClickListener {
            dismiss()
        }

        view.addButton.setOnClickListener {
            val remark = view.reasonInput.text.toString()

            if (userId.isEmpty()) {
                Toast.makeText(context, "用户ID不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 创建ApplicationBody对象，使用从arguments获取的userId
            val applicationBody =
                ApplicationBody(
                    friendId = userId,
                    remark = remark,
                )
            // 这个是AI的代码，实在不会调顺序，用了AI的
            // 移除之前可能存在的观察者
            sendApplicationViewmodel.sendApplicationLiveData.removeObservers(viewLifecycleOwner)

            // 先添加观察者再发送请求
            sendApplicationViewmodel.sendApplicationLiveData.observe(viewLifecycleOwner) {
                when {
                    it.status == Resource.Status.SUCCESS -> {
                        // 确保context不为空
                        context?.let { ctx ->
                            Toast.makeText(ctx, "添加成功", Toast.LENGTH_SHORT).show()
                        }
                        dismiss()
                    }
                    it.status == Resource.Status.ERROR -> {
                        context?.let { ctx ->
                            Toast.makeText(ctx, it.error?.message ?: "添加失败", Toast.LENGTH_SHORT).show()
                        }
                        dismiss()
                    }
                }
            }

            // 发送添加好友请求
            sendApplicationViewmodel.addFriend(applicationBody)
        }
    }

    override fun getView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
    ): AddDialogBinding = AddDialogBinding.inflate(inflater, parent, false)
}
