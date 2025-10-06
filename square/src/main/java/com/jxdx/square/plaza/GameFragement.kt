
package com.jxdx.square.plaza

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.corekit.common.BaseFragment
import com.jxdx.square.R
import com.jxdx.square.databinding.FragmentGameBinding

class GameFragment : BaseFragment<FragmentGameBinding>() {
    override fun bindLayout(): FragmentGameBinding = FragmentGameBinding.inflate(layoutInflater)

    override fun initView() {
        // 查找文本视图
        val textView: TextView? = find.artisticText
        if (textView != null) {
            // 设置文本动画效果，让文字优雅地显示出来
            textView.alpha = 0f
            textView.animate()
                .alpha(1f)
                .setDuration(1000)
                .start()
        }
    }

    override fun subscribeUi() {
        // 无需要订阅的UI元素
    }
}