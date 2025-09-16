package com.example.loding.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.corekit.common.BaseDialog
import com.jxdx.square.databinding.FriendDialogBinding

class DeleteDialog : BaseDialog<FriendDialogBinding>() {
    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        alpha = 0F
        ifCancelOnTouch = true
        enableBack = true
    }

    override fun initView(view: FriendDialogBinding) {
        // 在这⾥初始化对话框的视图和设置逻辑

        view.cancelButton.setOnClickListener {
            dismiss()
        }
        view.deleteButton.setOnClickListener {
            // 删除逻辑

            dismiss()
        }
    }

    override fun getView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
    ): FriendDialogBinding = FriendDialogBinding.inflate(layoutInflater, parent, false)
}
