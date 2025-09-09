package com.example.corekit.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.corekit.common.BaseDialog
import com.example.corekit.databinding.DialogNormalBinding
import com.example.corekit.util.singleClick

class NormalDialog : BaseDialog<DialogNormalBinding>() {

    var listener: NormalDialogListener? = null
    var positiveText: String? = null
    var negativeText: String? = null
    var title: String? = null
    var message: String? = null

    override fun initView(view: DialogNormalBinding) {
        view.dialogTitle.text = title
        view.dialogMessage.text = message
        view.positiveBtn.text = positiveText
        view.positiveBtn.singleClick {
            listener?.onPositiveClick()
        }
        view.negativeBtn.text = negativeText
        view.negativeBtn.singleClick {
            listener?.onNegativeClick()
        }
    }

    interface NormalDialogListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    override fun getView(inflater: LayoutInflater, parent: ViewGroup?): DialogNormalBinding {
        return DialogNormalBinding.inflate(inflater)
    }
}
